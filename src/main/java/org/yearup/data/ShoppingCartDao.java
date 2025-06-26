package org.yearup.data;

import org.yearup.models.ShoppingCart;

public interface ShoppingCartDao
{
    ShoppingCart getByUserId(int userId);
    void addItem(int userId, int productId);
    void updateItemQuantity(int userId, int productId, int quantity);
    void clearCart(int userId);
    // add additional method signatures here
}
