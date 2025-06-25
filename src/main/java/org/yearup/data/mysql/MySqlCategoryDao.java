package org.yearup.data.mysql;

import org.springframework.stereotype.Component;
import org.yearup.data.CategoryDao;
import org.yearup.models.Category;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Component
public class MySqlCategoryDao extends MySqlDaoBase implements CategoryDao
{
    // store the DataSource yourself so you can call getConnection()
    private final DataSource dataSource;

    public MySqlCategoryDao(DataSource dataSource)
    {
        super(dataSource);
        this.dataSource = dataSource;
    }

    @Override
    public List<Category> getAllCategories()
    {
        String sql = "SELECT category_id, name, description FROM categories";
        List<Category> results = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery())
        {
            while (rs.next())
            {
                results.add(mapRow(rs));
            }
        }
        catch (SQLException ex)
        {
            throw new RuntimeException("Error querying all categories", ex);
        }

        return results;
    }

    @Override
    public Category getById(int categoryId)
    {
        String sql = "SELECT category_id, name, description FROM categories WHERE category_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql))
        {
            ps.setInt(1, categoryId);
            try (ResultSet rs = ps.executeQuery())
            {
                return rs.next() ? mapRow(rs) : null;
            }
        }
        catch (SQLException ex)
        {
            throw new RuntimeException("Error querying category by id", ex);
        }
    }

    @Override
    public Category create(Category category)
    {
        String sql = "INSERT INTO categories (name, description) VALUES (?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS))
        {
            ps.setString(1, category.getName());
            ps.setString(2, category.getDescription());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys())
            {
                if (keys.next())
                {
                    category.setCategoryId(keys.getInt(1));
                }
            }

            return category;
        }
        catch (SQLException ex)
        {
            throw new RuntimeException("Error creating category", ex);
        }
    }

    @Override
    public void update(int categoryId, Category category)
    {
        String sql = "UPDATE categories SET name = ?, description = ? WHERE category_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql))
        {
            ps.setString(1, category.getName());
            ps.setString(2, category.getDescription());
            ps.setInt(3, categoryId);
            ps.executeUpdate();
        }
        catch (SQLException ex)
        {
            throw new RuntimeException("Error updating category", ex);
        }
    }

    @Override
    public void delete(int categoryId)
    {
        String sql = "DELETE FROM categories WHERE category_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql))
        {
            ps.setInt(1, categoryId);
            ps.executeUpdate();
        }
        catch (SQLException ex)
        {
            throw new RuntimeException("Error deleting category", ex);
        }
    }

    // helper to map a single row into a Category
    private Category mapRow(ResultSet row) throws SQLException
    {
        Category c = new Category();
        c.setCategoryId(row.getInt("category_id"));
        c.setName(row.getString("name"));
        c.setDescription(row.getString("description"));
        return c;
    }
}
