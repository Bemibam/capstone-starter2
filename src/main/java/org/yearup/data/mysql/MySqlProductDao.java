package org.yearup.data.mysql;

import org.springframework.stereotype.Component;
import org.yearup.models.Product;
import org.yearup.data.ProductDao;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Component
public class MySqlProductDao extends MySqlDaoBase implements ProductDao
{
    public MySqlProductDao(DataSource dataSource)
    {
        super(dataSource);
    }

    /**
     *  Interesting Code: Single-query search with optional filters
     * We use “sentinel” values so that null means “ignore this filter.”
     */
    @Override
    public List<Product> search(Integer categoryId,
                                BigDecimal minPrice,
                                BigDecimal maxPrice,
                                String color)
    {
        List<Product> products = new ArrayList<>();

        // 1) Normalize nulls to “ignore” values:
        int cat        = (categoryId == null) ? -1                    : categoryId;
        BigDecimal min = (minPrice   == null) ? BigDecimal.valueOf(-1) : minPrice;
        BigDecimal max = (maxPrice   == null) ? BigDecimal.valueOf(-1) : maxPrice;
        String col     = (color       == null) ? ""                   : color;

        // 2) One fixed SQL covers all filter combos.
        //    OR ? = -1 / OR ? = '' makes the clause always true when sentinel is passed.
        String sql =
                "SELECT *                                                       " +
                        "  FROM products                                                " +
                        " WHERE (category_id = ?    OR ?    = -1)                       " +  // category filter
                        "   AND (price       >= ?    OR ?    = -1)                       " +  // minPrice filter
                        "   AND (price       <= ?    OR ?    = -1)                       " +  // maxPrice filter
                        "   AND (color       = ?    OR ?    = '')                       ";   // color filter

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql))
        {
            // 3) Bind each param twice: once to apply, once to skip if sentinel
            stmt.setInt       (1, cat);
            stmt.setInt       (2, cat);
            stmt.setBigDecimal(3, min);
            stmt.setBigDecimal(4, min);
            stmt.setBigDecimal(5, max);
            stmt.setBigDecimal(6, max);
            stmt.setString    (7, col);
            stmt.setString    (8, col);

            // 4) Execute and map results
            try (ResultSet rs = stmt.executeQuery())
            {
                while (rs.next())
                {
                    products.add(mapRow(rs));
                }
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException("Error executing product search", e);
        }

        return products;
    }

    /**
     * List all products in a given category.
     */
    @Override
    public List<Product> listByCategoryId(int categoryId)
    {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM products WHERE category_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql))
        {
            stmt.setInt(1, categoryId);
            try (ResultSet rs = stmt.executeQuery())
            {
                while (rs.next())
                {
                    products.add(mapRow(rs));
                }
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException("Error listing products by category", e);
        }

        return products;
    }

    /**
     * Fetch a single product by its ID.
     */
    @Override
    public Product getById(int productId)
    {
        String sql = "SELECT * FROM products WHERE product_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql))
        {
            stmt.setInt(1, productId);
            try (ResultSet rs = stmt.executeQuery())
            {
                if (rs.next())
                {
                    return mapRow(rs);
                }
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException("Error fetching product by ID", e);
        }
        return null;
    }

    /**
     * Insert a new product and return it (with generated ID).
     */
    @Override
    public Product create(Product product)
    {
        String sql =
                "INSERT INTO products(name, price, category_id, description, color, image_url, stock, featured) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS))
        {
            stmt.setString   (1, product.getName());
            stmt.setBigDecimal(2, product.getPrice());
            stmt.setInt      (3, product.getCategoryId());
            stmt.setString   (4, product.getDescription());
            stmt.setString   (5, product.getColor());
            stmt.setString   (6, product.getImageUrl());
            stmt.setInt      (7, product.getStock());
            stmt.setBoolean  (8, product.isFeatured());

            int rows = stmt.executeUpdate();
            if (rows > 0)
            {
                try (ResultSet keys = stmt.getGeneratedKeys())
                {
                    if (keys.next())
                    {
                        return getById(keys.getInt(1));
                    }
                }
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException("Error creating product", e);
        }
        return null;
    }

    /**
     * Update an existing product.
     */
    @Override
    public void update(int productId, Product product)
    {
        String sql =
                "UPDATE products SET " +
                        "   name        = ?, " +
                        "   price       = ?, " +
                        "   category_id = ?, " +
                        "   description = ?, " +
                        "   color       = ?, " +
                        "   image_url   = ?, " +
                        "   stock       = ?, " +
                        "   featured    = ?  " +
                        "WHERE product_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql))
        {
            stmt.setString   (1, product.getName());
            stmt.setBigDecimal(2, product.getPrice());
            stmt.setInt      (3, product.getCategoryId());
            stmt.setString   (4, product.getDescription());
            stmt.setString   (5, product.getColor());
            stmt.setString   (6, product.getImageUrl());
            stmt.setInt      (7, product.getStock());
            stmt.setBoolean  (8, product.isFeatured());
            stmt.setInt      (9, productId);

            stmt.executeUpdate();
        }
        catch (SQLException e)
        {
            throw new RuntimeException("Error updating product", e);
        }
    }

    /**
     * Delete a product by its ID.
     */
    @Override
    public void delete(int productId)
    {
        String sql = "DELETE FROM products WHERE product_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql))
        {
            stmt.setInt(1, productId);
            stmt.executeUpdate();
        }
        catch (SQLException e)
        {
            throw new RuntimeException("Error deleting product", e);
        }
    }

    /**
     * Map a ResultSet row into a Product object.
     */
    protected static Product mapRow(ResultSet row) throws SQLException
    {
        return new Product(
                row.getInt       ("product_id"),
                row.getString    ("name"),
                row.getBigDecimal("price"),
                row.getInt       ("category_id"),
                row.getString    ("description"),
                row.getString    ("color"),
                row.getInt       ("stock"),
                row.getBoolean   ("featured"),
                row.getString    ("image_url")
        );
    }
}