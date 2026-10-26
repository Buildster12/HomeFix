package com.homefix.service;

import com.homefix.dao.CustomerDao;
import com.homefix.model.Customer;

import jakarta.servlet.http.HttpSession;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class CustomerService {

    private final CustomerDao customerDao;
    private final JavaMailSender mailSender;

    public CustomerService(CustomerDao customerDao,
                           JavaMailSender mailSender) {
        this.customerDao = customerDao;
        this.mailSender = mailSender;
    }

    // Check if email is already registered
    public boolean isEmailRegistered(String email) {
        return customerDao.existsByEmail(email);
    }

    // Generate six-digit OTP
    public String generateOtp() {
        int otp = 100000 + new Random().nextInt(900000);
        return String.valueOf(otp);
    }

    // Send OTP email
    public void sendOtpEmail(String email, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(email);
        message.setSubject("HomeFix - Verification OTP");
        message.setText(
                "Hello!\n\n"
                        + "Your HomeFix OTP is: " + otp
                        + "\n\nUse this code to complete your verification "
                        + "or password reset."
                        + "\n\nHomeFix Team"
        );

        mailSender.send(message);
    }

    // Store registration OTP in session
    public void storeOtpInSession(HttpSession session, String email) {
        String otp = generateOtp();

        session.setAttribute("customerOtp", otp);
        session.setAttribute("customerOtpEmail", email);

        sendOtpEmail(email, otp);
    }

    // Verify registration OTP
    public boolean verifyOtp(HttpSession session,
                             String email,
                             String enteredOtp) {

        String storedOtp =
                (String) session.getAttribute("customerOtp");

        String storedEmail =
                (String) session.getAttribute("customerOtpEmail");

        if (storedOtp != null
                && storedEmail != null
                && enteredOtp != null
                && storedEmail.equalsIgnoreCase(email)
                && storedOtp.equals(enteredOtp)) {

            session.removeAttribute("customerOtp");
            session.removeAttribute("customerOtpEmail");

            return true;
        }

        return false;
    }

    // Register customer
    public Customer registerCustomer(Customer customer) {
        if (customerDao.existsByEmail(customer.getEmail())) {
            return null;
        }

        customer.setEmailVerified(true);

        return customerDao.save(customer);
    }

    // Customer login
    public Customer loginCustomer(String email, String password) {
        Customer customer =
                customerDao.findByEmail(email).orElse(null);

        if (customer == null) {
            return null;
        }

        if (!customer.isEmailVerified()) {
            return null;
        }

        if (customer.getPassword().equals(password)) {
            return customer;
        }

        return null;
    }

    // Send password-reset OTP
    public boolean sendPasswordResetOtp(String email,
                                        HttpSession session) {

        Customer customer =
                customerDao.findByEmail(email).orElse(null);

        if (customer == null || !customer.isEmailVerified()) {
            return false;
        }

        String otp = generateOtp();

        session.setAttribute("resetPasswordOtp", otp);
        session.setAttribute("resetPasswordEmail",
                customer.getEmail());

        sendOtpEmail(customer.getEmail(), otp);

        return true;
    }

    // Verify password-reset OTP
    public boolean verifyPasswordResetOtp(HttpSession session,
                                          String enteredOtp) {

        String storedOtp =
                (String) session.getAttribute("resetPasswordOtp");

        return storedOtp != null
                && enteredOtp != null
                && storedOtp.equals(enteredOtp);
    }

    // Reset customer password
    public boolean resetPassword(HttpSession session,
                                 String enteredOtp,
                                 String newPassword) {

        String storedOtp =
                (String) session.getAttribute("resetPasswordOtp");

        String email =
                (String) session.getAttribute("resetPasswordEmail");

        if (storedOtp == null
                || email == null
                || enteredOtp == null
                || !storedOtp.equals(enteredOtp)) {
            return false;
        }

        Customer customer =
                customerDao.findByEmail(email).orElse(null);

        if (customer == null || !customer.isEmailVerified()) {
            return false;
        }

        if (newPassword == null || newPassword.trim().isEmpty()) {
            return false;
        }

        customer.setPassword(newPassword);
        customerDao.save(customer);

        session.removeAttribute("resetPasswordOtp");
        session.removeAttribute("resetPasswordEmail");

        return true;
    }
}