package org.example.response;

import org.example.entity.StockTransaction;
import org.example.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StockTransactionResponse {
    private Long id;
    private Long productId;
    private String productName;
    private TransactionType type;
    private int quantity;
    private BigDecimal unitPrice;
    private String note;
    private LocalDateTime createdAt;

    public static StockTransactionResponse from(StockTransaction transaction) {
        return new StockTransactionResponse(
                transaction.getId(),
                transaction.getProduct().getId(),
                transaction.getProduct().getName(),
                transaction.getType(),
                transaction.getQuantity(),
                transaction.getUnitPrice(),
                transaction.getNote(),
                transaction.getCreatedAt());
    }
}
