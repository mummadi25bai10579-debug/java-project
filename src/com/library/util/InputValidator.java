package com.library.util;

import com.library.exception.InvalidInputException;
import com.library.model.Member;

import java.util.regex.Pattern;

/**
 * Utility class providing validation for all user input and entity fields.
 */
public final class InputValidator {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    // Standard ISBN-10 or ISBN-13 format allowing hyphens
    private static final Pattern ISBN_PATTERN = Pattern.compile("^(?:\\d[- ]*){9}[\\dXx]|(?:\\d[- ]*){13}$");

    private InputValidator() {
        // Prevent instantiation
    }

    public static void validateNonEmpty(String value, String fieldName) throws InvalidInputException {
        if (value == null || value.trim().isEmpty()) {
            throw new InvalidInputException(fieldName + " cannot be empty.");
        }
    }

    public static void validateEmail(String email) throws InvalidInputException {
        validateNonEmpty(email, "Email");
        if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
            throw new InvalidInputException("Invalid email format: '" + email + "'. Expected user@domain.com");
        }
    }

    public static void validateIsbn(String isbn) throws InvalidInputException {
        validateNonEmpty(isbn, "ISBN");
        String sanitized = isbn.trim().replace("-", "").replace(" ", "");
        if (sanitized.length() != 10 && sanitized.length() != 13) {
            throw new InvalidInputException("Invalid ISBN: must be 10 or 13 digits. Provided: '" + isbn + "'");
        }
        if (!ISBN_PATTERN.matcher(isbn.trim()).matches()) {
            throw new InvalidInputException("Invalid ISBN format: '" + isbn + "'");
        }
    }

    public static void validatePositiveInt(int value, String fieldName) throws InvalidInputException {
        if (value <= 0) {
            throw new InvalidInputException(fieldName + " must be a positive integer greater than 0.");
        }
    }

    public static void validateNonNegativeInt(int value, String fieldName) throws InvalidInputException {
        if (value < 0) {
            throw new InvalidInputException(fieldName + " cannot be negative.");
        }
    }

    public static Member.MemberType validateMemberType(String typeStr) throws InvalidInputException {
        validateNonEmpty(typeStr, "Member Type");
        Member.MemberType type = Member.MemberType.fromString(typeStr);
        if (type == null) {
            throw new InvalidInputException("Invalid member type: '" + typeStr + "'. Allowed types: STUDENT, FACULTY");
        }
        return type;
    }
}
