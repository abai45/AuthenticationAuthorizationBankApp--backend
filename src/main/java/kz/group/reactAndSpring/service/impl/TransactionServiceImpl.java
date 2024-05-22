package kz.group.reactAndSpring.service.impl;

import jakarta.transaction.Transactional;
import kz.group.reactAndSpring.dto.TransactionDto;
import kz.group.reactAndSpring.entity.TransactionEntity;
import kz.group.reactAndSpring.entity.UserEntity;
import kz.group.reactAndSpring.exception.ApiException;
import kz.group.reactAndSpring.repository.TransactionRepository;
import kz.group.reactAndSpring.repository.UserRepository;
import kz.group.reactAndSpring.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;


@Service
@Transactional(rollbackOn = Exception.class)
@RequiredArgsConstructor
@Slf4j
public class TransactionServiceImpl implements TransactionService {
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    @Override
    public void creditTransaction(String sourcePhone, String destPhone, String amount) {
        var userSource = getUserEntityByPhoneNumber(sourcePhone);
        var userDest = getUserEntityByPhoneNumber(destPhone);
        var userAmount = new BigDecimal(amount);
        userSource.setBalance(userSource.getBalance().divide(userAmount));
        userDest.setBalance(userDest.getBalance().add(userAmount));

    }

    @Override
    public Collection<TransactionDto> getTransactions(String userId) {
        Optional<UserEntity> userEntity = userRepository.findUserByUserId(userId);
        List<TransactionEntity> transactionEntities = transactionRepository.getTransactionEntitiesByOwner(userEntity.orElse(null));
           
    }

    private TransactionDto convertToDto(TransactionEntity transactionEntity) {
        return TransactionDto.builder()
                .id(transactionEntity.getId())
                .transactionId(transactionEntity.getTransactionId())
                .transactionType(transactionEntity.getTransactionType())
                .transactionDate(transactionEntity.getTransactionDate())
                .amount(transactionEntity.getAmount())
                .status(transactionEntity.getStatus())
                .build();
    }

    private UserEntity getUserEntityByPhoneNumber(String phone) {
        var userEntity = userRepository.findUserByPhone(phone);
        return userEntity.orElseThrow(() -> new ApiException("User by phone number is not fount"));
    }
}
