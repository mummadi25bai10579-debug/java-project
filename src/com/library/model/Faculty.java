package com.library.model;

import java.time.LocalDate;

/**
 * Concrete class representing a Faculty member.
 * Borrowing limit: 5 books.
 * Loan duration: 30 days.
 */
public class Faculty extends Member {
    public static final int FACULTY_MAX_BOOKS = 5;
    public static final int FACULTY_LOAN_PERIOD_DAYS = 30;

    public Faculty() {
        super();
        setMemberType(MemberType.FACULTY);
    }

    public Faculty(int memberId, String name, String email, LocalDate registeredOn) {
        super(memberId, name, email, MemberType.FACULTY, registeredOn);
    }

    public Faculty(String name, String email) {
        super(name, email, MemberType.FACULTY);
    }

    @Override
    public int getMaxBooksAllowed() {
        return FACULTY_MAX_BOOKS;
    }

    @Override
    public int getLoanPeriodDays() {
        return FACULTY_LOAN_PERIOD_DAYS;
    }
}
