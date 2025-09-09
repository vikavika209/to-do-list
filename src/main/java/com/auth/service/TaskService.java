package com.auth.service;

import com.auth.entity.Task;
import com.auth.exception.WrongIdException;
import com.auth.repository.TaskRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

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

    public Optional<Task> getTask(long id) {
        log.info("Получение задачи с id: ", id);
        return taskRepository.findById(id);
    }

    public Task updateTask(Task task) {
        Task getTask = taskRepository.findById(task.getId())
                .orElseThrow(() -> new WrongIdException("Задача не найдена: " + task.getId()));

        log.info("Задача до обновления: ", getTask);

        getTask.setName(task.getName());
        getTask.setDone(task.isDone());

        log.info("Задача обновлена: {}", getTask);

        return getTask;
    }

    public List<Task> getTasks() {
        return taskRepository.findAll();
    }

    public void deleteTask(long id) {
        Optional<Task> optionalTask = getTask(id);

        if (optionalTask.isEmpty()) {
            log.error("Не удалось найти задачу с id: {}", id);
            throw new WrongIdException("Не удалось найти задачу с id: " + id);
        }
        taskRepository.delete(optionalTask.get());
    }
}
