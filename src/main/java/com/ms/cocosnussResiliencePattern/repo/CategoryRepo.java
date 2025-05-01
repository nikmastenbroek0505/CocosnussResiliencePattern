package com.ms.cocosnussResiliencePattern.repo;

import com.ms.cocosnussResiliencePattern.models.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepo extends JpaRepository<Category, Long> {
}
