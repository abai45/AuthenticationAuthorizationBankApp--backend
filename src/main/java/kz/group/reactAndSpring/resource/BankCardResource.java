package kz.group.reactAndSpring.resource;

import jakarta.servlet.http.HttpServletRequest;
import kz.group.reactAndSpring.domain.Response;
import kz.group.reactAndSpring.dto.UserDto;
import kz.group.reactAndSpring.dto.bankDto.BankCardNameRequestDto;
import kz.group.reactAndSpring.service.BankCardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static kz.group.reactAndSpring.utils.RequestUtils.getResponse;
import static org.springframework.http.HttpStatus.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = {"/cards"})
public class BankCardResource {
    private final BankCardService bankCardService;

    @PostMapping("/create")
    public ResponseEntity<Response> createCard(@AuthenticationPrincipal UserDto user, @RequestBody BankCardNameRequestDto bankCardNameRequest, HttpServletRequest request) {
        var bankCard = bankCardService.createBankCard(user, bankCardNameRequest.getCardName());
        return ResponseEntity.ok(getResponse(request, Map.of("bankCard", bankCard), "Bank Cart created successfully", OK));
    }

    @PostMapping("/list")
    public ResponseEntity<Response> listCards(@AuthenticationPrincipal UserDto user, HttpServletRequest request) {
        var cards = bankCardService.getBankCard(user);
        return ResponseEntity.ok(getResponse(request, Map.of("cards", cards), "Bank cards retrieved successfully", OK));
    }

    @PostMapping("/getfull")
    public ResponseEntity<Response> getFullCard(@AuthenticationPrincipal UserDto user, @RequestBody BankCardNameRequestDto bankCardNameRequest, HttpServletRequest request) {
        var card = bankCardService.getFullCardInfo(user, bankCardNameRequest.getCardName());
        return ResponseEntity.ok(getResponse(request, Map.of("card", card), "Full info of your card retrieved successfully", OK));
    }
}
