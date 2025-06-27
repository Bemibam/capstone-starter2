package org.yearup.data.mysql;

import org.springframework.stereotype.Component;
import org.yearup.data.OrderDao;
import org.yearup.models.Order;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class MySqlOrderDao extends MySqlDaoBase implements OrderDao
{
    private final DataSource dataSource;

    public MySqlOrderDao(DataSource dataSource)
    {
        super(dataSource);
        this.dataSource = dataSource;
    }

    @Override
    public Order create(Order order)
    {
        String sql = "INSERT INTO orders (user_id, date, address, city, state, zip, shipping_amount) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS))
        {
            ps.setInt(1, order.getUserId());
            ps.setTimestamp(2, Timestamp.valueOf(order.getDate()));
            ps.setString(3, order.getAddress());
            ps.setString(4, order.getCity());
            ps.setString(5, order.getState());
            ps.setString(6, order.getZip());
            ps.setBigDecimal(7, order.getShippingAmount());

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys())
            {
                if (keys.next())
                {
                    order.setOrderId(keys.getInt(1));
                }
            }

            return order;
        }
        catch (SQLException ex)
        {
            throw new RuntimeException("Error creating order", ex);
        }
    }

    @Override
    public List<Order> getByUserId(int userId)
    {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM orders WHERE user_id = ? ORDER BY date DESC";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql))
        {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery())
            {
                while (rs.next())
                {
                    orders.add(mapRow(rs));
                }
            }
        }
        catch (SQLException ex)
        {
            throw new RuntimeException("Error getting orders by user ID", ex);
        }

        return orders;
    }

    @Override
    public Order getById(int orderId)
    {
        String sql = "SELECT * FROM orders WHERE order_id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql))
        {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery())
            {
                return rs.next() ? mapRow(rs) : null;
            }
        }
        catch (SQLException ex)
        {
            throw new RuntimeException("Error getting order by ID", ex);
        }
    }

    private Order mapRow(ResultSet rs) throws SQLException
    {
        Order order = new Order();
        order.setOrderId(rs.getInt("order_id"));
        order.setUserId(rs.getInt("user_id"));
        order.setDate(rs.getTimestamp("date").toLocalDateTime());
        order.setAddress(rs.getString("address"));
        order.setCity(rs.getString("city"));
        order.setState(rs.getString("state"));
        order.setZip(rs.getString("zip"));
        order.setShippingAmount(rs.getBigDecimal("shipping_amount"));
        return order;
    }
} 