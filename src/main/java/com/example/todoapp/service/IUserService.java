package com.example.todoapp.service;

import com.example.todoapp.model.User;

public interface IUserService {
    User registerUser(String email, String password);

    User loginUser(String email, String password);

    User findByEmail(String email);
}
