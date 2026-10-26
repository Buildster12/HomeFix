package com.homefix.controller;

import com.homefix.dao.ServiceCategoryRepository;
import com.homefix.model.ServiceCategory;
import com.homefix.service.ServiceCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;


@RestController

public class ServiceCategoryController {

    private final ServiceCategoryService service;

    public ServiceCategoryController(ServiceCategoryService service) {
        this.service = service;
    }

    @GetMapping("/api/categories")
    public List<ServiceCategory> getAllCategories() {
        return service.getAllCategories();
    }
    @PostMapping("/api/categories")
    public ServiceCategory createCategory(@RequestBody ServiceCategory category) {
        return service.createCategory(category);
    }
    @GetMapping("/api/categories/{id}")
    public ResponseEntity<ServiceCategory> getCategoryById(
            @PathVariable int id) {

        Optional<ServiceCategory> category =
                service.getCategoryById(id);

        if (category.isPresent()) {
            return ResponseEntity.ok(category.get());
        }

        return ResponseEntity.notFound().build();
    }
    @PutMapping("/api/categories/{id}")
    public ResponseEntity<ServiceCategory> updateCategory(
            @PathVariable int id,
            @RequestBody ServiceCategory updatedCategory) {

        ServiceCategory category = service.updateCategory(id, updatedCategory);

        if (category != null) {
            return ResponseEntity.ok(category);
        }

        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/api/categories/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable int id) {

        boolean deleted = service.deleteCategory(id);

        if (deleted) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.notFound().build();
    }
}
