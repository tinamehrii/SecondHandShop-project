package com.secondhand.backend.dto;

/**
 * Body used by admin for creating or editing a category.
 */
public class CategoryRequest {
    public String name;
    public Long parentId; // null means root category
}
