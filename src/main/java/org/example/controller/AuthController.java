package org.example.controller;

import org.example.exception.InvalidCredentialsException;
import org.example.request.ChangePasswordRequest;
import org.example.request.LoginRequest;
import org.example.response.LoginResponse;
import org.example.security.JwtService;
import org.example.security.LoginAttemptService;
import org.example.security.UserPrincipal;
import org.example.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    // Named for easy log-routing later (e.g. a separate appender/alert) - kept
    // distinct from the class's own logger.
    private static final Logger auditLog = LoggerFactory.getLogger("SECURITY_AUDIT");

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final LoginAttemptService loginAttemptService;
    private final UserService userService;

    public AuthController(AuthenticationManager authenticationManager, JwtService jwtService,
                           LoginAttemptService loginAttemptService, UserService userService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.loginAttemptService = loginAttemptService;
        this.userService = userService;
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        String username = request.getUsername() == null ? "" : request.getUsername().trim();
        if (loginAttemptService.isLocked(username)) {
            auditLog.warn("Login blocked for '{}' - too many recent failed attempts", username);
            throw new InvalidCredentialsException("Too many failed attempts. Try again in a few minutes.");
        }

        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, request.getPassword()));
        } catch (AuthenticationException e) {
            loginAttemptService.recordFailure(username);
            auditLog.warn("Failed login attempt for '{}'", username);
            throw new InvalidCredentialsException("Invalid username or password");
        }

        loginAttemptService.recordSuccess(username);
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        auditLog.info("Successful login for '{}' (role {})", principal.getUsername(), principal.getUser().getRole());
        String token = jwtService.generateToken(principal);
        return new LoginResponse(token, principal.getUsername(), principal.getUser().getRole().name());
    }

    @PutMapping("/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(@RequestBody ChangePasswordRequest request, Authentication authentication) {
        userService.changePassword(authentication.getName(), request);
        auditLog.info("Password changed for '{}'", authentication.getName());
    }
}
