package org.yearup.data.mysql;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.yearup.models.Product;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MySqlProductDaoTest extends BaseDaoTestClass
{
    private MySqlProductDao dao;

    @BeforeEach
    public void setup()
    {
        dao = new MySqlProductDao(dataSource);
    }

    @Test
    public void getById_shouldReturn_theCorrectProduct()
    {
        // arrange
        int productId = 1;
        Product expected = new Product()
        {{
            setProductId(1);
            setName("Smartphone");
            setPrice(new BigDecimal("499.99"));
            setCategoryId(1);
            setDescription("A powerful and feature-rich smartphone for all your communication needs.");
            setColor("Black");
            setStock(50);
            setFeatured(false);
            setImageUrl("smartphone.jpg");
        }};

        // act
        var actual = dao.getById(productId);

        // assert
        assertEquals(expected.getPrice(), actual.getPrice(), "Because I tried to get product 1 from the database.");
    }

    @Test
    public void search_withColorFilter_shouldReturnOnlyMatchingProducts()
    {
        // arrange
        String color = "Black";

        // act
        List<Product> results = dao.search(null, null, null, color);

        // assert
        assertTrue(results.size() > 0, "Should return at least one product with color Black");
        for (Product product : results) {
            assertEquals(color, product.getColor(), "All returned products should have color " + color);
        }
    }

    @Test
    public void search_withoutColorFilter_shouldReturnAllProducts()
    {
        // arrange
        // act
        List<Product> results = dao.search(null, null, null, null);

        // assert
        assertTrue(results.size() > 0, "Should return all products when no color filter is applied");
    }

    @Test
    public void search_withCategoryFilter_shouldReturnOnlyMatchingProducts()
    {
        // arrange
        int categoryId = 1; // Electronics

        // act
        List<Product> results = dao.search(categoryId, null, null, null);

        // assert
        assertTrue(results.size() > 0, "Should return at least one product in category " + categoryId);
        for (Product product : results) {
            assertEquals(categoryId, product.getCategoryId(), "All returned products should be in category " + categoryId);
        }
    }

    @Test
    public void search_withPriceRange_shouldReturnOnlyMatchingProducts()
    {
        // arrange
        BigDecimal minPrice = new BigDecimal("100");
        BigDecimal maxPrice = new BigDecimal("500");

        // act
        List<Product> results = dao.search(null, minPrice, maxPrice, null);

        // assert
        assertTrue(results.size() > 0, "Should return at least one product in the price range");
        for (Product product : results) {
            assertTrue(product.getPrice().compareTo(minPrice) >= 0, "Product price should be >= minPrice");
            assertTrue(product.getPrice().compareTo(maxPrice) <= 0, "Product price should be <= maxPrice");
        }
    }
}