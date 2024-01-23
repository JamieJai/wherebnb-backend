package goorm.wherebnb.config.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import goorm.wherebnb.config.auth.dto.response.UserDto;
import goorm.wherebnb.config.auth.dto.response.WishListDto;
import goorm.wherebnb.config.auth.filter.JwtAuthenticationFilter;
import goorm.wherebnb.config.auth.filter.JwtAuthorizationFilter;
import goorm.wherebnb.config.auth.jwt.JwtTokenProvider;
import goorm.wherebnb.config.auth.user.CustomUserDetails;
import goorm.wherebnb.config.auth.user.oauth2.CustomOAuth2UserService;
import goorm.wherebnb.config.auth.user.CustomUserDetailsService;
import goorm.wherebnb.domain.dao.User;
import goorm.wherebnb.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@EnableWebSecurity
@EnableMethodSecurity
@Configuration
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    protected SecurityFilterChain configure(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable) // 3. 폼 로그인 사용하지 않음 -> 폼 기반 로그인 방식에서 JWT토근 기반 로그인 방식을 사용해야 되기 때문
                .httpBasic(AbstractHttpConfigurer::disable)
                .cors(configurer -> configurer.configurationSource(corsConfigurationSource())) // 2. 크로스 오리진 정책에서 벗어남(모든요청 허용)
                .sessionManagement(configure -> configure.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 1. Stateless 서버(세션x)
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                        .successHandler(this::onAuthenticationSuccess)
                        .failureHandler(this::onAuthenticationFailure))
                .addFilter(new JwtAuthenticationFilter(jwtTokenProvider, userRepository, authenticationManager(customUserDetailsService), customUserDetailsService, "/auth/token"))
                .addFilterAfter(new JwtAuthorizationFilter(userRepository, jwtTokenProvider), UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                new AntPathRequestMatcher("/guest/**"),
                                new AntPathRequestMatcher("/trips/**"),
                                new AntPathRequestMatcher("/wishlists/**"),
                                new AntPathRequestMatcher("/hosting/**"),
                                new AntPathRequestMatcher("/host/**"),
                                new AntPathRequestMatcher("/calender-router/**")
//                                new AntPathRequestMatcher("/users/**")
                        ).hasRole("USER")
                        .anyRequest().permitAll());

        return http.build();
    }

    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        log.info("oauth 인증 성공했다");
        //토큰 발급 시작
        String token = jwtTokenProvider.createToken(authentication);
        String refresh = jwtTokenProvider.createRefreshToken(authentication);
        log.info(token);
        log.info(refresh);
        ObjectMapper om = new ObjectMapper();
        response.addHeader("Authorization", "Bearer " + token);
        response.addHeader("RefreshToken", "Bearer " + refresh);
        log.info("토큰도 다 만들어서 넣었어");
        String role = authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).findAny().orElse("");
        String userEmail = "";
//        Date expiration = jwtTokenProvider.getClaims(token).getExpiration();
        if(role.equals("ROLE_USER")){
            CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
            userEmail = customUserDetails.getUsername();
        }
        User user = customUserDetailsService.selcetUser(userEmail);
        user.setRefreshToken("Bearer " + refresh);
        userRepository.save(user);
        userRepository.save(user);
        UserDto userDto = new UserDto();
        userDto.setUserId(user.getUserId());
        userDto.setAccessToken("Bearer " + token);
        userDto.setRefreshToken(user.getRefreshToken());
        log.info("user전부 다 body에 response해줄게~");
        String result = om.registerModule(new JavaTimeModule()).writeValueAsString(userDto);
        response.getWriter().write(result);
    }

    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
    }

    public AuthenticationManager authenticationManager(UserDetailsService userDetailsService) {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userDetailsService);
        authenticationProvider.setPasswordEncoder(passwordEncoder());

        return new ProviderManager(authenticationProvider);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.addAllowedHeader("*");
        configuration.addExposedHeader("*");
        configuration.setAllowCredentials(true);
        configuration.setAllowedOrigins(List.of("http://localhost:3000"));
        configuration.setAllowedMethods(Arrays.asList("GET","POST", "PATCH", "PUT", "DELETE", "OPTIONS"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

}
