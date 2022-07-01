package com.example.hhvolgograd.persistance.repository;

import com.example.hhvolgograd.persistance.entity.Phone;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PhoneRepository extends JpaRepository<Phone, Long> {
}
