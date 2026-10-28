package com.example.crm.service;

import com.example.crm.entity.Customer;
import com.example.crm.exception.CustomerNotFoundException;
import com.example.crm.exception.DuplicateEmailException;
import com.example.crm.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerServiceImpl customerService;

    private Customer sampleCustomer1;
    private Customer sampleCustomer2;

    @BeforeEach
    void setUp() {
        sampleCustomer1 = new Customer(1L, "Alice", "Smith", "alice.smith@example.com");
        sampleCustomer2 = new Customer(2L, "Bob", "Jones", "bob.jones@example.com");
    }

    @Test
    @DisplayName("Retrieve all customers sorted by ID")
    void testGetAllCustomers() {
        when(customerRepository.findAll(any(Sort.class)))
                .thenReturn(Arrays.asList(sampleCustomer1, sampleCustomer2));

        List<Customer> result = customerService.getAllCustomers();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getFirstName()).isEqualTo("Alice");
        assertThat(result.get(1).getFirstName()).isEqualTo("Bob");
        verify(customerRepository, times(1)).findAll(any(Sort.class));
    }

    @Test
    @DisplayName("Retrieve customers when database is empty")
    void testGetAllCustomers_Empty() {
        when(customerRepository.findAll(any(Sort.class)))
                .thenReturn(Collections.emptyList());

        List<Customer> result = customerService.getAllCustomers();

        assertThat(result).isEmpty();
        verify(customerRepository, times(1)).findAll(any(Sort.class));
    }

    @Test
    @DisplayName("Get customer by existing ID returns customer")
    void testGetCustomerById_Success() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(sampleCustomer1));

        Customer result = customerService.getCustomerById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getEmail()).isEqualTo("alice.smith@example.com");
        verify(customerRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Get customer by nonexistent ID throws CustomerNotFoundException")
    void testGetCustomerById_NotFound() {
        when(customerRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customerService.getCustomerById(999L))
                .isInstanceOf(CustomerNotFoundException.class)
                .hasMessageContaining("999");

        verify(customerRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("Create customer with valid and unique email successfully saves")
    void testCreateCustomer_Success() {
        Customer newCustomer = new Customer(null, "Charlie", "Brown", "charlie@example.com");
        Customer savedCustomer = new Customer(3L, "Charlie", "Brown", "charlie@example.com");

        when(customerRepository.existsByEmail("charlie@example.com")).thenReturn(false);
        when(customerRepository.save(any(Customer.class))).thenReturn(savedCustomer);

        Customer result = customerService.createCustomer(newCustomer);

        assertThat(result.getId()).isEqualTo(3L);
        assertThat(result.getEmail()).isEqualTo("charlie@example.com");
        verify(customerRepository, times(1)).existsByEmail("charlie@example.com");
        verify(customerRepository, times(1)).save(newCustomer);
    }

    @Test
    @DisplayName("Create customer with duplicate email throws DuplicateEmailException")
    void testCreateCustomer_DuplicateEmail() {
        Customer duplicateCustomer = new Customer(null, "Alice", "Duplicate", "alice.smith@example.com");

        when(customerRepository.existsByEmail("alice.smith@example.com")).thenReturn(true);

        assertThatThrownBy(() -> customerService.createCustomer(duplicateCustomer))
                .isInstanceOf(DuplicateEmailException.class)
                .hasMessageContaining("alice.smith@example.com");

        verify(customerRepository, times(1)).existsByEmail("alice.smith@example.com");
        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    @DisplayName("Update customer details successfully while retaining own email")
    void testUpdateCustomer_RetainOwnEmail_Success() {
        Customer updatedDetails = new Customer(1L, "Alice Updated", "Smith Updated", "alice.smith@example.com");

        when(customerRepository.findById(1L)).thenReturn(Optional.of(sampleCustomer1));
        when(customerRepository.existsByEmailAndIdNot("alice.smith@example.com", 1L)).thenReturn(false);
        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Customer result = customerService.updateCustomer(1L, updatedDetails);

        assertThat(result.getFirstName()).isEqualTo("Alice Updated");
        assertThat(result.getLastName()).isEqualTo("Smith Updated");
        assertThat(result.getEmail()).isEqualTo("alice.smith@example.com");
        verify(customerRepository, times(1)).existsByEmailAndIdNot("alice.smith@example.com", 1L);
        verify(customerRepository, times(1)).save(sampleCustomer1);
    }

    @Test
    @DisplayName("Update customer with new unique email succeeds")
    void testUpdateCustomer_NewUniqueEmail_Success() {
        Customer updatedDetails = new Customer(1L, "Alice", "Smith", "alice.new@example.com");

        when(customerRepository.findById(1L)).thenReturn(Optional.of(sampleCustomer1));
        when(customerRepository.existsByEmailAndIdNot("alice.new@example.com", 1L)).thenReturn(false);
        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Customer result = customerService.updateCustomer(1L, updatedDetails);

        assertThat(result.getEmail()).isEqualTo("alice.new@example.com");
        verify(customerRepository, times(1)).existsByEmailAndIdNot("alice.new@example.com", 1L);
        verify(customerRepository, times(1)).save(sampleCustomer1);
    }

    @Test
    @DisplayName("Update customer with email belonging to another customer throws DuplicateEmailException")
    void testUpdateCustomer_DuplicateEmailConflict() {
        Customer updatedDetails = new Customer(1L, "Alice", "Smith", "bob.jones@example.com");

        when(customerRepository.findById(1L)).thenReturn(Optional.of(sampleCustomer1));
        when(customerRepository.existsByEmailAndIdNot("bob.jones@example.com", 1L)).thenReturn(true);

        assertThatThrownBy(() -> customerService.updateCustomer(1L, updatedDetails))
                .isInstanceOf(DuplicateEmailException.class)
                .hasMessageContaining("bob.jones@example.com");

        verify(customerRepository, times(1)).existsByEmailAndIdNot("bob.jones@example.com", 1L);
        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    @DisplayName("Update nonexistent customer throws CustomerNotFoundException")
    void testUpdateCustomer_NotFound() {
        Customer updatedDetails = new Customer(999L, "Nobody", "None", "nobody@example.com");

        when(customerRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customerService.updateCustomer(999L, updatedDetails))
                .isInstanceOf(CustomerNotFoundException.class)
                .hasMessageContaining("999");

        verify(customerRepository, times(1)).findById(999L);
        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    @DisplayName("Delete customer by existing ID successfully removes record")
    void testDeleteCustomer_Success() {
        when(customerRepository.existsById(1L)).thenReturn(true);
        doNothing().when(customerRepository).deleteById(1L);

        customerService.deleteCustomerById(1L);

        verify(customerRepository, times(1)).existsById(1L);
        verify(customerRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Delete nonexistent customer throws CustomerNotFoundException")
    void testDeleteCustomer_NotFound() {
        when(customerRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> customerService.deleteCustomerById(999L))
                .isInstanceOf(CustomerNotFoundException.class)
                .hasMessageContaining("999");

        verify(customerRepository, times(1)).existsById(999L);
        verify(customerRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("isEmailDuplicate detects duplicate for new customer")
    void testIsEmailDuplicate_NewCustomer() {
        when(customerRepository.existsByEmail("alice.smith@example.com")).thenReturn(true);
        when(customerRepository.existsByEmail("unused@example.com")).thenReturn(false);

        assertThat(customerService.isEmailDuplicate("alice.smith@example.com", null)).isTrue();
        assertThat(customerService.isEmailDuplicate("unused@example.com", null)).isFalse();
    }

    @Test
    @DisplayName("isEmailDuplicate allows same customer to keep their email but flags another customer's email")
    void testIsEmailDuplicate_ExistingCustomer() {
        when(customerRepository.existsByEmailAndIdNot("alice.smith@example.com", 1L)).thenReturn(false);
        when(customerRepository.existsByEmailAndIdNot("bob.jones@example.com", 1L)).thenReturn(true);

        assertThat(customerService.isEmailDuplicate("alice.smith@example.com", 1L)).isFalse();
        assertThat(customerService.isEmailDuplicate("bob.jones@example.com", 1L)).isTrue();
    }
}
