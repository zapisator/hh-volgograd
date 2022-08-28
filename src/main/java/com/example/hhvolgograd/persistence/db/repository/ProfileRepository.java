package com.example.hhvolgograd.persistence.db.repository;

import com.example.hhvolgograd.persistence.db.model.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfileRepository extends JpaRepository<Profile, Long> {


}
