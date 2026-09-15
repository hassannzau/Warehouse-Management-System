package org.example.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProductRequest {
    private Long id;
    private String sku;
    private String name;
    private Long categoryId;
    private Long storeId;
    private BigDecimal unitPrice;
    private int quantityOnHand;
    private int reorderThreshold;
}
