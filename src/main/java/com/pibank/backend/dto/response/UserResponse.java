package com.pibank.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.pibank.backend.domain.enums.DocumentType;
import com.pibank.backend.domain.enums.UserRole;
import com.pibank.backend.domain.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponse {

    private String id;
    private String firstName;
    private String lastName;
    private String fullName;
    private String email;
    private String phoneNumber;
    private DocumentType documentType;
    private String documentNumber;
    private LocalDate birthDate;
    private String address;
    private String city;
    private UserRole role;
    private UserStatus status;
    private Boolean mfaEnabled;
    private Boolean emailVerified;
    private Boolean termsAccepted;
    private LocalDateTime termsAcceptedAt;
    private LocalDateTime lastLoginAt;
    private String profilePictureUrl;
    private LocalDateTime createdAt;
}
