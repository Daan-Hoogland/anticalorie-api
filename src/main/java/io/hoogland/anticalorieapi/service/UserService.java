package io.hoogland.anticalorieapi.service;

import io.hoogland.anticalorieapi.model.ERole;
import io.hoogland.anticalorieapi.model.request.RegisterRequest;
import io.hoogland.anticalorieapi.model.Role;
import io.hoogland.anticalorieapi.model.User;
import io.hoogland.anticalorieapi.repository.RoleRepository;
import io.hoogland.anticalorieapi.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder encoder;

    public boolean saveUserFromRegisterRequest(RegisterRequest registerRequest) {
        User user = new User();
        user.setEmail(registerRequest.getEmail());
        user.setPassword(encoder.encode(registerRequest.getPassword()));
        Set<Role> roles = new HashSet<>();
        Role role = roleRepository.findByRole(ERole.USER).orElseThrow(() -> new RuntimeException("Role not found"));

        roles.add(role);
        user.setRoles(roles);

        user.setIsEnabled(true);
        user.setIsLocked(false);

        return userRepository.save(user).getId() != null;
    }
}
