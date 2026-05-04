package com.hms.service;

import com.hms.entity.User;
import com.hms.exception.ApiException;
import com.hms.repository.UserRepository;
import com.hms.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public User requireCurrentUser() {
        String email = SecurityUtil.currentEmail();
        if (email == null) throw new ApiException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Unauthorized"));
    }

    public User save(User user) {
        if (user == null) throw new IllegalArgumentException("User cannot be null");
        return userRepository.save(user);
    }
}

