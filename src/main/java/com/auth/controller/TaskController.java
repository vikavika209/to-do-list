package com.auth.controller;

import com.auth.dto.UserDTO;
import com.auth.entity.Task;
import com.auth.entity.User;
import com.auth.exception.PasswordNotMatchesException;
import com.auth.exception.WrongIdException;
import com.auth.service.TaskService;
import com.auth.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class TaskController {
    private final TaskService taskService;
    private final UserService userService;

    @Value("${app.export-dir}")
    private String exportDir;


    @GetMapping("/token")
    public String getToken(@RequestBody UserDTO userDTO) {
        try{
            return userService.generateToken(userDTO);
        }catch (UsernameNotFoundException | PasswordNotMatchesException e){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
        }
    }

    @PostMapping("/tasks/new")
    public ResponseEntity<Task> createTask(@RequestBody Task task) {
        Task saved = taskService.addTask(task);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/tasks/admin/update/{id}")
    public ResponseEntity<Task> updateTask(@PathVariable Long id, @RequestBody Task task) {
        try {
            return ResponseEntity.ok(taskService.updateTask(id, task));
        }catch (WrongIdException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Задача с %d не найдена".formatted(id));
        }
    }

    @PutMapping("/tasks/admin/{id}")
    public ResponseEntity<String> updateTaskStatus(@PathVariable Long id) {
        try {
            taskService.markAsDoneTask(id);
            return ResponseEntity.ok("Задача выполнена");
        }catch (WrongIdException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Задача с %d не найдена".formatted(id));
        }
    }

    @GetMapping("/tasks/all_tasks")
    public ResponseEntity<Page<Task>> getAllTasks(Pageable pageable) {
        return ResponseEntity.ok(taskService.getTasks(pageable));
    }

    @GetMapping("/tasks/all_tasks/export")
    public void exportTasksToExcel(HttpServletResponse response, Pageable pageable) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=tasks.xlsx");
        taskService.exportAllTasks(response.getOutputStream());
    }

    @GetMapping("/tasks/undone_tasks/export")
    public void exportUndoneTasksToExcel(HttpServletResponse response, Pageable pageable) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=tasks.xlsx");
        taskService.exportUndoneTasks(response.getOutputStream());
    }

    @GetMapping("/tasks/done_tasks/export")
    public void exportDoneTasksToExcel(HttpServletResponse response, Pageable pageable) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=tasks.xlsx");
        taskService.exportDoneTasks(response.getOutputStream());
    }

    @GetMapping("/tasks/all_tasks/export/{id}")
    public void exportAllTasksToExcelByUserId(HttpServletResponse response, Pageable pageable, @PathVariable Long id)
            throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=tasks.xlsx");
        taskService.exportAllTasksByUserId(id, response.getOutputStream());
    }

    @GetMapping("/tasks/undone_tasks/export/{id}")
    public void exportUndoneTasksToExcelByUserId(HttpServletResponse response, Pageable pageable, @PathVariable Long id)
            throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=tasks.xlsx");
        taskService.exportUndoneTasksByUserId(id, response.getOutputStream());
    }

    @GetMapping("/tasks/done_tasks/export/{id}")
    public void exportDoneTasksToExcelByUserId(HttpServletResponse response, Pageable pageable, @PathVariable Long id)
            throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=tasks.xlsx");
        taskService.exportDoneTasksByUserId(id, response.getOutputStream());
    }

    @GetMapping("/tasks/{id}")
    public ResponseEntity<Task> getTaskById(@PathVariable("id") Long id) {
        return taskService.getTaskById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Задача с %d не найдена".formatted(id)));
    }

    @DeleteMapping("/tasks/admin/delete/{id}")
    public ResponseEntity<String> deleteTask(@PathVariable("id") Long id) {
        try{
            taskService.deleteTask(id);
            return ResponseEntity.ok("Задача удалена");
        }catch (WrongIdException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Не удалось найти задачу с id: " + id);
        }
    }

    @GetMapping("/tasks/undone")
    public ResponseEntity<Page<Task>> getUndoneTasks(Pageable pageable) {
        return ResponseEntity.ok(taskService.getUndoneTasks(pageable));
    }

    @GetMapping("/tasks/done")
    public ResponseEntity<Page<Task>> getDoneTasks(Pageable pageable) {
        return ResponseEntity.ok(taskService.getDoneTasks(pageable));
    }
    @GetMapping("/tasks/all_tasks/{id}")
    public ResponseEntity<Page<Task>> getAllTasksByUserId (@PathVariable Long id, Pageable pageable){
        return ResponseEntity.ok(taskService.getAllTasksByUserId(id, pageable));
    }

    @GetMapping("/tasks/all_undone_tasks/{id}")
    public ResponseEntity<Page<Task>> getAllUndoneTasksByUserId (
            @PathVariable Long id,
            Pageable pageable
    ){
        return ResponseEntity.ok(taskService.getAllUndoneTasksByUserId(id, pageable));
    }

    @GetMapping("/tasks/all_done_tasks/{id}")
    public ResponseEntity<Page<Task>> getAllDoneTasksByUserId
            (
             @PathVariable Long id,
             Pageable pageable
    ){
        return ResponseEntity.ok(taskService.getAllDoneTasksByUserId(id, pageable));
    }
}
