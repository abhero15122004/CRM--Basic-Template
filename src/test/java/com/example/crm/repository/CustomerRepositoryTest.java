package com.example.crm.repository;

import com.example.crm.entity.Customer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class CustomerRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CustomerRepository customerRepository;

    private Customer customer1;
    private Customer customer2;

    @BeforeEach
    void setUp() {
        customer1 = new Customer("Emma", "Watson", "emma.watson@example.com");
        customer2 = new Customer("Daniel", "Radcliffe", "daniel.r@example.com");

        entityManager.persist(customer1);
        entityManager.persist(customer2);
        entityManager.flush();
    }

    @Test
    @DisplayName("Find customer by email returns correct customer")
    void testFindByEmail_Success() {
        Optional<Customer> found = customerRepository.findByEmail("emma.watson@example.com");

        assertThat(found).isPresent();
        assertThat(found.get().getFirstName()).isEqualTo("Emma");
        assertThat(found.get().getLastName()).isEqualTo("Watson");
    }

    @Test
    @DisplayName("Find customer by nonexistent email returns empty Optional")
    void testFindByEmail_NotFound() {
        Optional<Customer> found = customerRepository.findByEmail("nonexistent@example.com");

        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("existsByEmail correctly returns true for existing email and false for new")
    void testExistsByEmail() {
        assertThat(customerRepository.existsByEmail("emma.watson@example.com")).isTrue();
        assertThat(customerRepository.existsByEmail("daniel.r@example.com")).isTrue();
        assertThat(customerRepository.existsByEmail("other@example.com")).isFalse();
    }

    @Test
    @DisplayName("existsByEmailAndIdNot returns false for current customer's email and true if another customer owns it")
    void testExistsByEmailAndIdNot() {
        Long customer1Id = customer1.getId();
        Long customer2Id = customer2.getId();

        // Customer 1 retaining their own email should return false
        assertThat(customerRepository.existsByEmailAndIdNot("emma.watson@example.com", customer1Id)).isFalse();

        // Customer 2 trying to use Customer 1's email should return true
        assertThat(customerRepository.existsByEmailAndIdNot("emma.watson@example.com", customer2Id)).isTrue();

        // An unused email should return false for any customer
        assertThat(customerRepository.existsByEmailAndIdNot("brandnew@example.com", customer1Id)).isFalse();
    }

    @Test
    @DisplayName("Persisting duplicate email triggers database uniqueness constraint violation")
    void testUniqueEmailConstraintViolation() {
        Customer duplicate = new Customer("Copy", "Cat", "emma.watson@example.com");

        assertThatThrownBy(() -> {
            customerRepository.saveAndFlush(duplicate);
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("Find all returns all persisted customers")
    void testFindAll() {
        List<Customer> all = customerRepository.findAll();

        assertThat(all).hasSize(2);
    }

    @Test
    @DisplayName("Delete customer by ID removes record from database")
    void testDeleteCustomer() {
        Long id = customer1.getId();
        customerRepository.deleteById(id);
        entityManager.flush();

        Optional<Customer> deleted = customerRepository.findById(id);
        assertThat(deleted).isEmpty();
    }
}
