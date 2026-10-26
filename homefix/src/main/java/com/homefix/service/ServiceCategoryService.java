package com.homefix.service;
import com.homefix.dao.ServiceCategoryRepository;
import com.homefix.model.ServiceCategory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ServiceCategoryService {
    private final ServiceCategoryRepository repository;

    public ServiceCategoryService(ServiceCategoryRepository repository) {

        this.repository = repository;

    }
    public List<ServiceCategory> getAllCategories()
    {
        return repository.findAll();
    }
    public ServiceCategory createCategory(ServiceCategory category) {
       return repository.save(category);
    }
    public Optional<ServiceCategory> getCategoryById(int id) {
        // yahan repository ka method call karna hai
        return repository.findById(id);
    }
    public ServiceCategory updateCategory(int id, ServiceCategory updatedCategory) {

        Optional<ServiceCategory> existingCategory = repository.findById(id);

        if (existingCategory.isPresent()) {

            ServiceCategory category = existingCategory.get();

            category.setName(updatedCategory.getName());
            category.setDescription(updatedCategory.getDescription());
            category.setActive(updatedCategory.isActive());

            return repository.save(category);
        }

        return null;
    }
    public boolean deleteCategory(int id) {
        Optional<ServiceCategory> category = repository.findById(id);

        if (category.isPresent()) {
            repository.delete(category.get());
            return true;
        }

        return false;
    }
}
