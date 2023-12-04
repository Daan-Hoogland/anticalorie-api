package io.hoogland.anticalorieapi.service;

import io.hoogland.anticalorieapi.model.User;
import io.hoogland.anticalorieapi.model.UserDetailsImpl;
import io.hoogland.anticalorieapi.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findUserByEmail(email).orElseThrow(() -> new UsernameNotFoundException("No user with email: " + email));

        return UserDetailsImpl.build(user);
    }
}
