package org.yearup.data.mysql;

import org.springframework.stereotype.Component;
import org.yearup.data.ProfileDao;
import org.yearup.models.Profile;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class MySqlProfileDao extends MySqlDaoBase implements ProfileDao
{
    public MySqlProfileDao(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public Profile create(Profile profile) {
        String sql = """
            INSERT INTO profiles
               (user_id, first_name, last_name, phone, email, address, city, state, zip)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql))
        {
            ps.setInt   (1, profile.getUserId());
            ps.setString(2, profile.getFirstName());
            ps.setString(3, profile.getLastName());
            ps.setString(4, profile.getPhone());
            ps.setString(5, profile.getEmail());
            ps.setString(6, profile.getAddress());
            ps.setString(7, profile.getCity());
            ps.setString(8, profile.getState());
            ps.setString(9, profile.getZip());
            ps.executeUpdate();

            // return the newly inserted row
            return getByUserId(profile.getUserId());
        }
        catch (SQLException e) {
            throw new RuntimeException("Error creating profile", e);
        }
    }

    @Override
    public Profile getByUserId(int userId) {
        String sql = "SELECT * FROM profiles WHERE user_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql))
        {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
            }
            return null;
        }
        catch (SQLException e) {
            throw new RuntimeException("Error fetching profile", e);
        }
    }

    @Override
    public Profile update(int userId, Profile profile) {
        String sql = """
            UPDATE profiles SET
                first_name = ?, 
                last_name  = ?, 
                phone      = ?,
                email      = ?,
                address    = ?, 
                city       = ?, 
                state      = ?, 
                zip        = ?
            WHERE user_id = ?
            """;

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql))
        {
            ps.setString(1, profile.getFirstName());
            ps.setString(2, profile.getLastName());
            ps.setString(3, profile.getPhone());
            ps.setString(4, profile.getEmail());
            ps.setString(5, profile.getAddress());
            ps.setString(6, profile.getCity());
            ps.setString(7, profile.getState());
            ps.setString(8, profile.getZip());
            ps.setInt   (9, userId);
            ps.executeUpdate();

            // return the updated row
            return getByUserId(userId);
        }
        catch (SQLException e) {
            throw new RuntimeException("Error updating profile", e);
        }
    }

    // helper to map a ResultSet to your Profile model
    private Profile mapRow(ResultSet rs) throws SQLException {
        Profile p = new Profile();
        p.setUserId   (rs.getInt   ("user_id"));
        p.setFirstName(rs.getString("first_name"));
        p.setLastName (rs.getString("last_name"));
        p.setAddress  (rs.getString("address"));
        p.setCity     (rs.getString("city"));
        p.setState    (rs.getString("state"));
        p.setZip      (rs.getString("zip"));
        p.setPhone    (rs.getString("phone"));
        p.setEmail    (rs.getString("email"));
        return p;
    }
}
