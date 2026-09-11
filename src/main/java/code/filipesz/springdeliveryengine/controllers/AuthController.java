package code.filipesz.springdeliveryengine.controllers;

import code.filipesz.springdeliveryengine.dto.AuthResponse;
import code.filipesz.springdeliveryengine.dto.OtpRequest;
import code.filipesz.springdeliveryengine.dto.OtpVerificationRequest;
import code.filipesz.springdeliveryengine.services.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login/request")
    public ResponseEntity<String> requestOtp(@Valid @RequestBody OtpRequest request) {
        return ResponseEntity.ok(authService.requestOtp(request));
    }

    @PostMapping("/login/verify")
    public ResponseEntity<AuthResponse> verifyOtp(@Valid @RequestBody OtpVerificationRequest request) {
        AuthResponse authResponse = authService.verifyOtp(request);

        String jwtToken = authResponse.token();

        ResponseCookie cookie = ResponseCookie.from("jwt_token", jwtToken)
                .httpOnly(true) // Zabezpieczenie przed kradzieżą przez JS (XSS)
                .secure(false) // Na produkcji dla HTTPS zmieniamy na true (localhost = false)
                .path("/") // Dostępne dla całej aplikacji /api
                .maxAge(8 * 3600) // Ważność: 8 godzin
                .sameSite("Lax") // Ochrona przed CSRF
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(authResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        String token = extractToken(request);

        if (token != null) {
            authService.logout(token);
        }

        ResponseCookie deleteCookie = ResponseCookie.from("jwt_token", "")
                .httpOnly(true)
                .path("/")
                .maxAge(0)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, deleteCookie.toString())
                .build();
    }

    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("jwt_token".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}