package com.auth.controller;

import com.auth.entity.Task;
import com.auth.exception.WrongIdException;
import com.auth.service.TaskService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@AllArgsConstructor
@RequestMapping("/api/tasks")
public class TaskController {
    private final TaskService taskService;

    @PostMapping("/new")
    public ResponseEntity<Task> createTask(@RequestBody Task task) {
        Task saved = taskService.addTask(task);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/admin/update/{id}")
    public ResponseEntity<Task> updateTask(@PathVariable Long id, @RequestBody Task task) {
        try {
            return ResponseEntity.ok(taskService.updateTask(id, task));
        }catch (WrongIdException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Задача с %d не найдена".formatted(id));
        }
    }

    @PutMapping("/admin/{id}")
    public ResponseEntity<String> updateTaskStatus(@PathVariable Long id) {
        try {
            taskService.markAsDoneTask(id);
            return ResponseEntity.ok("Задача выполнена");
        }catch (WrongIdException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Задача с %d не найдена".formatted(id));
        }
    }

    @GetMapping("/all_tasks")
    public ResponseEntity<Page<Task>> getAllTasks(Pageable pageable) {
        return ResponseEntity.ok(taskService.getTasks(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Task> getTaskById(@PathVariable("id") Long id) {
        return taskService.getTaskById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Задача с %d не найдена".formatted(id)));
    }

    @DeleteMapping("/admin/delete/{id}")
    public ResponseEntity<String> deleteTask(@PathVariable("id") Long id) {
        try{
            taskService.deleteTask(id);
            return ResponseEntity.ok("Задача удалена");
        }catch (WrongIdException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Не удалось найти задачу с id: " + id);
        }
    }

    @GetMapping("/undone")
    public ResponseEntity<Page<Task>> getUndoneTasks(Pageable pageable) {
        return ResponseEntity.ok(taskService.getUndoneTasks(pageable));
    }

    @GetMapping("/done")
    public ResponseEntity<Page<Task>> getDoneTasks(Pageable pageable) {
        return ResponseEntity.ok(taskService.getUndoneTasks(pageable));
    }
}
