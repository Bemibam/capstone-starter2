# 🚀 EasyShop — My Capstone 3 Project

**A Spring Boot + MySQL back end powering a lightweight e-commerce front end**  
Features full JWT auth, category & product management, persistent shopping cart, and per-user profiles.

---

## 🛠️ Tech Stack

- **Java 17 & Spring Boot**  
- **Spring Security** with JWT  
- **MySQL** (seeded via create_database.sql
- **Postman** for API testing  

---

## ✨ Key Features

- 🔐 **Authentication & Authorization**  
  - POST /register → create USER or ADMIN  
  - POST /login → receive Bearer <token> 

- 🗂️ **Categories** (ADMIN can add/edit/delete)  
  - GET  /categories  
  - GET  /categories/{id} 
  - POST /categories 
  - PUT  /categories/{id} 
  - DELETE /categories/{id} 

- 🛍️ **Products** (ADMIN can add/edit/delete)  
  - GET  /products with filters:  
    - ?cat=1 
    - ?minPrice=25&maxPrice=100  
    - ?color=Red  
  - GET  /products/{id}
  - POST /products 
  - PUT  /products/{id}
  - DELETE /products/{id}

- 🛒 **Shopping Cart** (USER only)  
  - GET    /cart 
  - POST   /cart/products/{productId} 
  - PUT    /cart/products/{productId} → { "quantity": 3 } 
  - DELETE /cart  

- 👤 **User Profile** (USER only) 



---
Interesting Code

@Override
public List<Product> search(Integer categoryId,
                            BigDecimal minPrice,
                            BigDecimal maxPrice,
                            String color) {
    // Prepare a list to collect matching products
    List<Product> products = new ArrayList<>();

    //    - If categoryId is null, set to -1 so the SQL OR clause skips it
    //    - If minPrice/maxPrice is null, set to -1 so the SQL OR clause skips it
    //    - If color is null, set to empty string so we don’t filter by color
    int cat        = (categoryId == null)? -1                 : categoryId;
    BigDecimal min = (minPrice   == null) ? BigDecimal.valueOf(-1) : minPrice;
    BigDecimal max = (maxPrice   == null) ? BigDecimal.valueOf(-1) : maxPrice;
    String col     = (color      == null) ? ""color                  : color;

  
    String sql =
        "SELECT *                                                       " +
        "  FROM products                                                " +
        " WHERE (category_id = ?    OR ?    = -1)                       " +
        "   AND (price       >= ?    OR ?    = -1)                       " +
        "   AND (price       <= ?    OR ?    = -1)                       " +
        "   AND (color       = ?    OR ?    = '')                       ";

    try (Connection conn = getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

        
        //    first to apply the filter, second to check if we should ignore it
        stmt.setInt       (1, cat);
        stmt.setInt       (2, cat);
        stmt.setBigDecimal(3, min);
        stmt.setBigDecimal(4, min);
        stmt.setBigDecimal(5, max);
        stmt.setBigDecimal(6, max);
        stmt.setString    (7, col);
        stmt.setString    (8, col);

        //  Execute the query and map each row to a Product object
        try (ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                products.add(mapRow(rs));
            }
        }
    }
    catch (SQLException e) {
        // If something goes wrong, wrap it in a runtime exception
        throw new RuntimeException("Error executing product search", e);
    }

    // Return all products that matched our filters
    return products;
}

Why this is cool:

Single query handles every combination of category, price range, and color filters.

No dynamic SQL building—just simple “ignore” sentinels (-1 or empty string).

Clean, easy to read, and safe from SQL.

<img width="1072" alt="Screenshot 2025-06-26 at 10 42 32 PM" src="https://github.com/user-attachments/assets/0e86f854-e86b-4b77-97c6-4e944eea760e" />
![image](https://github.com/user-attachments/assets/dea92888-93e3-40ea-9e5a-066c717bad4f)
![image](https://github.com/user-attachments/assets/bedc24f4-bcb9-4ea9-bd17-ec3fde4fd248)
![image](https://github.com/user-attachments/assets/44bcc386-a976-4c9c-b988-b0893ed222ca)





