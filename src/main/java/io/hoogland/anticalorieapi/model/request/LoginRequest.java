package io.hoogland.anticalorieapi.model.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

@Data
public class LoginRequest implements Serializable {

    @Email
    @NotBlank
    @Size(max=100)
    private String email;

    @NotBlank
    @Size(min=6, max=40)
    private String password;
}
