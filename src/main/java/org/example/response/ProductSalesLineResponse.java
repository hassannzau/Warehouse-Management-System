package org.example.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductSalesLineResponse {
    private Long productId;
    private String productName;
    private int quantitySold;
    private BigDecimal totalAmount;
}
