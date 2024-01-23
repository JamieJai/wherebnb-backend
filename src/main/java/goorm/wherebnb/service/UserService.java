package goorm.wherebnb.service;

import goorm.wherebnb.config.auth.dto.request.SignUpRequestDto;
import goorm.wherebnb.domain.dao.AuthProvider;
import goorm.wherebnb.domain.dao.User;
import goorm.wherebnb.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;


    public void saveUser(SignUpRequestDto signUpRequestDto) {
        User user = User.builder()
                .email(signUpRequestDto.getEmail())
                .name(signUpRequestDto.getUserName())
                .provider(AuthProvider.local)
                .password(passwordEncoder.encode(signUpRequestDto.getPassword())).build();
        userRepository.save(user);
    }
}
