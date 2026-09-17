package com.library.model;

import java.time.LocalDate;

/**
 * Abstract base class representing a library member.
 * Demonstrates abstraction, encapsulation, and polymorphism.
 */
public abstract class Member {

    public enum MemberType {
        STUDENT,
        FACULTY;

        public static MemberType fromString(String typeStr) {
            if (typeStr == null) return null;
            try {
                return MemberType.valueOf(typeStr.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
    }

    private int memberId;
    private String name;
    private String email;
    private MemberType memberType;
    private LocalDate registeredOn;

    public Member() {
    }

    public Member(int memberId, String name, String email, MemberType memberType, LocalDate registeredOn) {
        this.memberId = memberId;
        this.name = name;
        this.email = email;
        this.memberType = memberType;
        this.registeredOn = registeredOn != null ? registeredOn : LocalDate.now();
    }

    public Member(String name, String email, MemberType memberType) {
        this(0, name, email, memberType, LocalDate.now());
    }

    /**
     * Polymorphic method: Maximum number of active book loans permitted.
     */
    public abstract int getMaxBooksAllowed();

    /**
     * Polymorphic method: Standard borrowing duration in days.
     */
    public abstract int getLoanPeriodDays();

    public int getMemberId() {
        return memberId;
    }

    public void setMemberId(int memberId) {
        this.memberId = memberId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public MemberType getMemberType() {
        return memberType;
    }

    public void setMemberType(MemberType memberType) {
        this.memberType = memberType;
    }

    public LocalDate getRegisteredOn() {
        return registeredOn;
    }

    public void setRegisteredOn(LocalDate registeredOn) {
        this.registeredOn = registeredOn;
    }

    @Override
    public String toString() {
        return String.format("Member [ID=%d, Name='%s', Email='%s', Type=%s, MaxBooks=%d, LoanPeriod=%dd, Registered=%s]",
                memberId, name, email, memberType, getMaxBooksAllowed(), getLoanPeriodDays(), registeredOn);
    }
}
