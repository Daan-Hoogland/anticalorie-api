package io.hoogland.anticalorieapi.model.response;

import io.hoogland.anticalorieapi.model.Role;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
public class AuthenticationResponse implements Serializable {

    private Long id;

    private String email;

    private List<String> roles;

    private String token;

    private String refreshToken;

    private final String type = "Bearer";

    public AuthenticationResponse(Long id, String email, List<String> roles, String token, String refreshToken) {
        this.id = id;
        this.email = email;
        this.roles = roles;
        this.token = token;
        this.refreshToken = refreshToken;
    }
}
