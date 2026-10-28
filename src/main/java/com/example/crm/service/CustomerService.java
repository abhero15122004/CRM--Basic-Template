package com.example.crm.service;

import com.example.crm.entity.Customer;
import java.util.List;

/**
 * Service interface defining business operations for Customer management.
 */
public interface CustomerService {

    /**
     * Retrieves all registered customers sorted by ID.
     *
     * @return list of all customers
     */
    List<Customer> getAllCustomers();

    /**
     * Retrieves a single customer by their ID.
     *
     * @param id the customer ID
     * @return the Customer entity
     * @throws com.example.crm.exception.CustomerNotFoundException if customer is not found
     */
    Customer getCustomerById(Long id);

    /**
     * Creates a new customer after verifying that the email address is not already registered.
     *
     * @param customer the customer entity to create
     * @return the persisted customer entity
     * @throws com.example.crm.exception.DuplicateEmailException if the email is already in use
     */
    Customer createCustomer(Customer customer);

    /**
     * Updates an existing customer's details after verifying email uniqueness.
     *
     * @param id              the ID of the customer to update
     * @param customerDetails the updated customer data
     * @return the updated customer entity
     * @throws com.example.crm.exception.CustomerNotFoundException if the customer does not exist
     * @throws com.example.crm.exception.DuplicateEmailException   if another customer uses the new email
     */
    Customer updateCustomer(Long id, Customer customerDetails);

    /**
     * Saves a customer entity (delegating to create or update depending on ID presence).
     *
     * @param customer the customer entity
     * @return the persisted customer entity
     */
    Customer saveCustomer(Customer customer);

    /**
     * Deletes a customer by their ID.
     *
     * @param id the ID of the customer to delete
     * @throws com.example.crm.exception.CustomerNotFoundException if the customer does not exist
     */
    void deleteCustomerById(Long id);

    /**
     * Checks whether an email address is already in use.
     *
     * @param email             the email address to inspect
     * @param currentCustomerId the ID of the customer currently being edited (null for new customer)
     * @return true if the email is considered duplicate, false otherwise
     */
    boolean isEmailDuplicate(String email, Long currentCustomerId);
}
