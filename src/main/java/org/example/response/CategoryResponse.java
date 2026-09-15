package org.example.response;

import org.example.entity.Category;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponse {
    private Long id;
    private String name;

    public static CategoryResponse from(Category category) {
        return new CategoryResponse(category.getId(), category.getName());
    }

    // JavaFX's ComboBox/ListView render each item via toString() unless a custom
    // cell factory is set - without this override, the category dropdown in
    // ProductsController showed "org.example.response.CategoryResponse@<hash>"
    // instead of the category name.
    @Override
    public String toString() {
        return name;
    }
}
