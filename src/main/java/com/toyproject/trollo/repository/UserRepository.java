package com.toyproject.trollo.repository;

import com.toyproject.trollo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
