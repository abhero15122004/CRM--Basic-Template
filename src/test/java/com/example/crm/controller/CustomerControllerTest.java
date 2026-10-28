package com.example.crm.controller;

import com.example.crm.entity.Customer;
import com.example.crm.exception.CustomerNotFoundException;
import com.example.crm.service.CustomerService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CustomerController.class)
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CustomerService customerService;

    @Test
    @DisplayName("GET / redirects to /customers")
    void testRootRedirect() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/customers"));
    }

    @Test
    @DisplayName("GET /customers renders customers view with customer list")
    void testListCustomers() throws Exception {
        Customer c1 = new Customer(1L, "Grace", "Hopper", "grace@example.com");
        Customer c2 = new Customer(2L, "Ada", "Lovelace", "ada@example.com");

        when(customerService.getAllCustomers()).thenReturn(Arrays.asList(c1, c2));

        mockMvc.perform(get("/customers"))
                .andExpect(status().isOk())
                .andExpect(view().name("customers"))
                .andExpect(model().attributeExists("customers"))
                .andExpect(model().attribute("customers", hasSize(2)))
                .andExpect(content().string(containsString("Grace")))
                .andExpect(content().string(containsString("Ada")));

        verify(customerService, times(1)).getAllCustomers();
    }

    @Test
    @DisplayName("GET /customers renders empty state when no customers exist")
    void testListCustomers_Empty() throws Exception {
        when(customerService.getAllCustomers()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/customers"))
                .andExpect(status().isOk())
                .andExpect(view().name("customers"))
                .andExpect(content().string(containsString("No Customers Found")));

        verify(customerService, times(1)).getAllCustomers();
    }

    @Test
    @DisplayName("GET /customers/new renders customer form for creation")
    void testShowNewCustomerForm() throws Exception {
        mockMvc.perform(get("/customers/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("customer-form"))
                .andExpect(model().attributeExists("customer"))
                .andExpect(model().attribute("pageTitle", "Add Customer"))
                .andExpect(content().string(containsString("Add Customer")));
    }

    @Test
    @DisplayName("POST /customers/save creates valid customer and redirects with success message")
    void testSaveCustomer_CreateSuccess() throws Exception {
        when(customerService.isEmailDuplicate("alan.turing@example.com", null)).thenReturn(false);
        when(customerService.saveCustomer(any(Customer.class)))
                .thenReturn(new Customer(1L, "Alan", "Turing", "alan.turing@example.com"));

        mockMvc.perform(post("/customers/save")
                        .param("firstName", "Alan")
                        .param("lastName", "Turing")
                        .param("email", "alan.turing@example.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/customers"))
                .andExpect(flash().attributeExists("successMessage"))
                .andExpect(flash().attribute("successMessage", containsString("successfully added")));

        verify(customerService, times(1)).saveCustomer(any(Customer.class));
    }

    @Test
    @DisplayName("POST /customers/save rejects empty required fields and renders errors")
    void testSaveCustomer_MissingRequiredFields() throws Exception {
        mockMvc.perform(post("/customers/save")
                        .param("firstName", "")
                        .param("lastName", "")
                        .param("email", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("customer-form"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeHasFieldErrors("customer", "firstName"))
                .andExpect(model().attributeHasFieldErrors("customer", "lastName"))
                .andExpect(model().attributeHasFieldErrors("customer", "email"));

        verify(customerService, never()).saveCustomer(any(Customer.class));
    }

    @Test
    @DisplayName("POST /customers/save rejects invalid email format")
    void testSaveCustomer_InvalidEmailFormat() throws Exception {
        mockMvc.perform(post("/customers/save")
                        .param("firstName", "John")
                        .param("lastName", "Doe")
                        .param("email", "not-a-valid-email"))
                .andExpect(status().isOk())
                .andExpect(view().name("customer-form"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeHasFieldErrors("customer", "email"));

        verify(customerService, never()).saveCustomer(any(Customer.class));
    }

    @Test
    @DisplayName("POST /customers/save rejects duplicate email for new customer")
    void testSaveCustomer_DuplicateEmailRejection() throws Exception {
        when(customerService.isEmailDuplicate("existing@example.com", null)).thenReturn(true);

        mockMvc.perform(post("/customers/save")
                        .param("firstName", "John")
                        .param("lastName", "Doe")
                        .param("email", "existing@example.com"))
                .andExpect(status().isOk())
                .andExpect(view().name("customer-form"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeHasFieldErrorCode("customer", "email", "duplicate.email"));

        verify(customerService, never()).saveCustomer(any(Customer.class));
    }

    @Test
    @DisplayName("GET /customers/edit/{id} renders form with existing customer data")
    void testShowEditCustomerForm_Success() throws Exception {
        Customer existing = new Customer(5L, "Katherine", "Johnson", "katherine@example.com");
        when(customerService.getCustomerById(5L)).thenReturn(existing);

        mockMvc.perform(get("/customers/edit/5"))
                .andExpect(status().isOk())
                .andExpect(view().name("customer-form"))
                .andExpect(model().attributeExists("customer"))
                .andExpect(model().attribute("pageTitle", "Edit Customer"))
                .andExpect(content().string(containsString("Katherine")))
                .andExpect(content().string(containsString("Johnson")));

        verify(customerService, times(1)).getCustomerById(5L);
    }

    @Test
    @DisplayName("GET /customers/edit/{id} redirects with error when customer does not exist")
    void testShowEditCustomerForm_NotFound() throws Exception {
        when(customerService.getCustomerById(999L)).thenThrow(new CustomerNotFoundException(999L));

        mockMvc.perform(get("/customers/edit/999"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/customers"))
                .andExpect(flash().attributeExists("errorMessage"))
                .andExpect(flash().attribute("errorMessage", containsString("999")));

        verify(customerService, times(1)).getCustomerById(999L);
    }

    @Test
    @DisplayName("POST /customers/save allows existing customer to retain their own email")
    void testUpdateCustomer_RetainingOwnEmail_Success() throws Exception {
        when(customerService.isEmailDuplicate("katherine@example.com", 5L)).thenReturn(false);
        when(customerService.saveCustomer(any(Customer.class)))
                .thenReturn(new Customer(5L, "Katherine", "Johnson-Updated", "katherine@example.com"));

        mockMvc.perform(post("/customers/save")
                        .param("id", "5")
                        .param("firstName", "Katherine")
                        .param("lastName", "Johnson-Updated")
                        .param("email", "katherine@example.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/customers"))
                .andExpect(flash().attributeExists("successMessage"))
                .andExpect(flash().attribute("successMessage", containsString("successfully updated")));

        verify(customerService, times(1)).saveCustomer(any(Customer.class));
    }

    @Test
    @DisplayName("POST /customers/save rejects update when email is taken by another customer")
    void testUpdateCustomer_DuplicateEmailConflict() throws Exception {
        when(customerService.isEmailDuplicate("conflict@example.com", 5L)).thenReturn(true);

        mockMvc.perform(post("/customers/save")
                        .param("id", "5")
                        .param("firstName", "Katherine")
                        .param("lastName", "Johnson")
                        .param("email", "conflict@example.com"))
                .andExpect(status().isOk())
                .andExpect(view().name("customer-form"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeHasFieldErrorCode("customer", "email", "duplicate.email"));

        verify(customerService, never()).saveCustomer(any(Customer.class));
    }

    @Test
    @DisplayName("POST /customers/delete/{id} deletes customer and redirects with success message")
    void testDeleteCustomer_Success() throws Exception {
        doNothing().when(customerService).deleteCustomerById(5L);

        mockMvc.perform(post("/customers/delete/5"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/customers"))
                .andExpect(flash().attributeExists("successMessage"))
                .andExpect(flash().attribute("successMessage", containsString("successfully deleted")));

        verify(customerService, times(1)).deleteCustomerById(5L);
    }

    @Test
    @DisplayName("POST /customers/delete/{id} handles nonexistent customer ID gracefully")
    void testDeleteCustomer_NotFoundGracefulHandling() throws Exception {
        doThrow(new CustomerNotFoundException(999L)).when(customerService).deleteCustomerById(999L);

        mockMvc.perform(post("/customers/delete/999"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/customers"))
                .andExpect(flash().attributeExists("errorMessage"))
                .andExpect(flash().attribute("errorMessage", containsString("999")));

        verify(customerService, times(1)).deleteCustomerById(999L);
    }
}
