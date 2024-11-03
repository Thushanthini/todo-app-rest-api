package com.example.todoapp.service;

import com.example.todoapp.model.ToDo;
import com.example.todoapp.model.User;
import com.example.todoapp.repository.IToDoRepository;
import com.example.todoapp.repository.IUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

@Service
public class ToDoService implements IToDoService {
    private final IToDoRepository repository;
    private final IUserRepository userRepository;
    private static final Logger logger = LoggerFactory.getLogger(ToDoService.class);

    @Autowired
    public ToDoService(IToDoRepository repository, IUserRepository userRepository) {
        this.repository = repository;
        this.userRepository = userRepository;
    }

    @Override
    public Page<ToDo> getToDoItemsByUser(User user, Pageable pageable) {
        return repository.findByUser(user, pageable);
    }

    @Override
    public Page<ToDo> searchToDoItems(User user, String keyword, String status, Pageable pageable) {
        return repository.searchByKeywordAndStatus(user, keyword != null ? keyword : "",
                status != null ? status : "", pageable);
    }

    @Override
    public void createToDoItem(ToDo todo, String userEmail) {
        // Assuming User is the entity class for users, find the user by email
        User user = userRepository.findByEmail(userEmail).orElseThrow(() -> new RuntimeException("User not found."));
        todo.setUser(user);  // Set the authenticated user on the ToDo item

        try {
            repository.save(todo);
            logger.info("ToDo item created: {}", todo);
        } catch (Exception e) {
            logger.error("Error creating ToDo item: {}", e.getMessage());
            throw new RuntimeException("Failed to create Todo item.");
        }
    }

    @Override
    public boolean updateToDoItem(ToDo todo, String userEmail) {
        Optional<ToDo> existingToDo = repository.findById(todo.getId());

        if (existingToDo.isPresent() && existingToDo.get().getUser().getEmail().equals(userEmail)) {
            try {
                todo.setUser(existingToDo.get().getUser());  // Ensure the user remains the same
                repository.save(todo);
                logger.info("ToDo item updated by user {}: {}", userEmail, todo);
                return true;
            } catch (Exception e) {
                logger.error("Error updating ToDo item: {}", e.getMessage());
                throw new RuntimeException("Failed to update Todo item.");
            }
        } else {
            logger.warn("ToDo item not found or unauthorized update attempt by user {}: {}", userEmail, todo.getId());
            return false;
        }
    }

    @Override
    public boolean deleteToDoItem(Long id, String userEmail) {
        Optional<ToDo> todo = repository.findById(id);

        if (todo.isPresent()) {
            // Check if the ToDo belongs to the user
            if (todo.get().getUser().getEmail().equals(userEmail)) {
                repository.deleteById(id);
                logger.info("ToDo item with ID {} deleted successfully in the service layer.", id);
                return true;
            } else {
                logger.warn("User {} attempted to delete ToDo item with ID {} that does not belong to them.", userEmail, id);
                throw new RuntimeException("Unauthorized action: You cannot delete this ToDo item.");
            }
        } else {
            logger.warn("Attempted to delete non-existing ToDo item with ID {}.", id);
            return false;
        }
    }
}
