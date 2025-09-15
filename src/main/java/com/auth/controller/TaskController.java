package com.auth.controller;

import com.auth.entity.Task;
import com.auth.exception.WrongIdException;
import com.auth.service.TaskService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/tasks")
public class TaskController {
    private final TaskService taskService;

    @Value("${app.export-dir}")
    private String exportDir;

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

    @GetMapping("/all_tasks/export")
    public void exportTasksToExcel(HttpServletResponse response, Pageable pageable) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=tasks.xlsx");

        Page<Task> tasks = taskService.getTasks(pageable);

        Path dirPath = Paths.get(exportDir);
        Files.createDirectories(dirPath);

        String fileName = "tasks.xlsx";
        Path path = dirPath.resolve(fileName);

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Tasks");

            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("ID");
            headerRow.createCell(1).setCellValue("Name");
            headerRow.createCell(2).setCellValue("Done");
            headerRow.createCell(3).setCellValue("User ID");

            int rowNum = 1;
            for (Task task : tasks.getContent()) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(task.getId());
                row.createCell(1).setCellValue(task.getName());
                row.createCell(2).setCellValue(task.isDone());
                row.createCell(3).setCellValue(task.getUserId() != null ? task.getUserId() : -1);
            }

            try (FileOutputStream fileOut = new FileOutputStream(path.toFile())) {
                workbook.write(fileOut);
            }
        }


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
        return ResponseEntity.ok(taskService.getDoneTasks(pageable));
    }
    @GetMapping("/all_tasks/{id}")
    public ResponseEntity<Page<Task>> getAllTasksByUserId (@PathVariable Long id, Pageable pageable){
        return ResponseEntity.ok(taskService.getAllTasksByUserId(id, pageable));
    }

    @GetMapping("/all_undone_tasks/{id}")
    public ResponseEntity<Page<Task>> getAllUndoneTasksByUserId (
            @PathVariable Long id,
            Pageable pageable
    ){
        return ResponseEntity.ok(taskService.getAllUndoneTasksByUserId(id, pageable));
    }

    @GetMapping("/all_done_tasks/{id}")
    public ResponseEntity<Page<Task>> getAllDoneTasksByUserId
            (
             @PathVariable Long id,
             Pageable pageable
    ){
        return ResponseEntity.ok(taskService.getAllDoneTasksByUserId(id, pageable));
    }
}
