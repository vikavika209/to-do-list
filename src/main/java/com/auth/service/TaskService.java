package com.auth.service;

import com.auth.entity.Task;
import com.auth.exception.WrongIdException;
import com.auth.repository.TaskRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class TaskService {
    private final TaskRepository taskRepository;

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


}
