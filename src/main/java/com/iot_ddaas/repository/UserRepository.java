package com.iot_ddaas.repository;

import com.iot_ddaas.frontend.auth.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    User findByEmail(String email);
}
