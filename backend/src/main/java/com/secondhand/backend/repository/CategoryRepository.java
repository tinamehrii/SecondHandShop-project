package com.secondhand.backend.repository;

import com.secondhand.backend.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByParentIsNull();

    List<Category> findByParentId(Long parentId);

    boolean existsByNameAndParentId(String name, Long parentId);
}
