package com.library.model;

import java.time.LocalDate;

/**
 * Concrete class representing a Student member.
 * Borrowing limit: 3 books.
 * Loan duration: 14 days.
 */
public class Student extends Member {
    public static final int STUDENT_MAX_BOOKS = 3;
    public static final int STUDENT_LOAN_PERIOD_DAYS = 14;

    public Student() {
        super();
        setMemberType(MemberType.STUDENT);
    }

    public Student(int memberId, String name, String email, LocalDate registeredOn) {
        super(memberId, name, email, MemberType.STUDENT, registeredOn);
    }

    public Student(String name, String email) {
        super(name, email, MemberType.STUDENT);
    }

    @Override
    public int getMaxBooksAllowed() {
        return STUDENT_MAX_BOOKS;
    }

    @Override
    public int getLoanPeriodDays() {
        return STUDENT_LOAN_PERIOD_DAYS;
    }
}
