package com.pipemasters.server.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AuthenticationRequestDto {
    @NotBlank(message = "Username cannot be blank")
    private String username;

    @NotBlank(message = "Password cannot be blank")
    @Size(min = 8, max = 32, message = "Password must be between 8 and 32 characters long")
    private String password;

    public AuthenticationRequestDto(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public AuthenticationRequestDto() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
