package com.example.todoapp.service;

import com.example.todoapp.model.ToDo;
import com.example.todoapp.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IToDoService {
    Page<ToDo> getToDoItemsByUser(User user, Pageable pageable);

    Page<ToDo> searchToDoItems(User user, String keyword, String status, Pageable pageable);

    void createToDoItem(ToDo todo, String userEmail);

    boolean updateToDoItem(ToDo todo, String userEmail);

    boolean deleteToDoItem(Long id, String userEmail);
}
