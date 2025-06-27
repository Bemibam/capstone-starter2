package org.yearup.data;

import org.yearup.models.Order;
import java.util.List;

public interface OrderDao
{
    /**
     * Create a new order and return it with the generated ID
     */
    Order create(Order order);

    /**
     * Get all orders for a specific user
     */
    List<Order> getByUserId(int userId);

    /**
     * Get a specific order by ID
     */
    Order getById(int orderId);
} 