package com.ap01.url_shortener.repository;

import com.ap01.url_shortener.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
}
