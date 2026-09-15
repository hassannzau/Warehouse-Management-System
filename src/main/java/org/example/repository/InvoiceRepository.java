package org.example.repository;

import org.example.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    List<Invoice> findByStoreIdAndCreatedAtGreaterThanEqualAndCreatedAtLessThanOrderByCreatedAtAsc(
            Long storeId, LocalDateTime start, LocalDateTime end);

    @Query("select distinct i from Invoice i left join fetch i.items it left join fetch it.product " +
            "where i.store.id = :storeId and i.createdAt >= :start and i.createdAt < :end")
    List<Invoice> findWithItemsByStoreAndDateRange(@Param("storeId") Long storeId,
                                                    @Param("start") LocalDateTime start,
                                                    @Param("end") LocalDateTime end);
}
