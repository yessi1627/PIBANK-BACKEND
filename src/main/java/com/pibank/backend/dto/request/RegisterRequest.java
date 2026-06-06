package com.pibank.backend.dto.request;

import com.pibank.backend.domain.enums.DocumentType;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class RegisterRequest {

    @NotBlank(message = "El nombre es requerido")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    private String firstName;

    @NotBlank(message = "El apellido es requerido")
    @Size(min = 2, max = 100, message = "El apellido debe tener entre 2 y 100 caracteres")
    private String lastName;

    @NotBlank(message = "El email es requerido")
    @Email(message = "Formato de email inválido")
    private String email;

    @NotBlank(message = "El teléfono es requerido")
    @Pattern(regexp = "^[+]?[0-9]{10,15}$", message = "Formato de teléfono inválido")
    private String phoneNumber;

    @NotNull(message = "El tipo de documento es requerido")
    private DocumentType documentType;

    @NotBlank(message = "El número de documento es requerido")
    @Size(min = 6, max = 20, message = "El número de documento debe tener entre 6 y 20 caracteres")
    private String documentNumber;

    @Past(message = "La fecha de nacimiento debe ser en el pasado")
    private LocalDate birthDate;

    @Size(max = 255, message = "La dirección no puede superar 255 caracteres")
    private String address;

    @Size(max = 100, message = "La ciudad no puede superar 100 caracteres")
    private String city;

    @NotBlank(message = "La contraseña es requerida")
    @Size(min = 8, max = 100, message = "La contraseña debe tener entre 8 y 100 caracteres")
    @Pattern(
        regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).*$",
        message = "La contraseña debe contener al menos: una mayúscula, una minúscula, un número y un carácter especial (@#$%^&+=!)"
    )
    private String password;

    @NotBlank(message = "La confirmación de contraseña es requerida")
    private String confirmPassword;

    @NotNull(message = "Debe aceptar los términos y condiciones")
    @AssertTrue(message = "Debe aceptar los términos y condiciones para registrarse")
    private Boolean termsAccepted;
}
