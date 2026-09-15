package org.example.repository;

import org.example.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findAllByOrderByNameAsc();

    @Query("select p from Product p where p.quantityOnHand <= p.reorderThreshold order by p.name")
    List<Product> findLowStockOrderByNameAsc();

    boolean existsBySku(String sku);
}
