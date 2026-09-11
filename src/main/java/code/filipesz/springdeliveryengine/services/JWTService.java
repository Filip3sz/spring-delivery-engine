package code.filipesz.springdeliveryengine.services;

import code.filipesz.springdeliveryengine.entities.Courier;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class JWTService {

    // Ważność sesji = 8 godzin (1000ms * 60s * 60m * 8h)
    private static final long EXPIRATION_TIME = 1000L * 60 * 60 * 8;
    private final SecretKey secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    public String generateToken(Courier courier) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("firstName", courier.getFirstName());
        claims.put("lastName", courier.getLastName());

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + EXPIRATION_TIME);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(courier.getId().toString())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(secretKey)
                .compact();
    }

    public UUID extractCourierId(String token) {
        String subject = extractClaims(token).getSubject();
        return UUID.fromString(subject);
    }

    public long getRemainingExpirationTime(String token) {
        Date expiration = extractClaims(token).getExpiration();
        long diff = expiration.getTime() - System.currentTimeMillis();
        return Math.max(0, diff);
    }

    private Claims extractClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}