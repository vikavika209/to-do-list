package com.auth.component;

import com.auth.entity.Task;
import com.auth.entity.User;
import com.auth.repository.TaskRepository;
import com.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AppStartupListener implements ApplicationListener<ApplicationReadyEvent> {

    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        if (userRepository.count() == 0) {
            List<User> users = new ArrayList<>();

            for (int i = 1; i <= 10; i++) {
                User user = new User();
                user.setUsername("user" + i);
                user.setPassword(passwordEncoder.encode("password" + i));

                if (i == 1) {
                    user.setRoles(List.of("ADMIN", "USER"));
                } else {
                    user.setRoles(List.of("USER"));
                }

                users.add(user);
            }

            userRepository.saveAll(users);

            users.forEach(user -> {
                List<Task> tasks = new ArrayList<>();
                for (int j = 1; j <= 3; j++) {
                    Task task = new Task();
                    task.setName("Task " + j + " for " + user.getUsername());
                    task.setDone(false);
                    task.setUser(user);
                    tasks.add(task);
                }
                taskRepository.saveAll(tasks);
            });

            System.out.println("🎉 Приложение готово к работе!");
        }
    }
}