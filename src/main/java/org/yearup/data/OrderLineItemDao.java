package org.yearup.data;

import org.yearup.models.OrderLineItem;
import java.util.List;

public interface OrderLineItemDao
{
    /**
     * Create a new order line item and return it with the generated ID
     */
    OrderLineItem create(OrderLineItem orderLineItem);

    /**
     * Get all line items for a specific order
     */
    List<OrderLineItem> getByOrderId(int orderId);
} 