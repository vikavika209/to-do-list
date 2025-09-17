package com.auth.entity;

import lombok.*;
import jakarta.persistence.*;

@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "tasks")
@EqualsAndHashCode
@Getter
@Setter
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "done", nullable = false)
    boolean done;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public Task(String name, boolean done) {
        this.name = name;
        this.done = done;
    }

    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", done=" + done +
                ", user=" + (user != null ? user.getUsername() : "null") +
                '}';
    }
}
