package com.auth.service;

import com.auth.entity.Task;
import com.auth.exception.WrongIdException;
import com.auth.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskService {
    private final TaskRepository taskRepository;

    @Value("${app.export-dir}")
    private String exportDir;

    public Task addTask(Task task) {
        Task savedTask = taskRepository.save(task);
        log.info("Задача сохранена: {}", savedTask);
        return savedTask;
    }

    public Optional<Task> getTaskById(Long id) {
        log.info("Получение задачи с id: {}", id);
        return taskRepository.findById(id);
    }

    public Task updateTask(Long id, Task task) {
        log.info("Получена задача: {}", task);

        Task getTask = taskRepository.findById(id)
                .orElseThrow(() -> new WrongIdException("Задача не найдена: " + task.getId()));

        log.info("Задача до обновления: {}", getTask.toString());

        getTask.setName(task.getName());
        getTask.setDone(task.isDone());

        Task savedTask = taskRepository.save(getTask);

        log.info("Задача обновлена: {}", savedTask);

        return savedTask;
    }

    public Page<Task> getTasks(Pageable pageable) {
        return taskRepository.findAll(pageable);
    }

    public void deleteTask(Long id) {
        Task task = getTask(id);
        taskRepository.delete(task);
    }

    private Task updateTaskStatus(Long id, boolean status) {
        Task task = getTask(id);
        task.setDone(status);
        Task savedTask = taskRepository.save(task);
        log.info("Статус обновлён: {}", savedTask.toString());
        return savedTask;
    }

    public void markAsDoneTask(Long id) {
        Task task = updateTaskStatus(id, true);
        log.info("Задача выполнена: {}", task.toString());
    }

    private Task getTask(Long id) {
        Optional<Task> optionalTask = getTaskById(id);

        if (optionalTask.isEmpty()) {
            log.error("Не удалось найти задачу с id: {}", id);
            throw new WrongIdException("Не удалось найти задачу с id: " + id);
        }
        return optionalTask.get();
    }

    public Page<Task> getUndoneTasks(Pageable pageable) {
        return getTaskByStatus(pageable, false);
    }

    public Page<Task> getDoneTasks(Pageable pageable) {
        return getTaskByStatus(pageable, true);
    }

    private Page<Task> getTaskByStatus(Pageable pageable, boolean status) {
        List<Task> allTasks = taskRepository.findAll().stream()
                .filter(t -> t.isDone() == status)
                .collect(Collectors.toList());
        return new PageImpl<>(allTasks, pageable, allTasks.size());
    }

    public Page<Task> getAllTasksByUserId(Long userId, Pageable pageable){
       List<Task> allTasksByUserId = taskRepository.findAll().stream()
               .filter(t -> t.getUser().getId().equals(userId))
               .collect(Collectors.toList());
       return new PageImpl<>(allTasksByUserId, pageable, allTasksByUserId.size());
    }

    public Page<Task> getAllUndoneTasksByUserId(Long userId, Pageable pageable){
        List<Task> allTasksByUserId = taskRepository.findAll().stream()
                .filter(t -> t.getUser().getId().equals(userId))
                .filter(t -> t.isDone() == false)
                .collect(Collectors.toList());
        return new PageImpl<>(allTasksByUserId, pageable, allTasksByUserId.size());
    }

    public Page<Task> getAllDoneTasksByUserId(Long userId, Pageable pageable){
        List<Task> allTasksByUserId = taskRepository.findAll().stream()
                .filter(t -> t.getUser().getId().equals(userId))
                .filter(t -> t.isDone() == true)
                .collect(Collectors.toList());
        return new PageImpl<>(allTasksByUserId, pageable, allTasksByUserId.size());
    }

    private void exportToFile (Page<Task> tasks, String fileName, OutputStream out) throws IOException {
        Path dirPath = Paths.get(exportDir);
        Files.createDirectories(dirPath);

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
                row.createCell(3).setCellValue(task.getUser() != null ? task.getUser().getId() : -1);
            }
            workbook.write(out);
        }

    }

    public void exportAllTasks(OutputStream out) throws IOException {
        Page<Task> tasks = getTasks(Pageable.unpaged());
        exportToFile(tasks, "all_tasks.xlsx", out);
    }

    public void exportUndoneTasks(OutputStream out) throws IOException {
        Page<Task> tasks = getUndoneTasks(Pageable.unpaged());
        exportToFile(tasks, "undone_tasks.xlsx", out);
    }

    public void exportDoneTasks(OutputStream out) throws IOException {
        Page<Task> tasks = getDoneTasks(Pageable.unpaged());
        exportToFile(tasks, "done_tasks.xlsx", out);
    }

    public void exportAllTasksByUserId(Long id, OutputStream out) throws IOException {
        Page<Task> tasks = getAllTasksByUserId(id, Pageable.unpaged());
        exportToFile(tasks, "all_tasks_" + id + ".xlsx", out);
    }

    public void exportUndoneTasksByUserId(Long id, OutputStream out) throws IOException {
        Page<Task> tasks = getAllUndoneTasksByUserId(id, Pageable.unpaged());
        exportToFile(tasks, "undone_tasks_" + id + ".xlsx", out);
    }

    public void exportDoneTasksByUserId(Long id, OutputStream out) throws IOException {
        Page<Task> tasks = getAllDoneTasksByUserId(id, Pageable.unpaged());
        exportToFile(tasks, "done_tasks_" + id + ".xlsx", out);
    }
}
