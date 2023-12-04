package io.hoogland.anticalorieapi.repository;

import io.hoogland.anticalorieapi.model.ERole;
import io.hoogland.anticalorieapi.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByRole(ERole role);
}
