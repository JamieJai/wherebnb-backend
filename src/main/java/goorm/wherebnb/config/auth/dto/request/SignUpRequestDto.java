package goorm.wherebnb.config.auth.dto.request;

import lombok.Data;

@Data
public class SignUpRequestDto {

    private String email;
    private String userName;
    private String password;
}
