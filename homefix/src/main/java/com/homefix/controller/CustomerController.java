package com.homefix.controller;

import com.homefix.model.Customer;
import com.homefix.service.CustomerService;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    // ==================== CUSTOMER REGISTRATION ====================

    @GetMapping("/customer/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("customer", new Customer());
        return "customer-register";
    }

    @PostMapping("/customer/send-otp")
    public String sendOtp(@ModelAttribute Customer customer,
                          HttpSession session,
                          Model model) {

        if (customerService.isEmailRegistered(customer.getEmail())) {
            model.addAttribute("errorMessage", "Email already registered!");
            model.addAttribute("customer", customer);
            return "customer-register";
        }

        session.setAttribute("pendingCustomer", customer);

        try {
            customerService.storeOtpInSession(
                    session, customer.getEmail()
            );

            return "redirect:/customer/verify-otp";

        } catch (Exception e) {
            session.removeAttribute("pendingCustomer");

            model.addAttribute(
                    "errorMessage",
                    "OTP email could not be sent. Check your email configuration."
            );

            model.addAttribute("customer", customer);

            return "customer-register";
        }
    }

    // ==================== REGISTRATION OTP VERIFICATION ====================

    @GetMapping("/customer/verify-otp")
    public String showOtpForm(HttpSession session, Model model) {

        Customer customer =
                (Customer) session.getAttribute("pendingCustomer");

        if (customer == null) {
            return "redirect:/customer/register";
        }

        model.addAttribute("email", customer.getEmail());

        return "customer-verify-otp";
    }

    @PostMapping("/customer/verify-otp")
    public String verifyOtp(@RequestParam String otp,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {

        Customer customer =
                (Customer) session.getAttribute("pendingCustomer");

        if (customer == null) {
            return "redirect:/customer/register";
        }

        boolean verified = customerService.verifyOtp(
                session, customer.getEmail(), otp
        );

        if (!verified) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Invalid OTP. Please try again."
            );

            return "redirect:/customer/verify-otp";
        }

        Customer savedCustomer =
                customerService.registerCustomer(customer);

        if (savedCustomer == null) {
            session.removeAttribute("pendingCustomer");

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Email already registered!"
            );

            return "redirect:/customer/register";
        }

        session.removeAttribute("pendingCustomer");

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Registration successful! You can now log in."
        );

        return "redirect:/customer/login";
    }

    // ==================== CUSTOMER LOGIN ====================

    @GetMapping("/customer/login")
    public String showLoginPage() {
        return "customer-login";
    }

    @PostMapping("/customer/login")
    public String loginCustomer(@RequestParam String email,
                                @RequestParam String password,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {

        Customer customer =
                customerService.loginCustomer(email, password);

        if (customer == null) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Invalid email or password, or email is not verified."
            );

            return "redirect:/customer/login";
        }

        session.setAttribute("loggedInCustomer", customer);

        return "redirect:/";
    }

    // ==================== CUSTOMER LOGOUT ====================

    @PostMapping("/customer/logout")
    public String logoutCustomer(HttpSession session,
                                 RedirectAttributes redirectAttributes) {

        session.removeAttribute("loggedInCustomer");

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "You have logged out successfully."
        );

        return "redirect:/";
    }

    // ==================== FORGOT PASSWORD ====================

    @GetMapping("/customer/forgot-password")
    public String showForgotPasswordPage() {
        return "forgot-password";
    }

    @PostMapping("/customer/forgot-password")
    public String sendPasswordResetOtp(
            @RequestParam String email,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        try {
            boolean sent =
                    customerService.sendPasswordResetOtp(email, session);

            if (!sent) {
                redirectAttributes.addFlashAttribute(
                        "errorMessage",
                        "No verified account found with this email address."
                );

                return "redirect:/customer/forgot-password";
            }

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "OTP sent successfully. Check your email."
            );

            return "redirect:/customer/reset-password";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "OTP email could not be sent. Please try again."
            );

            return "redirect:/customer/forgot-password";
        }
    }

    // ==================== RESET PASSWORD PAGE ====================

    @GetMapping("/customer/reset-password")
    public String showResetPasswordPage(HttpSession session) {

        if (session.getAttribute("resetPasswordOtp") == null
                || session.getAttribute("resetPasswordEmail") == null) {

            return "redirect:/customer/forgot-password";
        }

        return "reset-password";
    }

    // ==================== RESET PASSWORD ====================

    @PostMapping("/customer/reset-password")
    public String resetPassword(
            @RequestParam String otp,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (newPassword == null
                || newPassword.trim().isEmpty()) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Password cannot be empty."
            );

            return "redirect:/customer/reset-password";
        }

        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Passwords do not match."
            );

            return "redirect:/customer/reset-password";
        }

        boolean reset = customerService.resetPassword(
                session, otp, newPassword
        );

        if (!reset) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Invalid OTP or reset session expired. Please try again."
            );

            return "redirect:/customer/forgot-password";
        }

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Password reset successful! Please log in with your new password."
        );

        return "redirect:/customer/login";
    }
}