package com.example.hhvolgograd.persistance.repository;

import com.example.hhvolgograd.persistance.model.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfileRepository extends JpaRepository<Profile, Long> {


}
