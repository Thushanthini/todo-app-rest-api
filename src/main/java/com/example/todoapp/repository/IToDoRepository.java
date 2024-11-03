package com.example.todoapp.repository;

import com.example.todoapp.model.ToDo;
import com.example.todoapp.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IToDoRepository extends JpaRepository<ToDo, Long> {
    List<ToDo> findByUser(User user);

    Page<ToDo> findByUser(User user, Pageable pageable);

    @Query("SELECT t FROM ToDo t WHERE t.user = :user AND " +
            "(LOWER(t.task) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(t.status) = LOWER(:status))")
    Page<ToDo> searchByKeywordAndStatus(@Param("user") User user,
                                        @Param("keyword") String keyword,
                                        @Param("status") String status,
                                        Pageable pageable);
}
