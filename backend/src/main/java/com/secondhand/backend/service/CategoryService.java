package com.secondhand.backend.service;

import com.secondhand.backend.dto.CategoryRequest;
import com.secondhand.backend.exception.ApiException;
import com.secondhand.backend.model.Category;
import com.secondhand.backend.repository.AdvertisementRepository;
import com.secondhand.backend.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Reading the list of categories (for everyone)
 * and managing them (only for admin).
 */
@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final AdvertisementRepository adRepository;

    public CategoryService(CategoryRepository categoryRepository,
                           AdvertisementRepository adRepository) {
        this.categoryRepository = categoryRepository;
        this.adRepository = adRepository;
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public Category createCategory(CategoryRequest request) {
        if (request.name == null || request.name.trim().isEmpty()) {
            throw new ApiException(400, "نام دسته‌بندی نمی‌تواند خالی باشد");
        }
        Category parent = null;
        if (request.parentId != null) {
            parent = categoryRepository.findById(request.parentId)
                    .orElseThrow(() -> new ApiException(400, "دسته‌بندی مادر پیدا نشد"));
            if (parent.getParent() != null) {
                // we keep the tree simple: only two levels are allowed
                throw new ApiException(400, "فقط دو سطح دسته‌بندی پشتیبانی می‌شود");
            }
        }
        if (categoryRepository.existsByNameAndParentId(request.name.trim(),
                parent == null ? null : parent.getId())) {
            throw new ApiException(400, "دسته‌بندی با این نام قبلا ساخته شده است");
        }
        return categoryRepository.save(new Category(request.name.trim(), parent));
    }

    public Category updateCategory(Long id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ApiException(404, "دسته‌بندی پیدا نشد"));
        if (request.name == null || request.name.trim().isEmpty()) {
            throw new ApiException(400, "نام دسته‌بندی نمی‌تواند خالی باشد");
        }
        category.setName(request.name.trim());
        return categoryRepository.save(category);
    }

    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ApiException(404, "دسته‌بندی پیدا نشد"));
        if (!categoryRepository.findByParentId(id).isEmpty()) {
            throw new ApiException(400, "ابتدا زیردسته‌های این دسته‌بندی را حذف کنید");
        }
        if (adRepository.existsByCategoryId(id)) {
            throw new ApiException(400, "برای این دسته‌بندی آگهی ثبت شده و قابل حذف نیست");
        }
        categoryRepository.delete(category);
    }
}
