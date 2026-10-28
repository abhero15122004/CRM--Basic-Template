package com.example.crm.exception;

/**
 * Exception thrown when a requested customer is not found in the database.
 */
public class CustomerNotFoundException extends RuntimeException {

    public CustomerNotFoundException(Long id) {
        super("Customer not found with ID: " + id);
    }

    public CustomerNotFoundException(String message) {
        super(message);
    }
}
