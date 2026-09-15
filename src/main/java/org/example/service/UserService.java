package org.example.service;

import org.example.request.ChangePasswordRequest;
import org.example.request.CreateUserRequest;
import org.example.response.UserResponse;

import java.util.List;

public interface UserService {

    List<UserResponse> listUsers();

    UserResponse createUser(CreateUserRequest request);

    void changePassword(String username, ChangePasswordRequest request);
}
