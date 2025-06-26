package org.yearup.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.yearup.data.ProductDao;
import org.yearup.models.Product;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/products")
@CrossOrigin
public class ProductsController
{
    private final ProductDao productDao;

    @Autowired
    public ProductsController(ProductDao productDao) {
        this.productDao = productDao;
    }

    // GET /products?cat=&minPrice=&maxPrice=&color=
    @GetMapping
    @PreAuthorize("permitAll()")
    public List<Product> search(
            @RequestParam(name="cat", required=false)      Integer categoryId,
            @RequestParam(name="minPrice", required=false) BigDecimal minPrice,
            @RequestParam(name="maxPrice", required=false) BigDecimal maxPrice,
            @RequestParam(name="color", required=false)    String color
    ) {
        // normalize nulls exactly as your DAO expects
        categoryId = (categoryId == null) ? -1 : categoryId;
        minPrice   = (minPrice   == null) ? new BigDecimal("-1") : minPrice;
        maxPrice   = (maxPrice   == null) ? new BigDecimal("-1") : maxPrice;
        // Don't normalize color - let it be null if not provided

        return productDao.search(categoryId, minPrice, maxPrice, color);
    }

    // GET /products/{id}
    @GetMapping("/{id}")
    @PreAuthorize("permitAll()")
    public Product getById(@PathVariable int id) {
        Product p = productDao.getById(id);
        if (p == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found");
        }
        return p;
    }

    // POST /products
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Product addProduct(@RequestBody Product product) {
        return productDao.create(product);
    }

    // PUT /products/{id}
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void updateProduct(@PathVariable int id, @RequestBody Product product) {
        productDao.update(id, product);
    }

    // DELETE /products/{id}
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteProduct(@PathVariable int id) {
        if (productDao.getById(id) == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found");
        }
        productDao.delete(id);
    }
}
