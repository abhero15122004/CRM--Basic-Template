package com.example.crm.repository;

import com.example.crm.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for Customer entity operations.
 */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    /**
     * Finds a customer by their unique email address.
     *
     * @param email the email address to look up
     * @return an Optional containing the Customer if found
     */
    Optional<Customer> findByEmail(String email);

    /**
     * Checks if a customer exists with the given email address.
     *
     * @param email the email address to check
     * @return true if an existing customer has this email, false otherwise
     */
    boolean existsByEmail(String email);

    /**
     * Checks if an email is already in use by any customer other than the specified ID.
     * Used during update operations to ensure email uniqueness.
     *
     * @param email the email address to check
     * @param id    the ID of the current customer being updated
     * @return true if another customer uses this email, false otherwise
     */
    boolean existsByEmailAndIdNot(String email, Long id);
}
