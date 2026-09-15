package org.example.enums;

/*
 * ADMIN manages users/categories/products and can delete products; MANAGER can
 * create/update products and categories; STAFF handles stock movements and sales
 * but cannot change the catalog.
 */
public enum Role {
    ADMIN,
    MANAGER,
    STAFF
}
