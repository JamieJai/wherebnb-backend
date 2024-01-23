package goorm.wherebnb.api;

import goorm.wherebnb.config.auth.dto.request.SignUpRequestDto;
import goorm.wherebnb.domain.dao.User;
import goorm.wherebnb.repository.UserRepository;
import goorm.wherebnb.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class LoginApiController {
    private final UserRepository userRepository;
    private final UserService userService;

    @GetMapping("/")
    public String start() {
        System.out.println("hello");
        return "/에 접속하였습니다.";
    }

    @GetMapping("/hosting/12")
    public String start12() {
        System.out.println("hello12");
        return "/hosting/12에 접속하였습니다";
    }

    @GetMapping("/users/{userId}")
    public User users(@PathVariable("userId") Long userId) {
        User userByUserId = userRepository.findUserByUserId(userId);
        return userByUserId;
    }

    @GetMapping("/auth/refresh")
    public String refreshToken() {
        return "리프레시 토큰 발급용 URL API End-point";
    }

    @PostMapping("/users")
    public ResponseEntity<?> signup(@RequestBody SignUpRequestDto signUpRequestDto) {
        userService.saveUser(signUpRequestDto);
        return ResponseEntity.ok().build();
    }
}
// 이상하게 보이지만 꼭 필요한 api입니다 지우지 말아주세요 ㅜㅜ