package com.tuition.new_tuition.repository;

import com.tuition.new_tuition.entity.AppUser;
import com.tuition.new_tuition.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByEmail(String email);

    Optional<AppUser> findByEmailIgnoreCase(String email);

    List<AppUser> findByRole(Role role);
}