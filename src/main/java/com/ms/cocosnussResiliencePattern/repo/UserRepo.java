package com.ms.cocosnussResiliencePattern.repo;

import com.ms.cocosnussResiliencePattern.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepo extends JpaRepository<User, Long> {
}
