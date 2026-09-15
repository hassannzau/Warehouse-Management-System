package org.example.service.impl;

import org.example.entity.Category;
import org.example.repository.CategoryRepository;
import org.example.request.AddCategoryRequest;
import org.example.response.CategoryResponse;
import org.example.service.CategoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryServiceImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> listCategories() {
        return categoryRepository.findAllByOrderByNameAsc().stream().map(CategoryResponse::from).toList();
    }

    @Override
    @Transactional
    public CategoryResponse addCategory(AddCategoryRequest request) {
        Category category = categoryRepository.save(new Category(request.getName()));
        return CategoryResponse.from(category);
    }
}
