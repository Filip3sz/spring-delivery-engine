package code.filipesz.springdeliveryengine.services;

import code.filipesz.springdeliveryengine.dto.CodeRequest;
import code.filipesz.springdeliveryengine.dto.CodeResponse;
import code.filipesz.springdeliveryengine.entities.Code;
import code.filipesz.springdeliveryengine.repositories.CodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CodeService {

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();
    private final CodeRepository codeRepository;

    @Transactional
    public Code createCode(CodeRequest request) {
        String codeString;

        if (request.code() != null && !request.code().isBlank()) {
            codeString = request.code().trim().toUpperCase();
            if (codeRepository.existsById(codeString)) {
                throw new IllegalArgumentException("Kod rabatowy " + codeString + " już istnieje.");
            }
        } else {
            codeString = generateUniqueRandomCode();
        }

        Code code = Code.builder()
                .code(codeString)
                .discount(request.discount())
                .usesCount(request.usesCount())
                .build();

        return codeRepository.save(code);
    }

    public CodeResponse checkAvailability(String code) {
        String cleanCode = code.trim().toUpperCase();

        Code promoCode = codeRepository.findById(cleanCode)
                .orElseThrow(() -> new IllegalArgumentException("Kod rabatowy " + cleanCode + " nie istnieje."));

        if (promoCode.getUsesCount() <= 0) {
            throw new IllegalStateException("Kod rabatowy " + cleanCode + " został już w pełni wykorzystany.");
        }

        return new CodeResponse(promoCode.getCode(), promoCode.getDiscount(), promoCode.getUsesCount());
    }

    private String generateUniqueRandomCode() {
        String randomCode;
        do {
            StringBuilder sb = new StringBuilder(10);
            for (int i = 0; i < 10; i++) {
                sb.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
            }
            randomCode = sb.toString();
        } while (codeRepository.existsById(randomCode));

        return randomCode;
    }
}