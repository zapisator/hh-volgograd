package com.example.hhvolgograd.persistance.db.repository;

import com.example.hhvolgograd.persistance.db.model.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfileRepository extends JpaRepository<Profile, Long> {


}
