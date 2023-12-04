package io.hoogland.anticalorieapi.model.response;

import lombok.Data;

import java.util.List;

@Data
public class RefreshTokenResponse {

    private String token;

    private String refreshToken;

    private final String type = "Bearer";

    public RefreshTokenResponse(String token, String refreshToken) {
        this.token = token;
        this.refreshToken = refreshToken;
    }
}
