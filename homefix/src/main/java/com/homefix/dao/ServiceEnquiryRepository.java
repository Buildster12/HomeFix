package com.homefix.dao;

import com.homefix.model.ServiceEnquiry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServiceEnquiryRepository
        extends JpaRepository<ServiceEnquiry, Integer> {

    long countByStatus(String status);
    List<ServiceEnquiry> findByEmailIgnoreCase(String email);
}