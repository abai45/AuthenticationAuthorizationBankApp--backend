package kz.group.reactAndSpring.resource;

import jakarta.servlet.http.HttpServletRequest;
import kz.group.reactAndSpring.domain.Response;
import kz.group.reactAndSpring.dto.UserDto;
import kz.group.reactAndSpring.dto.bankDto.BankCardNameRequestDto;
import kz.group.reactAndSpring.dto.bankDto.SetLimitRequestDto;
import kz.group.reactAndSpring.service.BankCardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static java.util.Collections.emptyMap;
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
        return ResponseEntity.ok(getResponse(request, Map.of("bankCard", bankCard), "Bank Card created successfully", OK));
    }

    @PostMapping("/delete")
    public ResponseEntity<Response> deleteCard(@AuthenticationPrincipal UserDto user, @RequestParam("cardname") String cardName, HttpServletRequest request) {
        bankCardService.deleteBankCard(user, cardName);
        return ResponseEntity.ok(getResponse(request, emptyMap(), "Bank Card deleted successfully", OK));
    }

    @GetMapping("/list")
    public ResponseEntity<Response> listCards(@AuthenticationPrincipal UserDto user, HttpServletRequest request) {
        var cards = bankCardService.getBankCard(user);
        return ResponseEntity.ok(getResponse(request, Map.of("cards", cards), "Bank cards retrieved successfully", OK));
    }

    @GetMapping("/getfull")
    public ResponseEntity<Response> getFullCardInfo(@AuthenticationPrincipal UserDto user, @RequestParam("cardname") String cardName, HttpServletRequest request) {
        var bankCard = bankCardService.getFullCardInfo(user, cardName);
        return ResponseEntity.ok().body(getResponse(request,Map.of("bankCard", bankCard), "Photo updated successfully", OK));
    }

    @PostMapping("/setlimit")
    public ResponseEntity<Response> setLimit(@AuthenticationPrincipal UserDto user, @RequestBody SetLimitRequestDto setLimitRequest, HttpServletRequest request) {
        bankCardService.setLimitToCard(user, setLimitRequest.getCardName(), setLimitRequest.getLimit());
        return ResponseEntity.ok(getResponse(request, emptyMap(), "Limit updated successfully", OK));
    }
}
