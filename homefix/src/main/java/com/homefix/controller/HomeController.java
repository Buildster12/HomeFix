package com.homefix.controller;

import com.homefix.service.ServiceCategoryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final ServiceCategoryService service;

    public HomeController(ServiceCategoryService service) {
        this.service = service;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("categories", service.getAllCategories());
        return "index";
    }
}