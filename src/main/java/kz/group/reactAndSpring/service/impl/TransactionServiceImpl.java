package kz.group.reactAndSpring.service.impl;

import jakarta.transaction.Transactional;
import kz.group.reactAndSpring.dto.bankDto.TransactionDto;
import kz.group.reactAndSpring.entity.BankCardEntity;
import kz.group.reactAndSpring.entity.TransactionEntity;
import kz.group.reactAndSpring.entity.UserEntity;
import kz.group.reactAndSpring.enumeration.BankType;
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
    public TransactionDto transferTransaction(String sourcePhone, String destPhone, BigDecimal amount) {
        var sourceCard = getBankEntityByPhoneNumber(sourcePhone);
        var destCard = getBankEntityByPhoneNumber(destPhone);
        if (sourceCard.equals(destCard)) {
            throw new ApiException("Source and dest card are the same");
        }
        checkBalanceForTransaction(sourceCard, amount);
        sourceCard.setBalance(sourceCard.getBalance().subtract(amount));
        destCard.setBalance(destCard.getBalance().add(amount));
        bankCardRepository.save(sourceCard);
        bankCardRepository.save(destCard);
        var transaction = createTransferTransaction(sourceCard, destCard, amount, COMPLETED);
        transactionRepository.save(transaction);
        return convertToDto(transaction);
    }

    @Override
    public TransactionDto debitTransaction(String phoneNumber, BigDecimal amount) {
        var bankEntity = getBankEntityByPhoneNumber(phoneNumber);
        checkCardTransactionLimit(bankEntity, amount);
        checkBalanceForTransaction(bankEntity, amount);
        var bonuses = BigDecimal.ZERO;
        if (bankEntity.getCardName().equals(BankType.PREMIUM.getValue())) {
            bonuses = amount.multiply(BigDecimal.valueOf(0.10));
        } else {
            bonuses = amount.multiply(BigDecimal.valueOf(0.05));
        }
        bankEntity.setBalance(bankEntity.getBalance().subtract(amount));
        bankEntity.setTransactionLimit(bankEntity.getTransactionLimit().subtract(amount));
        bankEntity.setBalance(bankEntity.getBalance().add(bonuses));
        bankEntity.setBonuses(bankEntity.getBalance().add(bonuses));
        bankCardRepository.save(bankEntity);
        var transaction = createCreditDebitTransaction(bankEntity, DEBIT, amount, COMPLETED);
        transactionRepository.save(transaction);
        return convertToDto(transaction);
    }

    @Override
    public TransactionDto creditTransfer(String phoneNumber, BigDecimal amount) {
        var bankEntity = getBankEntityByPhoneNumber(phoneNumber);
        bankEntity.setBalance(bankEntity.getBalance().add(amount));
        bankCardRepository.save(bankEntity);
        var transaction = createCreditDebitTransaction(bankEntity, CREDIT, amount, COMPLETED);
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

    private void checkCardTransactionLimit(BankCardEntity bankCardEntity, BigDecimal amount) {
        if(bankCardEntity.getTransactionLimit().compareTo(amount) < 0) {
            throw new ApiException("Transaction limit exceeded");
        }
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
