package code.filipesz.springdeliveryengine.controllers;

import code.filipesz.springdeliveryengine.dto.CodeRequest;
import code.filipesz.springdeliveryengine.dto.CodeResponse;
import code.filipesz.springdeliveryengine.entities.Code;
import code.filipesz.springdeliveryengine.services.CodeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/codes")
@RequiredArgsConstructor
public class CodeController {

    private final CodeService codeService;

    @PostMapping
    public ResponseEntity<Code> createCode(@Valid @RequestBody CodeRequest request) {
        return new ResponseEntity<>(codeService.createCode(request), HttpStatus.CREATED);
    }

    @GetMapping("/{code}")
    public ResponseEntity<CodeResponse> checkAvailability(@PathVariable String code) {
        return ResponseEntity.ok(codeService.checkAvailability(code));
    }
}