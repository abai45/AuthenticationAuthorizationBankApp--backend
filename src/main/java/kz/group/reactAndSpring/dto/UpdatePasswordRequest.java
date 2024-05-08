package kz.group.reactAndSpring.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdatePasswordRequest {
    @NotEmpty(message = "Password cannot be empty or null")
    private String password;
    @NotEmpty(message = "New password cannot be empty or null")
    private String newPassword;
    @NotEmpty(message = "Confirm new password be empty or null")
    private String confirmNewPassword;
}
