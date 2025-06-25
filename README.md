# 🚀 EasyShop — My Capstone 3 Project

> A Spring Boot + MySQL back-end powering a simple e-commerce site  
> Featuring JWT auth, flexible product search, admin tools, persistent carts & user profiles.

---

## 🛠️ Tech Stack

- **Java 17 & Spring Boot**  
- **Spring Security** with JWT  
- **MySQL** (seeded via `create_database.sql`)  
- **Maven** build  
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

