package org.yearup.data.mysql;

import org.springframework.stereotype.Component;
import org.yearup.data.ShoppingCartDao;
import org.yearup.data.ProductDao;
import org.yearup.models.ShoppingCart;
import org.yearup.models.ShoppingCartItem;
import org.yearup.models.Product;

import javax.sql.DataSource;
import java.sql.*;
import java.util.List;

@Component
public class MySqlShoppingCartDao extends MySqlDaoBase implements ShoppingCartDao
{
    private final DataSource dataSource;
    private final ProductDao productDao;

    public MySqlShoppingCartDao(DataSource dataSource, ProductDao productDao)
    {
        super(dataSource);
        this.dataSource = dataSource;
        this.productDao = productDao;
    }

    @Override
    public ShoppingCart getByUserId(int userId)
    {
        ShoppingCart cart = new ShoppingCart();
        String sql = "SELECT product_id, quantity FROM shopping_cart WHERE user_id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql))
        {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery())
            {
                while (rs.next())
                {
                    int productId = rs.getInt("product_id");
                    int quantity = rs.getInt("quantity");
                    
                    Product product = productDao.getById(productId);
                    if (product != null)
                    {
                        ShoppingCartItem item = new ShoppingCartItem();
                        item.setProduct(product);
                        item.setQuantity(quantity);
                        cart.add(item);
                    }
                }
            }
        }
        catch (SQLException ex)
        {
            throw new RuntimeException("Error getting shopping cart for user", ex);
        }

        return cart;
    }

    public void addItem(int userId, int productId)
    {
        // Check if item already exists in cart
        String checkSql = "SELECT quantity FROM shopping_cart WHERE user_id = ? AND product_id = ?";
        String insertSql = "INSERT INTO shopping_cart (user_id, product_id, quantity) VALUES (?, ?, 1)";
        String updateSql = "UPDATE shopping_cart SET quantity = quantity + 1 WHERE user_id = ? AND product_id = ?";

        try (Connection conn = dataSource.getConnection())
        {
            // Check if item exists
            try (PreparedStatement checkPs = conn.prepareStatement(checkSql))
            {
                checkPs.setInt(1, userId);
                checkPs.setInt(2, productId);
                try (ResultSet rs = checkPs.executeQuery())
                {
                    if (rs.next())
                    {
                        // Item exists, update quantity
                        try (PreparedStatement updatePs = conn.prepareStatement(updateSql))
                        {
                            updatePs.setInt(1, userId);
                            updatePs.setInt(2, productId);
                            updatePs.executeUpdate();
                        }
                    }
                    else
                    {
                        // Item doesn't exist, insert new item
                        try (PreparedStatement insertPs = conn.prepareStatement(insertSql))
                        {
                            insertPs.setInt(1, userId);
                            insertPs.setInt(2, productId);
                            insertPs.executeUpdate();
                        }
                    }
                }
            }
        }
        catch (SQLException ex)
        {
            throw new RuntimeException("Error adding item to shopping cart", ex);
        }
    }

    public void updateItemQuantity(int userId, int productId, int quantity)
    {
        String sql = "UPDATE shopping_cart SET quantity = ? WHERE user_id = ? AND product_id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql))
        {
            ps.setInt(1, quantity);
            ps.setInt(2, userId);
            ps.setInt(3, productId);
            ps.executeUpdate();
        }
        catch (SQLException ex)
        {
            throw new RuntimeException("Error updating shopping cart item", ex);
        }
    }

    public void clearCart(int userId)
    {
        String sql = "DELETE FROM shopping_cart WHERE user_id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql))
        {
            ps.setInt(1, userId);
            ps.executeUpdate();
        }
        catch (SQLException ex)
        {
            throw new RuntimeException("Error clearing shopping cart", ex);
        }
    }
} 