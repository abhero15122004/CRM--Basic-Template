package com.example.crm.service;

import com.example.crm.entity.Customer;
import com.example.crm.exception.CustomerNotFoundException;
import com.example.crm.exception.DuplicateEmailException;
import com.example.crm.repository.CustomerRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementation of CustomerService handling business logic, validation,
 * transaction boundaries, and interaction with CustomerRepository.
 */
@Service
@Transactional(readOnly = true)
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;

    /**
     * Constructor injection for the CustomerRepository dependency.
     *
     * @param customerRepository the repository to interact with customer data
     */
    public CustomerServiceImpl(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Override
    public List<Customer> getAllCustomers() {
        return customerRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
    }

    @Override
    public Customer getCustomerById(Long id) {
        if (id == null) {
            throw new CustomerNotFoundException("Customer ID cannot be null.");
        }
        return customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(id));
    }

    @Override
    @Transactional
    public Customer createCustomer(Customer customer) {
        if (customer == null) {
            throw new IllegalArgumentException("Customer cannot be null.");
        }

        String normalizedEmail = normalizeEmail(customer.getEmail());
        customer.setEmail(normalizedEmail);

        if (customerRepository.existsByEmail(normalizedEmail)) {
            throw new DuplicateEmailException(normalizedEmail);
        }

        customer.setId(null); // Ensure fresh insertion
        return customerRepository.save(customer);
    }

    @Override
    @Transactional
    public Customer updateCustomer(Long id, Customer customerDetails) {
        if (id == null) {
            throw new CustomerNotFoundException("Customer ID cannot be null.");
        }
        if (customerDetails == null) {
            throw new IllegalArgumentException("Updated customer details cannot be null.");
        }

        Customer existingCustomer = customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(id));

        String normalizedEmail = normalizeEmail(customerDetails.getEmail());

        if (customerRepository.existsByEmailAndIdNot(normalizedEmail, id)) {
            throw new DuplicateEmailException(normalizedEmail);
        }

        existingCustomer.setFirstName(customerDetails.getFirstName());
        existingCustomer.setLastName(customerDetails.getLastName());
        existingCustomer.setEmail(normalizedEmail);

        return customerRepository.save(existingCustomer);
    }

    @Override
    @Transactional
    public Customer saveCustomer(Customer customer) {
        if (customer == null) {
            throw new IllegalArgumentException("Customer cannot be null.");
        }

        if (customer.getId() != null) {
            return updateCustomer(customer.getId(), customer);
        } else {
            return createCustomer(customer);
        }
    }

    @Override
    @Transactional
    public void deleteCustomerById(Long id) {
        if (id == null) {
            throw new CustomerNotFoundException("Customer ID cannot be null.");
        }

        if (!customerRepository.existsById(id)) {
            throw new CustomerNotFoundException(id);
        }

        customerRepository.deleteById(id);
    }

    @Override
    public boolean isEmailDuplicate(String email, Long currentCustomerId) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }

        String normalizedEmail = normalizeEmail(email);

        if (currentCustomerId == null) {
            return customerRepository.existsByEmail(normalizedEmail);
        } else {
            return customerRepository.existsByEmailAndIdNot(normalizedEmail, currentCustomerId);
        }
    }

    private String normalizeEmail(String email) {
        return email != null ? email.trim().toLowerCase() : null;
    }
}
