package com.example.crm.exception;

/**
 * Exception thrown when attempting to create or update a customer with an email
 * address that is already registered to another customer.
 */
public class DuplicateEmailException extends RuntimeException {

    public DuplicateEmailException(String email) {
        super("A customer with email '" + email + "' already exists.");
    }
}
