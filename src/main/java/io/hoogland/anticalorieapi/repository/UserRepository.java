package io.hoogland.anticalorieapi.repository;

import io.hoogland.anticalorieapi.model.Role;
import io.hoogland.anticalorieapi.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findUserByEmail(String email);

    List<User> findAllByRolesIn(Set<Role> roles);

    Boolean existsByEmail(String email);
}
