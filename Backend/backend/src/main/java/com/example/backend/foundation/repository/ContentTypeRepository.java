package com.example.backend.foundation.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.backend.entity.ContentType;

public interface ContentTypeRepository extends JpaRepository<ContentType, Long> {

}
