package io.hoogland.anticalorieapi.controller.v1;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.hoogland.anticalorieapi.config.interceptor.RegisterRateLimitInterceptor;
import io.hoogland.anticalorieapi.exception.RefreshTokenException;
import io.hoogland.anticalorieapi.model.RefreshToken;
import io.hoogland.anticalorieapi.model.User;
import io.hoogland.anticalorieapi.model.UserDetailsImpl;
import io.hoogland.anticalorieapi.model.request.LoginRequest;
import io.hoogland.anticalorieapi.model.request.RefreshTokenRequest;
import io.hoogland.anticalorieapi.model.request.RegisterRequest;
import io.hoogland.anticalorieapi.model.response.AuthenticationResponse;
import io.hoogland.anticalorieapi.model.response.RefreshTokenResponse;
import io.hoogland.anticalorieapi.repository.RoleRepository;
import io.hoogland.anticalorieapi.repository.UserRepository;
import io.hoogland.anticalorieapi.service.RefreshTokenService;
import io.hoogland.anticalorieapi.service.UserService;
import io.hoogland.anticalorieapi.utils.JwtUtils;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private RegisterRateLimitInterceptor registerRateLimitInterceptor;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody @Valid RegisterRequest registerRequest, @RequestAttribute String identifier) {
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            registerRateLimitInterceptor.getBucket(identifier).addTokens(1);
            return ResponseEntity.badRequest().body("Username already in use");
        }
        boolean success = userService.saveUserFromRegisterRequest(registerRequest);
        if (success) {
            return ResponseEntity.ok("User registered successfully!");
        } else {
            return ResponseEntity.badRequest().body("Something went wrong");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody @Valid LoginRequest loginRequest) {
        Optional<User> user = userRepository.findUserByEmail(loginRequest.getEmail());
        if (user.isEmpty()) {
            return ResponseEntity.badRequest().body("Invalid email or password");
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());


        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getId());

        return ResponseEntity.ok(new AuthenticationResponse(userDetails.getId(),
                userDetails.getEmail(), roles, jwt, refreshToken.getToken()));
    }

    @PostMapping("/token/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody @Valid RefreshTokenRequest refreshTokenRequest) {
        String refreshToken = refreshTokenRequest.getRefreshToken();

        return refreshTokenService.findByToken(refreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    String token = jwtUtils.generateJwtToken(user.getEmail());
                    return ResponseEntity.ok(new RefreshTokenResponse(token, refreshToken));
                })
                .orElseThrow(() -> new RefreshTokenException(refreshToken,
                        "Invalid refresh token."));
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Map<String, String> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return errors;
    }
}
