package com.ms.cocosnussResiliencePattern.repo;

import com.ms.cocosnussResiliencePattern.models.Adress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdressRepo extends JpaRepository<Adress, Long> {
}
