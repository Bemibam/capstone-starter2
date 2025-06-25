package org.yearup.data;

import org.yearup.models.Product;
import java.math.BigDecimal;
import java.util.List;

public interface ProductDao
{
    /**
     * Search products by optional filters.
     * @param categoryId only products in this category (null = ignore)
     * @param minPrice   only products priced ≥ this (null = ignore)
     * @param maxPrice   only products priced ≤ this (null = ignore)
     * @param color      only products with this color (null = ignore)
     * @return list of matching products
     */
    List<Product> search(Integer categoryId,
                         BigDecimal minPrice,
                         BigDecimal maxPrice,
                         String color);

    /**
     * List all products in a single category.
     */
    List<Product> listByCategoryId(int categoryId);

    /**
     * Lookup one product by its ID.
     */
    Product getById(int productId);

    /**
     * Insert a new product and return it (with its generated ID).
     */
    Product create(Product product);

    /**
     * Update the given productId with the values in the provided product.
     */
    void update(int productId, Product product);

    /**
     * Delete the product with the given ID.
     */
    void delete(int productId);
}
