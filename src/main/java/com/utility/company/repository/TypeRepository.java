package com.utility.company.repository;

import com.utility.company.model.Type;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TypeRepository extends JpaRepository<Type, UUID> {

    Page<Type> findAll(Pageable pageable);

    List<Type> findAll();
    Type findOneByTextIgnoreCase(String text);
}
