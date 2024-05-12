package kz.group.reactAndSpring.service.impl;

import jakarta.transaction.Transactional;
import kz.group.reactAndSpring.entity.UserEntity;
import kz.group.reactAndSpring.exception.ApiException;
import kz.group.reactAndSpring.repository.TransactionRepository;
import kz.group.reactAndSpring.repository.UserRepository;
import kz.group.reactAndSpring.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;


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
    }


    private UserEntity getUserEntityByPhoneNumber(String phone) {
        var userEntity = userRepository.findUserByPhone(phone);
        return userEntity.orElseThrow(() -> new ApiException("User by phone number is not fount"));
    }
}
