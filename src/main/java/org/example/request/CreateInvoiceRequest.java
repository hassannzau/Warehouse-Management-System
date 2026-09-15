package org.example.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateInvoiceRequest {

    // The store the sale belongs to
    private Long storeId;

    // Map of productId to quantity to sell.
    private Map<Long, Integer> lineItems;
}
