package com.example.backend.foundation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.backend.entity.UserRole;

public interface UserRoleRepository extends JpaRepository<UserRole, Long> {

}
