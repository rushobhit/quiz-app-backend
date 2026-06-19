package com.quizmicroservice.repository;

import com.quizmicroservice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;



import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

	Page<User> findByRoleIgnoreCase(String role, Pageable pageable);
	
    Optional<User> findByEmailIgnoreCase(String email);

    Optional<User> findByUsernameIgnoreCase(String username);

    Optional<User> findTopByEmailIgnoreCaseOrUsernameIgnoreCase(String email, String username);

    List<User> findByRoleIgnoreCase(String role);

    List<User> findByStatusIgnoreCase(String status);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByUsernameIgnoreCase(String username);
}