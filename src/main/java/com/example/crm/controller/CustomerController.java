package com.example.crm.controller;

import com.example.crm.entity.Customer;
import com.example.crm.exception.CustomerNotFoundException;
import com.example.crm.exception.DuplicateEmailException;
import com.example.crm.service.CustomerService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Spring MVC Controller handling web requests for Customer CRUD operations.
 */
@Controller
public class CustomerController {

    private static final Logger log = LoggerFactory.getLogger(CustomerController.class);

    private final CustomerService customerService;

    /**
     * Constructor injection of CustomerService.
     *
     * @param customerService service handling customer business operations
     */
    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    /**
     * Redirects the root path '/' to the main customer list page.
     */
    @GetMapping("/")
    public String rootRedirect() {
        return "redirect:/customers";
    }

    /**
     * Displays the complete list of customers.
     *
     * @param model the UI Model
     * @return Thymeleaf view 'customers'
     */
    @GetMapping("/customers")
    public String listCustomers(Model model) {
        log.info("Fetching customer list");
        List<Customer> customers = customerService.getAllCustomers();
        model.addAttribute("customers", customers);
        return "customers";
    }

    /**
     * Displays the form to create a new customer.
     *
     * @param model the UI Model
     * @return Thymeleaf view 'customer-form'
     */
    @GetMapping("/customers/new")
    public String showNewCustomerForm(Model model) {
        log.info("Displaying new customer form");
        model.addAttribute("customer", new Customer());
        model.addAttribute("pageTitle", "Add Customer");
        return "customer-form";
    }

    /**
     * Handles customer form submission for both new creation and updates.
     * Implements server-side validation and the Post/Redirect/Get pattern.
     *
     * @param customer           the customer entity populated from form
     * @param bindingResult      holds validation results and binding errors
     * @param redirectAttributes flash attributes passed across redirects
     * @param model              the UI Model
     * @return redirect to /customers on success, or customer-form view on validation failure
     */
    @PostMapping("/customers/save")
    public String saveCustomer(
            @Valid @ModelAttribute("customer") Customer customer,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {

        boolean isUpdate = (customer.getId() != null);
        String actionType = isUpdate ? "update" : "creation";
        log.info("Processing customer {} for email: {}", actionType, customer.getEmail());

        // Application-level duplicate email check before attempting DB persistence
        if (customer.getEmail() != null && !customer.getEmail().trim().isEmpty()) {
            if (customerService.isEmailDuplicate(customer.getEmail(), customer.getId())) {
                bindingResult.rejectValue("email", "duplicate.email", "This email address is already in use by another customer.");
            }
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("pageTitle", isUpdate ? "Edit Customer" : "Add Customer");
            return "customer-form";
        }

        try {
            customerService.saveCustomer(customer);
            String successMessage = isUpdate
                    ? "Customer '" + customer.getFirstName() + " " + customer.getLastName() + "' was successfully updated."
                    : "Customer '" + customer.getFirstName() + " " + customer.getLastName() + "' was successfully added.";
            redirectAttributes.addFlashAttribute("successMessage", successMessage);
            return "redirect:/customers";
        } catch (DuplicateEmailException ex) {
            bindingResult.rejectValue("email", "duplicate.email", ex.getMessage());
            model.addAttribute("pageTitle", isUpdate ? "Edit Customer" : "Add Customer");
            return "customer-form";
        } catch (CustomerNotFoundException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/customers";
        }
    }

    /**
     * Displays the edit form populated with the selected customer's data.
     *
     * @param id                 the ID of the customer to edit
     * @param model              the UI Model
     * @param redirectAttributes flash attributes passed across redirects
     * @return customer-form view, or redirects to /customers if not found
     */
    @GetMapping("/customers/edit/{id}")
    public String showEditCustomerForm(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        log.info("Loading edit form for customer ID: {}", id);
        try {
            Customer customer = customerService.getCustomerById(id);
            model.addAttribute("customer", customer);
            model.addAttribute("pageTitle", "Edit Customer");
            return "customer-form";
        } catch (CustomerNotFoundException ex) {
            log.warn("Attempted to edit nonexistent customer ID: {}", id);
            redirectAttributes.addFlashAttribute("errorMessage", "Customer with ID " + id + " was not found.");
            return "redirect:/customers";
        }
    }

    /**
     * Deletes a customer by their ID via HTTP POST.
     * Handles nonexistent customer IDs gracefully.
     *
     * @param id                 the ID of the customer to delete
     * @param redirectAttributes flash attributes passed across redirects
     * @return redirect to /customers with status message
     */
    @PostMapping("/customers/delete/{id}")
    public String deleteCustomer(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        log.info("Deleting customer with ID: {}", id);
        try {
            customerService.deleteCustomerById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Customer was successfully deleted.");
        } catch (CustomerNotFoundException ex) {
            log.warn("Cannot delete customer: ID {} not found", id);
            redirectAttributes.addFlashAttribute("errorMessage", "Cannot delete: Customer with ID " + id + " does not exist.");
        }
        return "redirect:/customers";
    }
}
