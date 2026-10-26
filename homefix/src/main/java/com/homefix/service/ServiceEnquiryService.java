package com.homefix.service;

import com.homefix.dao.ServiceEnquiryRepository;
import com.homefix.model.ServiceEnquiry;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ServiceEnquiryService {

    private final ServiceEnquiryRepository repository;

    public ServiceEnquiryService(ServiceEnquiryRepository repository) {
        this.repository = repository;
    }

    public ServiceEnquiry saveEnquiry(ServiceEnquiry enquiry) {
        return repository.save(enquiry);
    }
    public List<ServiceEnquiry> getAllEnquiries() {
        return repository.findAll();
    }
    public List<ServiceEnquiry> getCustomerEnquiries(String email) {
        return repository.findByEmailIgnoreCase(email);
    }
    public boolean updateEnquiryStatus(int id, String status) {

        Optional<ServiceEnquiry> optionalEnquiry =repository.findById(id);
        if (optionalEnquiry.isPresent()) {
            ServiceEnquiry enquiry = optionalEnquiry.get();
            enquiry.setStatus(status);
            repository.save(enquiry);
            return true;
        }
        return false;
    }
    public long getTotalEnquiries() {
        return repository.count();
    }

    public long getPendingEnquiries() {
        return repository.countByStatus("Pending");
    }

    public long getContactedEnquiries() {
        return repository.countByStatus("Contacted");
    }

    public long getCompletedEnquiries() {
        return repository.countByStatus("Completed");
    }
    public Optional<ServiceEnquiry> getEnquiryById(int id) {
        return repository.findById(id);
    }
    public boolean deleteEnquiry(int id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return true;
        }

        return false;
    }
    public boolean cancelCustomerEnquiry(int id, String email) {

        Optional<ServiceEnquiry> optionalEnquiry =
                repository.findById(id);

        if (optionalEnquiry.isEmpty()) {
            return false;
        }

        ServiceEnquiry enquiry = optionalEnquiry.get();

        if (!enquiry.getEmail().equalsIgnoreCase(email)
                || !"Pending".equalsIgnoreCase(enquiry.getStatus())) {
            return false;
        }

        enquiry.setStatus("Cancelled");
        repository.save(enquiry);

        return true;
    }
}