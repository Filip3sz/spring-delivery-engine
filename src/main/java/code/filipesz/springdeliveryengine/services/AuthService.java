package code.filipesz.springdeliveryengine.services;

import code.filipesz.springdeliveryengine.dto.AuthResponse;
import code.filipesz.springdeliveryengine.dto.OtpRequest;
import code.filipesz.springdeliveryengine.dto.OtpVerificationRequest;
import code.filipesz.springdeliveryengine.entities.Courier;
import code.filipesz.springdeliveryengine.entities.CourierStatus;
import code.filipesz.springdeliveryengine.repositories.CourierRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AuthService {

    private static final String REDIS_OTP_PREFIX = "otp:";
    private static final String REDIS_BLACKLIST_PREFIX = "blacklisted_token:";
    private static final String REDIS_ACTIVE_TOKEN_PREFIX = "active_token:";
    private final CourierRepository courierRepository;
    private final StringRedisTemplate redisTemplate;
    private final JWTService jwtService;
    private final SecureRandom secureRandom = new SecureRandom();
    // private final JavaMailSender javaMailSender;

    public String requestOtp(OtpRequest request) {
        String cleanEmail = request.email().trim().toLowerCase();

        Courier courier = courierRepository.findByEmail(cleanEmail)
                .orElseThrow(() -> new IllegalArgumentException("Kurier o podanym adresie email nie istnieje w systemie."));

        int randomCode = 100000 + secureRandom.nextInt(900000);
        String otpCode = String.valueOf(randomCode);

        String redisKey = REDIS_OTP_PREFIX + cleanEmail;
        redisTemplate.opsForValue().set(redisKey, otpCode, 5, TimeUnit.MINUTES);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("company@gmail.com");
        message.setTo(request.email());
        message.setSubject("Jednorazowy kod dla twojego konta kurierskiego");
        message.setText("Twój jednorazowy kod dostępu do aplikacji kurierskiej to: " + otpCode);
        // mailSender.send(message);

        log.info("Kurier ID: {} wysłał żądanie kodu OTP na adres {}", courier.getId(), cleanEmail);
        return otpCode;
    }

    @Transactional
    public AuthResponse verifyOtp(OtpVerificationRequest request) {
        String cleanEmail = request.email().trim().toLowerCase();
        String redisKey = REDIS_OTP_PREFIX + cleanEmail;

        String savedCode = redisTemplate.opsForValue().get(redisKey);

        if (savedCode == null) {
            throw new IllegalStateException("Kod OTP wygasł lub nie został wygenerowany. Poproś o nowy kod.");
        }

        if (!savedCode.equals(request.code().trim())) {
            throw new IllegalArgumentException("Podany kod OTP jest nieprawidłowy.");
        }

        redisTemplate.delete(redisKey);

        Courier courier = courierRepository.findByEmail(cleanEmail)
                .orElseThrow(() -> new IllegalArgumentException("Błąd krytyczny: Kurier nagle zniknął z bazy danych."));

        String activeTokenKey = REDIS_ACTIVE_TOKEN_PREFIX + courier.getId();
        String oldToken = redisTemplate.opsForValue().get(activeTokenKey);

        if (oldToken != null) {
            long remainingTime = jwtService.getRemainingExpirationTime(oldToken);
            if (remainingTime > 0) {
                redisTemplate.opsForValue().set(
                        REDIS_BLACKLIST_PREFIX + oldToken,
                        "true",
                        remainingTime,
                        TimeUnit.MILLISECONDS
                );
                log.info("Poprzednia sesja dla kuriera ID: {} została unieważniona.", courier.getId());
            }
        }

        courier.setStatus(CourierStatus.AVAILABLE);
        String newToken = jwtService.generateToken(courier);

        redisTemplate.opsForValue().set(activeTokenKey, newToken, 8, TimeUnit.HOURS);

        log.info("Kurier o numerze ID: {} rozpoczął nową sesję.", courier.getId());
        return new AuthResponse(courier, newToken);
    }

    @Transactional
    public void logout(String token) {
        UUID courierId = jwtService.extractCourierId(token);

        Courier courier = courierRepository.findById(courierId)
                .orElseThrow(() -> new IllegalArgumentException("Kurier o podanym ID nie istnieje."));

        courier.setStatus(CourierStatus.OFFLINE);

        String activeTokenKey = REDIS_ACTIVE_TOKEN_PREFIX + courierId;
        redisTemplate.delete(activeTokenKey);

        long remainingTime = jwtService.getRemainingExpirationTime(token);
        if (remainingTime > 0) {
            redisTemplate.opsForValue().set(
                    REDIS_BLACKLIST_PREFIX + token,
                    "true",
                    remainingTime,
                    TimeUnit.MILLISECONDS
            );
        }

        log.info("Kurier ID: {} wylogowany. Token został zblacklistowany.", courierId);
    }

    public boolean isTokenBlacklisted(String token) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(REDIS_BLACKLIST_PREFIX + token));
    }
}