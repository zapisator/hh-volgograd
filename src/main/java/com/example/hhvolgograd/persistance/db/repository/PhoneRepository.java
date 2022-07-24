package com.example.hhvolgograd.persistance.db.repository;

import com.example.hhvolgograd.persistance.db.model.Phone;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PhoneRepository extends JpaRepository<Phone, Long> {
}
