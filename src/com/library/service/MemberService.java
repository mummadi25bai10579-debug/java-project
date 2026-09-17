package com.library.service;

import com.library.dao.MemberDAO;
import com.library.dao.TransactionDAO;
import com.library.exception.InvalidInputException;
import com.library.exception.LibraryException;
import com.library.model.Faculty;
import com.library.model.Member;
import com.library.model.Student;
import com.library.util.InputValidator;

import java.sql.SQLException;
import java.util.List;

/**
 * Service handling member registration, updates, and business validation.
 */
public class MemberService {

    private final MemberDAO memberDAO;
    private final TransactionDAO transactionDAO;

    public MemberService() {
        this.memberDAO = new MemberDAO();
        this.transactionDAO = new TransactionDAO();
    }

    public MemberService(MemberDAO memberDAO, TransactionDAO transactionDAO) {
        this.memberDAO = memberDAO;
        this.transactionDAO = transactionDAO;
    }

    public Member registerMember(String name, String email, String memberTypeStr) throws LibraryException {
        InputValidator.validateNonEmpty(name, "Name");
        InputValidator.validateEmail(email);
        Member.MemberType type = InputValidator.validateMemberType(memberTypeStr);

        try {
            Member existing = memberDAO.findByEmail(email);
            if (existing != null) {
                throw new InvalidInputException("Member with email '" + email + "' already exists (ID: " + existing.getMemberId() + ").");
            }

            Member member;
            if (type == Member.MemberType.FACULTY) {
                member = new Faculty(name.trim(), email.trim().toLowerCase());
            } else {
                member = new Student(name.trim(), email.trim().toLowerCase());
            }

            boolean created = memberDAO.addMember(member);
            if (!created) {
                throw new LibraryException("Failed to register member in the database.");
            }
            return member;
        } catch (SQLException e) {
            throw new LibraryException("Database error registering member: " + e.getMessage(), e);
        }
    }

    public boolean updateMember(int memberId, String name, String email) throws LibraryException {
        InputValidator.validatePositiveInt(memberId, "Member ID");
        InputValidator.validateNonEmpty(name, "Name");
        InputValidator.validateEmail(email);

        try {
            Member existing = memberDAO.findById(memberId);
            if (existing == null) {
                throw new LibraryException("Member ID " + memberId + " does not exist.");
            }

            Member emailOwner = memberDAO.findByEmail(email);
            if (emailOwner != null && emailOwner.getMemberId() != memberId) {
                throw new InvalidInputException("Email '" + email + "' is already registered to member ID " + emailOwner.getMemberId());
            }

            existing.setName(name.trim());
            existing.setEmail(email.trim().toLowerCase());
            return memberDAO.updateMember(existing);
        } catch (SQLException e) {
            throw new LibraryException("Database error updating member: " + e.getMessage(), e);
        }
    }

    public boolean deleteMember(int memberId) throws LibraryException {
        InputValidator.validatePositiveInt(memberId, "Member ID");
        try {
            Member existing = memberDAO.findById(memberId);
            if (existing == null) {
                throw new LibraryException("Member ID " + memberId + " does not exist.");
            }

            int activeLoans = transactionDAO.getActiveTransactionCountByMember(memberId);
            if (activeLoans > 0) {
                throw new LibraryException("Cannot delete member: Member currently has " + activeLoans + " active book loan(s).");
            }

            return memberDAO.deleteMember(memberId);
        } catch (SQLException e) {
            throw new LibraryException("Database error deleting member: " + e.getMessage(), e);
        }
    }

    public Member getMemberById(int memberId) throws LibraryException {
        InputValidator.validatePositiveInt(memberId, "Member ID");
        try {
            Member member = memberDAO.findById(memberId);
            if (member == null) {
                throw new LibraryException("Member ID " + memberId + " does not exist.");
            }
            return member;
        } catch (SQLException e) {
            throw new LibraryException("Database error retrieving member: " + e.getMessage(), e);
        }
    }

    public List<Member> getAllMembers() throws LibraryException {
        try {
            return memberDAO.getAllMembers();
        } catch (SQLException e) {
            throw new LibraryException("Database error fetching members: " + e.getMessage(), e);
        }
    }

    public List<Member> searchMembers(String keyword) throws LibraryException {
        InputValidator.validateNonEmpty(keyword, "Search keyword");
        return memberDAO.search(keyword);
    }
}
