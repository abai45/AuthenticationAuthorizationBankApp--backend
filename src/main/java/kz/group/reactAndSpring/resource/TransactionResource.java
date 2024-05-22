package kz.group.reactAndSpring.resource;

import jakarta.servlet.http.HttpServletRequest;
import kz.group.reactAndSpring.domain.Response;
import kz.group.reactAndSpring.dto.TransactionRequestDto;
import kz.group.reactAndSpring.dto.UserDto;
import kz.group.reactAndSpring.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static java.util.Collections.emptyMap;
import static kz.group.reactAndSpring.utils.RequestUtils.getResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = {"/transactions"})
public class TransactionResource {
    private final TransactionService transactionService;

    @PostMapping("/credit")
    @PreAuthorize("hasAnyAuthority('transaction:create') or hasAnyRole('USER','ADMIN','SUPER_ADMIN')")
    public ResponseEntity<Response> credit(@AuthenticationPrincipal UserDto user, @RequestBody TransactionRequestDto transactionRequest, HttpServletRequest request) {
        transactionService.creditTransaction(transactionRequest.getSourcePhone(), transactionRequest.getDestPhone(), transactionRequest.getAmount());
        return ResponseEntity.ok(getResponse(request, emptyMap(), "Credit operation successful ended", HttpStatus.OK));
    }
    @GetMapping("/gettransactions")
    public ResponseEntity<Response> getTransactions(@AuthenticationPrincipal UserDto user, HttpServletRequest request) {
        var transactions = transactionService.getTransactions(user.getUserId());
        return ResponseEntity.ok(getResponse(request, emptyMap(), "Get transactions", HttpStatus.OK));
    }
}
