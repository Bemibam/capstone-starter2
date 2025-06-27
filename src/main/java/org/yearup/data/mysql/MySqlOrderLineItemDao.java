package org.yearup.data.mysql;

import org.springframework.stereotype.Component;
import org.yearup.data.OrderLineItemDao;
import org.yearup.data.ProductDao;
import org.yearup.models.OrderLineItem;
import org.yearup.models.Product;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Component
public class MySqlOrderLineItemDao extends MySqlDaoBase implements OrderLineItemDao
{
    private final DataSource dataSource;
    private final ProductDao productDao;

    public MySqlOrderLineItemDao(DataSource dataSource, ProductDao productDao)
    {
        super(dataSource);
        this.dataSource = dataSource;
        this.productDao = productDao;
    }

    @Override
    public OrderLineItem create(OrderLineItem orderLineItem)
    {
        String sql = "INSERT INTO order_line_items (order_id, product_id, sales_price, quantity, discount) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS))
        {
            ps.setInt(1, orderLineItem.getOrderId());
            ps.setInt(2, orderLineItem.getProductId());
            ps.setBigDecimal(3, orderLineItem.getSalesPrice());
            ps.setInt(4, orderLineItem.getQuantity());
            ps.setBigDecimal(5, orderLineItem.getDiscount());

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys())
            {
                if (keys.next())
                {
                    orderLineItem.setOrderLineItemId(keys.getInt(1));
                }
            }

            return orderLineItem;
        }
        catch (SQLException ex)
        {
            throw new RuntimeException("Error creating order line item", ex);
        }
    }

    @Override
    public List<OrderLineItem> getByOrderId(int orderId)
    {
        List<OrderLineItem> lineItems = new ArrayList<>();
        String sql = "SELECT * FROM order_line_items WHERE order_id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql))
        {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery())
            {
                while (rs.next())
                {
                    OrderLineItem item = mapRow(rs);
                    // Load the associated product
                    Product product = productDao.getById(item.getProductId());
                    item.setProduct(product);
                    lineItems.add(item);
                }
            }
        }
        catch (SQLException ex)
        {
            throw new RuntimeException("Error getting order line items by order ID", ex);
        }

        return lineItems;
    }

    private OrderLineItem mapRow(ResultSet rs) throws SQLException
    {
        OrderLineItem item = new OrderLineItem();
        item.setOrderLineItemId(rs.getInt("order_line_item_id"));
        item.setOrderId(rs.getInt("order_id"));
        item.setProductId(rs.getInt("product_id"));
        item.setSalesPrice(rs.getBigDecimal("sales_price"));
        item.setQuantity(rs.getInt("quantity"));
        item.setDiscount(rs.getBigDecimal("discount"));
        return item;
    }
} 