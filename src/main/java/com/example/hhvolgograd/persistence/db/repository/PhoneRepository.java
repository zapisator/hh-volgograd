package com.example.hhvolgograd.persistence.db.repository;

import com.example.hhvolgograd.persistence.db.model.Phone;
import com.example.hhvolgograd.persistence.db.repository.custom.NativeSqlPhoneRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PhoneRepository extends JpaRepository<Phone, Long>, NativeSqlPhoneRepository {

}
