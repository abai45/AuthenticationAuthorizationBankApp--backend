package kz.group.reactAndSpring.resource;

import jakarta.servlet.http.HttpServletRequest;
import kz.group.reactAndSpring.domain.Response;
import kz.group.reactAndSpring.dto.bankDto.CreditDebitRequestDto;
import kz.group.reactAndSpring.dto.bankDto.TransactionId;
import kz.group.reactAndSpring.dto.bankDto.TransferRequestDto;
import kz.group.reactAndSpring.dto.UserDto;
import kz.group.reactAndSpring.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static java.util.Collections.emptyMap;
import static kz.group.reactAndSpring.utils.RequestUtils.getResponse;
import static org.springframework.http.HttpStatus.OK;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = {"/transactions"})
public class TransactionResource {
    private final TransactionService transactionService;

    @PostMapping("/transfer")
    @PreAuthorize("hasAnyAuthority('transaction:create') or hasAnyRole('USER','ADMIN','SUPER_ADMIN')")
    public ResponseEntity<Response> transfer(@AuthenticationPrincipal UserDto user, @RequestBody TransferRequestDto transactionRequest, HttpServletRequest request) {
        var transaction = transactionService.transferTransaction(transactionRequest.getSourceCardNumber(), transactionRequest.getDestCardNumber(), transactionRequest.getAmount());
        return ResponseEntity.ok(getResponse(request, Map.of("transaction", transaction), "Credit operation successful ended", OK));
    }

    @PostMapping("/debit")
    @PreAuthorize("hasAnyAuthority('transaction:create') or hasAnyRole('USER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Response> debit(@AuthenticationPrincipal UserDto user, @RequestBody CreditDebitRequestDto creditDebitRequest, HttpServletRequest request) {
        var debit = transactionService.debitTransaction(creditDebitRequest.getCardNumber(), creditDebitRequest.getAmount());
        return ResponseEntity.ok(getResponse(request, Map.of("debit", debit), "Debit operation successful ended", OK));
    }

    @PostMapping("/credit")
    @PreAuthorize("hasAnyAuthority('transaction:create') or hasAnyRole('USER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Response> credit(@AuthenticationPrincipal UserDto user, @RequestBody CreditDebitRequestDto creditDebitRequest, HttpServletRequest request) {
        var credit = transactionService.creditTransfer(creditDebitRequest.getCardNumber(), creditDebitRequest.getAmount());
        return ResponseEntity.ok(getResponse(request, Map.of("credit", credit), "Credit operation successful ended", OK));
    }

    @PostMapping("/delete")
    @PreAuthorize("hasAnyAuthority('transaction:delete') or hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Response> deleteTransaction(@AuthenticationPrincipal UserDto user, @RequestParam("transaction") String transactionId, HttpServletRequest request) {
        transactionService.deleteTransaction(transactionId);
        return ResponseEntity.ok(getResponse(request, emptyMap(),"Transaction deleted successfully", OK));
    }

    @GetMapping("/gettransactions")
    @PreAuthorize("hasAnyAuthority('transaction:read') or hasAnyRole('USER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Response> getTransactions(@AuthenticationPrincipal UserDto user, HttpServletRequest request) {
        var transactions = transactionService.getTransactions(user.getUserId());
        return ResponseEntity.ok(getResponse(request, Map.of("transactions", transactions), "Get transactions", OK));
    }
}
