package com.jjap.berries.global.security;

import com.jjap.berries.user.domain.UserRole;

public record AuthenticatedUser(Long userId, UserRole role) {}
