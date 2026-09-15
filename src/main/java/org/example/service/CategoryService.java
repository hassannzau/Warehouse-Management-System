package org.example.service;

import org.example.request.AddCategoryRequest;
import org.example.response.CategoryResponse;

import java.util.List;

public interface CategoryService {

    List<CategoryResponse> listCategories();

    CategoryResponse addCategory(AddCategoryRequest request);
}
