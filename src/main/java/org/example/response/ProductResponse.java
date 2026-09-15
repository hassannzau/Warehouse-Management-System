package org.example.response;

import org.example.entity.Product;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {
    private Long id;
    private String sku;
    private String name;
    private Long categoryId;
    private String categoryName;
    private Long storeId;
    private BigDecimal unitPrice;
    private int quantityOnHand;
    private int reorderThreshold;
    private boolean lowStock;

    public static ProductResponse from(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getSku(),
                product.getName(),
                product.getCategory() != null ? product.getCategory().getId() : null,
                product.getCategory() != null ? product.getCategory().getName() : null,
                product.getStore().getId(),
                product.getUnitPrice(),
                product.getQuantityOnHand(),
                product.getReorderThreshold(),
                product.isLowStock());
    }

    // JavaFX's ComboBox/ListView render each item via toString() unless a custom
    // cell factory is set - without this override, the "Select product" dropdown
    // on the Billing screen showed "org.example.response.ProductResponse@<hash>"
    // instead of the product name (same bug as CategoryResponse had).
    @Override
    public String toString() {
        return name;
    }
}
