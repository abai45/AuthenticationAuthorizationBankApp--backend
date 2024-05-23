package kz.group.reactAndSpring.service.impl;

import jakarta.transaction.Transactional;
import kz.group.reactAndSpring.dto.bankDto.TransactionDto;
import kz.group.reactAndSpring.entity.TransactionEntity;
import kz.group.reactAndSpring.entity.UserEntity;
import kz.group.reactAndSpring.exception.ApiException;
import kz.group.reactAndSpring.repository.TransactionRepository;
import kz.group.reactAndSpring.repository.UserRepository;
import kz.group.reactAndSpring.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.time.LocalDateTime.now;


@Service
@Transactional(rollbackOn = Exception.class)
@RequiredArgsConstructor
@Slf4j
public class TransactionServiceImpl implements TransactionService {
    public static final String COMPLETED = "COMPLETED";
    public static final String TRANSFER = "TRANSFER";
    public static final String DEBIT = "DEBIT";
    public static final String CREDIT = "CREDIT";
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    @Override
    public TransactionDto transferTransaction(String sourcePhone, String destPhone, String amount) {
        var userSource = getUserEntityByPhoneNumber(sourcePhone);
        var userDest = getUserEntityByPhoneNumber(destPhone);
        var userAmount = new BigDecimal(amount);
        checkBalanceForTransaction(userSource, userAmount);
        userSource.setBalance(userSource.getBalance().subtract(userAmount));
        userDest.setBalance(userDest.getBalance().add(userAmount));
        userRepository.save(userSource);
        userRepository.save(userDest);
        var transaction = createTransferTransaction(userSource, userDest, userAmount, COMPLETED);
        transactionRepository.save(transaction);
        return convertToDto(transaction);
    }

    @Override
    public TransactionDto debitTransaction(String phoneNumber, String amount) {
        var userEntity = getUserEntityByPhoneNumber(phoneNumber);
        var userAmount = new BigDecimal(amount);
        checkBalanceForTransaction(userEntity, userAmount);
        userEntity.setBalance(userEntity.getBalance().subtract(userAmount));
        userRepository.save(userEntity);
        var transaction = createCreditDebitTransaction(userEntity, DEBIT, userAmount, COMPLETED);
        transactionRepository.save(transaction);
        return convertToDto(transaction);
    }

    @Override
    public TransactionDto creditTransfer(String phoneNumber, String amount) {
        var userEntity = getUserEntityByPhoneNumber(phoneNumber);
        var userAmount = new BigDecimal(amount);
        userEntity.setBalance(userEntity.getBalance().add(userAmount));
        userRepository.save(userEntity);
        var transaction = createCreditDebitTransaction(userEntity, CREDIT, userAmount, COMPLETED);
        transactionRepository.save(transaction);
        return convertToDto(transaction);
    }

    @Override
    public void deleteTransaction(String transactionId) {
        var transaction = transactionRepository.findByTransactionId(transactionId);
        if (transaction != null) {
            transactionRepository.delete(transaction);
        } else {
            throw new ApiException("Transaction by transaction ID is not found");
        }
    }



    @Override
    public Collection<TransactionDto> getTransactions(String userId) {
        var userEntity = getUserEntityByUserId(userId);
        List<TransactionEntity> transactionEntities = transactionRepository.getTransactionEntitiesBySourceUser(userEntity);
        List<TransactionDto> transactionDtos = transactionEntities.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return transactionDtos;
    }

    private void checkBalanceForTransaction(UserEntity user, BigDecimal amount) {
        if(user.getBalance().compareTo(amount) < 0) {
            throw new ApiException("You have not enough money to deposit.");
        }
    }

    private TransactionEntity createTransferTransaction(UserEntity sourceUser, UserEntity destUser, BigDecimal amount, String status) {
        return TransactionEntity.builder()
                .transactionId(UUID.randomUUID().toString())
                .transactionType(TRANSFER)
                .sourceUser(sourceUser)
                .destUser(destUser)
                .transactionDate(now())
                .amount(amount)
                .status(status)
                .build();
    }

    private TransactionEntity createCreditDebitTransaction(UserEntity user, String type, BigDecimal amount, String status) {
        return TransactionEntity.builder()
                .transactionId(UUID.randomUUID().toString())
                .transactionType(type)
                .sourceUser(user)
                .transactionDate(now())
                .amount(amount)
                .status(status)
                .build();
    }

    private TransactionDto convertToDto(TransactionEntity transactionEntity) {
        var transaction = new TransactionDto();
        BeanUtils.copyProperties(transactionEntity, transaction);
        return transaction;
    }

    private UserEntity getUserEntityByUserId(String userId) {
        var userEntity = userRepository.findUserByUserId(userId);
        return userEntity.orElseThrow(() -> new ApiException("User by user id is not found."));
    }

    private UserEntity getUserEntityByPhoneNumber(String phone) {
        var userEntity = userRepository.findUserByPhone(phone);
        return userEntity.orElseThrow(() -> new ApiException("User by phone number is not found."));
    }
}
