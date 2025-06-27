package org.yearup.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.yearup.data.OrderDao;
import org.yearup.data.OrderLineItemDao;
import org.yearup.data.ProfileDao;
import org.yearup.data.ShoppingCartDao;
import org.yearup.data.UserDao;
import org.yearup.models.Order;
import org.yearup.models.OrderLineItem;
import org.yearup.models.Profile;
import org.yearup.models.ShoppingCart;
import org.yearup.models.User;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/orders")
@CrossOrigin
@PreAuthorize("isAuthenticated()")
public class OrdersController
{
    private final OrderDao orderDao;
    private final OrderLineItemDao orderLineItemDao;
    private final ShoppingCartDao shoppingCartDao;
    private final UserDao userDao;
    private final ProfileDao profileDao;

    @Autowired
    public OrdersController(OrderDao orderDao, OrderLineItemDao orderLineItemDao, 
                          ShoppingCartDao shoppingCartDao, UserDao userDao, ProfileDao profileDao)
    {
        this.orderDao = orderDao;
        this.orderLineItemDao = orderLineItemDao;
        this.shoppingCartDao = shoppingCartDao;
        this.userDao = userDao;
        this.profileDao = profileDao;
    }

    @PostMapping
    public Order checkout(Principal principal)
    {
        try
        {
            // Get current user
            String userName = principal.getName();
            User user = userDao.getByUserName(userName);
            int userId = user.getId();

            // Get user's profile for shipping information
            Profile profile = profileDao.getByUserId(userId);
            if (profile == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Profile not found. Please complete your profile before checkout.");
            }

            // Get current shopping cart
            ShoppingCart cart = shoppingCartDao.getByUserId(userId);
            if (cart.getItems().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Shopping cart is empty. Cannot create order.");
            }

            // Create the order
            Order order = new Order();
            order.setUserId(userId);
            order.setDate(LocalDateTime.now());
            order.setAddress(profile.getAddress());
            order.setCity(profile.getCity());
            order.setState(profile.getState());
            order.setZip(profile.getZip());
            order.setShippingAmount(BigDecimal.ZERO); // Free shipping for now

            // Save the order to get the generated ID
            order = orderDao.create(order);

            // Convert cart items to order line items
            List<OrderLineItem> lineItems = new ArrayList<>();
            for (var cartItem : cart.getItems().values()) {
                OrderLineItem lineItem = new OrderLineItem();
                lineItem.setOrderId(order.getOrderId());
                lineItem.setProductId(cartItem.getProductId());
                lineItem.setSalesPrice(cartItem.getProduct().getPrice());
                lineItem.setQuantity(cartItem.getQuantity());
                lineItem.setDiscount(cartItem.getDiscountPercent());
                lineItems.add(orderLineItemDao.create(lineItem));
            }

            order.setLineItems(lineItems);

            // Clear the shopping cart
            shoppingCartDao.clearCart(userId);

            return order;
        }
        catch (ResponseStatusException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error during checkout: " + e.getMessage());
        }
    }

    @GetMapping
    public List<Order> getOrders(Principal principal)
    {
        try
        {
            String userName = principal.getName();
            User user = userDao.getByUserName(userName);
            int userId = user.getId();

            List<Order> orders = orderDao.getByUserId(userId);
            
            // Load line items for each order
            for (Order order : orders) {
                List<OrderLineItem> lineItems = orderLineItemDao.getByOrderId(order.getOrderId());
                order.setLineItems(lineItems);
            }

            return orders;
        }
        catch (Exception e)
        {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error retrieving orders");
        }
    }

    @GetMapping("/{orderId}")
    public Order getOrderById(Principal principal, @PathVariable int orderId)
    {
        try
        {
            String userName = principal.getName();
            User user = userDao.getByUserName(userName);
            int userId = user.getId();

            Order order = orderDao.getById(orderId);
            if (order == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found");
            }

            // Ensure the order belongs to the current user
            if (order.getUserId() != userId) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
            }

            // Load line items
            List<OrderLineItem> lineItems = orderLineItemDao.getByOrderId(orderId);
            order.setLineItems(lineItems);

            return order;
        }
        catch (ResponseStatusException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error retrieving order");
        }
    }
} 