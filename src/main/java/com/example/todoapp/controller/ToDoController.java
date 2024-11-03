package com.example.todoapp.controller;

import com.example.todoapp.model.ToDo;
import com.example.todoapp.model.User;
import com.example.todoapp.service.ToDoService;
import com.example.todoapp.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@RestController
public class ToDoController {
    private final ToDoService toDoService;
    private static final Logger logger = LoggerFactory.getLogger(ToDoController.class);

    @Autowired
    public ToDoController(ToDoService toDoService) {
        this.toDoService = toDoService;
    }

    @GetMapping("/")
    public ResponseEntity<List<ToDo>> getUserToDoItems(
            Authentication authentication,
            @RequestParam(value = "sortBy", required = false) String sortBy,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {

        User user = (User) authentication.getPrincipal();
        Pageable pageable = PageRequest.of(page, size,
                sortBy != null ? Sort.by(sortBy) : Sort.unsorted());

        Page<ToDo> todos = toDoService.getToDoItemsByUser(user, pageable);
        return ResponseEntity.ok(todos.getContent());
    }

    @GetMapping("/search")
    public ResponseEntity<List<ToDo>> searchToDoItems(
            Authentication authentication,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {

        User user = (User) authentication.getPrincipal();
        Pageable pageable = PageRequest.of(page, size);

        Page<ToDo> todos = toDoService.searchToDoItems(user, keyword, status, pageable);
        return ResponseEntity.ok(todos.getContent());
    }

    @PostMapping("/addToDoItem")
    public ResponseEntity<String> addToDoItem(@RequestBody @Valid ToDo todo) {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        try {
            toDoService.createToDoItem(todo, userEmail);  // Pass the user's email to the service
            logger.info("ToDo item added successfully: {}", todo);
            return ResponseEntity.status(HttpStatus.CREATED).body("Todo item added successfully.");
        } catch (Exception e) {
            logger.error("Error adding Todo item: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to add todo item.");
        }
    }

    @PutMapping("/updateToDoItem/{id}")
    public ResponseEntity<String> updateToDoItem(@PathVariable Long id, @RequestBody @Valid ToDo todo) {
        todo.setId(id);
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        try {
            boolean isUpdated = toDoService.updateToDoItem(todo, userEmail);
            if (isUpdated) {
                logger.info("ToDo item updated successfully by user {}: {}", userEmail, todo);
                return ResponseEntity.ok("Edit Success");
            } else {
                logger.warn("ToDo item not found or unauthorized update attempt by user {}: {}", userEmail, id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Todo item not found or not authorized to update.");
            }
        } catch (Exception e) {
            logger.error("Error updating Todo item by user {}: {}", userEmail, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to update todo item.");
        }
    }

    @DeleteMapping("/deleteToDoItem/{id}")
    public ResponseEntity<String> deleteToDoItem(@PathVariable Long id) {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        try {
            if (toDoService.deleteToDoItem(id, userEmail)) {
                logger.info("Successfully deleted ToDo item with ID: {}", id);
                return ResponseEntity.ok("Delete Success");
            } else {
                logger.warn("Failed to delete ToDo item. Item with ID {} not found.", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("ToDo item with ID " + id + " not found.");
            }
        } catch (RuntimeException e) {
            logger.error("Error deleting ToDo item: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }
}
