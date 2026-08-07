package com.jjap.berries.auth.service;

import com.jjap.berries.auth.dto.SignupRequest;
import com.jjap.berries.auth.dto.SignupResponse;
import com.jjap.berries.global.exception.BusinessException;
import com.jjap.berries.global.exception.ErrorCode;
import com.jjap.berries.user.domain.User;
import com.jjap.berries.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public SignupResponse signup(SignupRequest request) {
        validateDuplicate(request);

        User user = new User(
                request.email(),
                passwordEncoder.encode(request.password()),
                request.nickname()
        );

        return SignupResponse.from(userRepository.save(user));
    }

    private void validateDuplicate(SignupRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }
        if (userRepository.existsByNickname(request.nickname())) {
            throw new BusinessException(ErrorCode.NICKNAME_ALREADY_EXISTS);
        }
    }

}
