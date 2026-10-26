package com.homefix.controller;

import com.homefix.model.ServiceEnquiry;
import com.homefix.service.ServiceEnquiryService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
public class AdminController {

    private final ServiceEnquiryService enquiryService;

    public AdminController(ServiceEnquiryService enquiryService) {
        this.enquiryService = enquiryService;
    }

    @GetMapping("/admin/enquiries")
    public String showAllEnquiries(
            Model model,
            HttpSession session) {

        Boolean loggedIn =
                (Boolean) session.getAttribute("adminLoggedIn");

        if (!Boolean.TRUE.equals(loggedIn)) {
            return "redirect:/admin/login";
        }

        model.addAttribute("enquiries",
                enquiryService.getAllEnquiries());

        model.addAttribute("totalEnquiries",
                enquiryService.getTotalEnquiries());

        model.addAttribute("pendingEnquiries",
                enquiryService.getPendingEnquiries());

        model.addAttribute("contactedEnquiries",
                enquiryService.getContactedEnquiries());

        model.addAttribute("completedEnquiries",
                enquiryService.getCompletedEnquiries());

        return "admin-enquiries";
    }
    @PostMapping("/admin/enquiries/update-status")
    public String updateEnquiryStatus(
            @RequestParam int id,
            @RequestParam String status,
            RedirectAttributes redirectAttributes,
            HttpSession session) {

        Boolean loggedIn =
                (Boolean) session.getAttribute("adminLoggedIn");

        if (!Boolean.TRUE.equals(loggedIn)) {
            return "redirect:/admin/login";
        }

        boolean updated =
                enquiryService.updateEnquiryStatus(id, status);

        if (updated) {
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Enquiry status updated successfully!"
            );
        } else {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Enquiry not found!"
            );
        }
        return "redirect:/admin/enquiries";
    }

    @GetMapping("/admin/login")
    public String showLoginPage()
    {
        return "admin-login";
    }
    @PostMapping("/admin/login")
    public String login( @RequestParam String username,@RequestParam String password,HttpSession session,Model model) {

        if ("admin".equals(username) && "HomeFix@123".equals(password)) {

            session.setAttribute("adminLoggedIn", true);

            return "redirect:/admin/enquiries";
        }
        model.addAttribute( "errorMessage","Invalid username or password!" );
        return "admin-login";
    }
    @GetMapping("/admin/logout")
    public String logout(HttpSession session) {

        session.invalidate();

        return "redirect:/admin/login";
    }
    @GetMapping("/admin/enquiries/{id}")
    public String showEnquiryDetails(
            @PathVariable int id,
            Model model,
            HttpSession session) {

        Boolean loggedIn =
                (Boolean) session.getAttribute("adminLoggedIn");

        if (!Boolean.TRUE.equals(loggedIn)) {
            return "redirect:/admin/login";
        }

        Optional<ServiceEnquiry> optionalEnquiry =
                enquiryService.getEnquiryById(id);

        if (optionalEnquiry.isEmpty()) {
            return "redirect:/admin/enquiries";
        }

        model.addAttribute("enquiry", optionalEnquiry.get());

        return "admin-enquiry-details";
    }
    @PostMapping("/admin/enquiries/delete")
    public String deleteEnquiry(
            @RequestParam int id,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (!Boolean.TRUE.equals(session.getAttribute("adminLoggedIn"))) {
            return "redirect:/admin/login";
        }

        boolean deleted = enquiryService.deleteEnquiry(id);

        if (deleted) {
            redirectAttributes.addFlashAttribute(
                    "successMessage", "Enquiry deleted successfully!");
        } else {
            redirectAttributes.addFlashAttribute(
                    "errorMessage", "Enquiry not found!");
        }

        return "redirect:/admin/enquiries";
    }
}