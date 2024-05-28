package kz.group.reactAndSpring.service;

import kz.group.reactAndSpring.entity.BankCardEntity;
import kz.group.reactAndSpring.enumeration.BankType;
import kz.group.reactAndSpring.repository.BankCardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class DepositService {
    @Autowired
    private BankCardRepository bankCardRepository;

    @Scheduled(cron = "0 0 0 1 * ?")
    @Transactional
    public void addMonthlyDeposit() {
        List<BankCardEntity> depositCards = bankCardRepository.findAllByCardName(BankType.DEPOSIT.getValue());
        for (BankCardEntity depositCard : depositCards) {
            BigDecimal addingDepositAmount = calculateAddingDeposit(depositCard.getBalance());
            depositCard.setBalance(depositCard.getBalance().add(addingDepositAmount));
            bankCardRepository.save(depositCard);
        }
    }

    private BigDecimal calculateAddingDeposit(BigDecimal balance) {
        BigDecimal depositAmount = new BigDecimal("0.05");
        return balance.multiply(depositAmount);
    }
}
