package com.iot_ddaas.repository;

import com.iot_ddaas.IoTData;
import com.iot_ddaas.frontend.auth.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    User findByEmail(String email);
    User findByUsername (String username);


}

