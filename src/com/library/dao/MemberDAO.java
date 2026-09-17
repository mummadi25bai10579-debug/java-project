package com.library.dao;

import com.library.interfaces.Searchable;
import com.library.model.Faculty;
import com.library.model.Member;
import com.library.model.Student;
import com.library.util.DBConnection;
import com.library.util.DateUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Member entities.
 * Dynamically instantiates polymorphic Student or Faculty subclasses based on stored member_type.
 */
public class MemberDAO implements Searchable<Member> {

    public boolean addMember(Member member) throws SQLException {
        String sql = "INSERT INTO members (name, email, member_type, max_books, registered_on) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, member.getName());
            pstmt.setString(2, member.getEmail());
            pstmt.setString(3, member.getMemberType().name());
            pstmt.setInt(4, member.getMaxBooksAllowed());
            pstmt.setString(5, DateUtil.format(member.getRegisteredOn()));

            int affected = pstmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        member.setMemberId(rs.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    public boolean updateMember(Member member) throws SQLException {
        String sql = "UPDATE members SET name = ?, email = ? WHERE member_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, member.getName());
            pstmt.setString(2, member.getEmail());
            pstmt.setInt(3, member.getMemberId());

            return pstmt.executeUpdate() > 0;
        }
    }

    public boolean deleteMember(int memberId) throws SQLException {
        String sql = "DELETE FROM members WHERE member_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, memberId);
            return pstmt.executeUpdate() > 0;
        }
    }

    public Member findById(int memberId) throws SQLException {
        String sql = "SELECT member_id, name, email, member_type, max_books, registered_on FROM members WHERE member_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, memberId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToMember(rs);
                }
            }
        }
        return null;
    }

    public Member findByEmail(String email) throws SQLException {
        String sql = "SELECT member_id, name, email, member_type, max_books, registered_on FROM members WHERE LOWER(email) = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email.trim().toLowerCase());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToMember(rs);
                }
            }
        }
        return null;
    }

    public List<Member> getAllMembers() throws SQLException {
        List<Member> members = new ArrayList<>();
        String sql = "SELECT member_id, name, email, member_type, max_books, registered_on FROM members ORDER BY member_id ASC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                members.add(mapRowToMember(rs));
            }
        }
        return members;
    }

    @Override
    public List<Member> search(String keyword) {
        List<Member> results = new ArrayList<>();
        String sql = """
            SELECT member_id, name, email, member_type, max_books, registered_on
            FROM members
            WHERE LOWER(name) LIKE ?
               OR LOWER(email) LIKE ?
               OR CAST(member_id AS TEXT) = ?
            ORDER BY member_id ASC
        """;

        String pattern = "%" + keyword.toLowerCase().trim() + "%";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, pattern);
            pstmt.setString(2, pattern);
            pstmt.setString(3, keyword.trim());

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    results.add(mapRowToMember(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Database error during member search: " + e.getMessage());
        }
        return results;
    }

    /**
     * Polymorphic factory method: instantiates Student or Faculty based on stored member_type.
     */
    private Member mapRowToMember(ResultSet rs) throws SQLException {
        int id = rs.getInt("member_id");
        String name = rs.getString("name");
        String email = rs.getString("email");
        String typeStr = rs.getString("member_type");
        LocalDate registered = DateUtil.parse(rs.getString("registered_on"));

        Member.MemberType type = Member.MemberType.fromString(typeStr);
        if (type == Member.MemberType.FACULTY) {
            return new Faculty(id, name, email, registered);
        } else {
            return new Student(id, name, email, registered);
        }
    }
}
