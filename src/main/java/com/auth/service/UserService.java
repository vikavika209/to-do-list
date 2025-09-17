package com.auth.service;

import com.auth.dto.UserDTO;
import com.auth.entity.User;
import com.auth.exception.PasswordNotMatchesException;
import com.auth.repository.UserRepository;
import com.auth.security.JwtTokenProvider;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public String generateToken(UserDTO userDTO) {
        Optional<User> optionalUser = userRepository.findByUsername(userDTO.getUsername());
        if (!optionalUser.isPresent()) {
            throw new UsernameNotFoundException("Пользователь не найден: " + userDTO.getUsername());
        }
        User user = optionalUser.get();
        if (!passwordEncoder.matches(userDTO.getPassword(), user.getPassword())) {
            throw new PasswordNotMatchesException("Неверный пароль: " + user.getUsername());
        }
        return jwtTokenProvider.generateToken(userDTO.getUsername(), user.getRoles());
    }

    public User save(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public User getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден: " + username));
    }

    public User updateUser(User user) {
        User userInDataBase = getUser(user.getUsername());
        userInDataBase.setUsername(user.getUsername());
        userInDataBase.setPassword(passwordEncoder.encode(user.getPassword()));
        userInDataBase.setRoles(user.getRoles());
        return userRepository.save(userInDataBase);
    }

    public Page<User> allUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    public void deleteUser(String username) {
        User user = getUser(username);
        userRepository.delete(user);
    }
}
