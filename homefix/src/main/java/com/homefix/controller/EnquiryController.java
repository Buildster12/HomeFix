package com.homefix.controller;

import com.homefix.model.Customer;
import com.homefix.model.ServiceEnquiry;
import com.homefix.service.ServiceEnquiryService;
import com.homefix.service.ServiceCategoryService;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class EnquiryController {

    private final ServiceEnquiryService service;
    private final ServiceCategoryService categoryService;

    public EnquiryController(
            ServiceEnquiryService service,
            ServiceCategoryService categoryService) {

        this.service = service;
        this.categoryService = categoryService;
    }

    // Show enquiry form
    @GetMapping("/enquiry")
    public String showEnquiryForm(
            Model model,
            HttpSession session) {

        if (session.getAttribute("loggedInCustomer") == null) {
            return "redirect:/customer/login";
        }

        model.addAttribute("enquiry", new ServiceEnquiry());

        model.addAttribute(
                "categories",
                categoryService.getAllCategories()
        );

        return "enquiry";
    }

    // Submit enquiry
    @PostMapping("/enquiry")
    public String submitEnquiry(
            @ModelAttribute("enquiry") ServiceEnquiry enquiry,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Customer customer =
                (Customer) session.getAttribute("loggedInCustomer");

        if (customer == null) {
            return "redirect:/customer/login";
        }

        // Use logged-in customer's details
        enquiry.setCustomerName(customer.getName());
        enquiry.setEmail(customer.getEmail());

        // Every new enquiry starts as Pending
        enquiry.setStatus("Pending");

        service.saveEnquiry(enquiry);

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Your enquiry has been submitted successfully!"
        );

        return "redirect:/enquiry/success";
    }
    // Enquiry success page
    @GetMapping("/enquiry/success")
    public String showSuccessPage() {
        return "enquiry-success";
    }

    // Customer's bookings
    @GetMapping("/customer/my-bookings")
    public String showMyBookings(
            HttpSession session,
            Model model) {

        Customer customer =
                (Customer) session.getAttribute("loggedInCustomer");

        if (customer == null) {
            return "redirect:/customer/login";
        }

        model.addAttribute(
                "enquiries",
                service.getCustomerEnquiries(customer.getEmail())
        );

        return "my-bookings";
    }
    @PostMapping("/customer/my-bookings/{id}/cancel")
    public String cancelBooking(
            @PathVariable int id,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Customer customer =
                (Customer) session.getAttribute("loggedInCustomer");

        if (customer == null) {
            return "redirect:/customer/login";
        }

        boolean cancelled = service.cancelCustomerEnquiry(
                id, customer.getEmail()
        );

        if (cancelled) {
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Booking cancelled successfully."
            );
        } else {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Booking could not be cancelled. Only your pending bookings can be cancelled."
            );
        }

        return "redirect:/customer/my-bookings";
    }
}