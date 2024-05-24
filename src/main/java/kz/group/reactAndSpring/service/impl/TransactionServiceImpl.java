package kz.group.reactAndSpring.service.impl;

import jakarta.transaction.Transactional;
import kz.group.reactAndSpring.dto.bankDto.TransactionDto;
import kz.group.reactAndSpring.entity.BankCardEntity;
import kz.group.reactAndSpring.entity.TransactionEntity;
import kz.group.reactAndSpring.entity.UserEntity;
import kz.group.reactAndSpring.exception.ApiException;
import kz.group.reactAndSpring.repository.BankCardRepository;
import kz.group.reactAndSpring.repository.TransactionRepository;
import kz.group.reactAndSpring.repository.UserRepository;
import kz.group.reactAndSpring.service.EncryptionService;
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
    private final BankCardRepository bankCardRepository;
    private final EncryptionService encryptionService;

    @Override
    public TransactionDto transferTransaction(String sourcePhone, String destPhone, String amount) {
        var sourceCard = getBankEntityByPhoneNumber(sourcePhone);
        var destCard = getBankEntityByPhoneNumber(sourcePhone);
        var userAmount = new BigDecimal(amount);
        checkBalanceForTransaction(sourceCard, userAmount);
        sourceCard.setBalance(sourceCard.getBalance().subtract(userAmount));
        destCard.setBalance(sourceCard.getBalance().add(userAmount));
        bankCardRepository.save(sourceCard);
        bankCardRepository.save(destCard);
        var transaction = createTransferTransaction(sourceCard, destCard, userAmount, COMPLETED);
        transactionRepository.save(transaction);
        return convertToDto(transaction);
    }

    @Override
    public TransactionDto debitTransaction(String phoneNumber, String amount) {
        var bankEntity = getBankEntityByPhoneNumber(phoneNumber);
        var userAmount = new BigDecimal(amount);
        checkBalanceForTransaction(bankEntity, userAmount);
        bankEntity.setBalance(bankEntity.getBalance().subtract(userAmount));
        bankCardRepository.save(bankEntity);
        var transaction = createCreditDebitTransaction(bankEntity, DEBIT, userAmount, COMPLETED);
        transactionRepository.save(transaction);
        return convertToDto(transaction);
    }

    @Override
    public TransactionDto creditTransfer(String phoneNumber, String amount) {
        var bankEntity = getBankEntityByPhoneNumber(phoneNumber);
        var userAmount = new BigDecimal(amount);
        bankEntity.setBalance(bankEntity.getBalance().add(userAmount));
        bankCardRepository.save(bankEntity);
        var transaction = createCreditDebitTransaction(bankEntity, CREDIT, userAmount, COMPLETED);
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
        var bankEntity = getBankEntityByPhoneNumber(userEntity.getPhone());
        List<TransactionEntity> transactionEntities = transactionRepository.getTransactionEntitiesBySourceCard(bankEntity);
        List<TransactionDto> transactionDtos = transactionEntities.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return transactionDtos;
    }

    private void checkBalanceForTransaction(BankCardEntity bankCard, BigDecimal amount) {
        if(bankCard.getBalance().compareTo(amount) < 0) {
            throw new ApiException("You have not enough money to deposit.");
        }
    }

    private TransactionEntity createTransferTransaction(BankCardEntity sourceBankCard, BankCardEntity destBankCard, BigDecimal amount, String status) {
        return TransactionEntity.builder()
                .transactionId(UUID.randomUUID().toString())
                .transactionType(TRANSFER)
                .sourceCard(sourceBankCard)
                .destCard(destBankCard)
                .transactionDate(now())
                .amount(amount)
                .status(status)
                .build();
    }

    private TransactionEntity createCreditDebitTransaction(BankCardEntity bankCard, String type, BigDecimal amount, String status) {
        return TransactionEntity.builder()
                .transactionId(UUID.randomUUID().toString())
                .transactionType(type)
                .sourceCard(bankCard)
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

    private BankCardEntity getBankEntityByPhoneNumber(String phoneNumber) {
        var BankEntity = bankCardRepository.findByCardNumber(encryptionService.encrypt(phoneNumber));
        return BankEntity.orElseThrow(() -> new ApiException("Bank card is not found"));
    }

    private UserEntity getUserEntityByUserId(String userId) {
        var userEntity = userRepository.findUserByUserId(userId);
        return userEntity.orElseThrow(() -> new ApiException("User by user id is not found."));
    }
}
