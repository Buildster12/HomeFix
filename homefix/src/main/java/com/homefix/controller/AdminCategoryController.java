package com.homefix.controller;

import com.homefix.model.ServiceCategory;
import com.homefix.service.ServiceCategoryService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
public class AdminCategoryController {

    private final ServiceCategoryService service;

    public AdminCategoryController(ServiceCategoryService service) {
        this.service = service;
    }

    @GetMapping("/admin/categories")
    public String showCategories(HttpSession session, Model model) {

        if (!Boolean.TRUE.equals(session.getAttribute("adminLoggedIn"))) {
            return "redirect:/admin/login";
        }

        model.addAttribute("categories", service.getAllCategories());

        return "admin-categories";
    }

    @GetMapping("/admin/categories/add")
    public String showAddCategoryForm(
            HttpSession session,
            Model model) {

        if (!Boolean.TRUE.equals(session.getAttribute("adminLoggedIn"))) {
            return "redirect:/admin/login";
        }

        model.addAttribute("category", new ServiceCategory());
        model.addAttribute("editMode", false);

        return "admin-category-form";
    }

    @PostMapping("/admin/categories/add")
    public String addCategory(
            @ModelAttribute ServiceCategory category,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (!Boolean.TRUE.equals(session.getAttribute("adminLoggedIn"))) {
            return "redirect:/admin/login";
        }

        service.createCategory(category);

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Service category added successfully!"
        );

        return "redirect:/admin/categories";
    }

    @GetMapping("/admin/categories/edit/{id}")
    public String showEditCategoryForm(
            @PathVariable int id,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (!Boolean.TRUE.equals(session.getAttribute("adminLoggedIn"))) {
            return "redirect:/admin/login";
        }

        Optional<ServiceCategory> optionalCategory =
                service.getCategoryById(id);

        if (optionalCategory.isEmpty()) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Category not found!"
            );

            return "redirect:/admin/categories";
        }

        model.addAttribute("category", optionalCategory.get());
        model.addAttribute("editMode", true);

        return "admin-category-form";
    }

    @PostMapping("/admin/categories/update")
    public String updateCategory(
            @RequestParam int id,
            @ModelAttribute ServiceCategory category,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (!Boolean.TRUE.equals(session.getAttribute("adminLoggedIn"))) {
            return "redirect:/admin/login";
        }

        ServiceCategory updatedCategory =
                service.updateCategory(id, category);

        if (updatedCategory != null) {
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Category updated successfully!"
            );
        } else {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Category not found!"
            );
        }

        return "redirect:/admin/categories";
    }
    @PostMapping("/admin/categories/delete")
    public String deleteCategory(
            @RequestParam int id,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (!Boolean.TRUE.equals(session.getAttribute("adminLoggedIn"))) {
            return "redirect:/admin/login";
        }

        boolean deleted = service.deleteCategory(id);

        if (deleted) {
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Category deleted successfully!"
            );
        } else {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Category not found!"
            );
        }

        return "redirect:/admin/categories";
    }
}