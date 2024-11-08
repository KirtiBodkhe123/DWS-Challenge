package com.dws.challenge.service;

import com.dws.challenge.domain.Account;
import com.dws.challenge.exception.InsufficientFundsException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class TransferService {

    private final AccountsService accountService;
    private final NotificationService notificationService;

    @Transactional
    public void transferMoney(String accountFromId, String accountToId, BigDecimal amount)  {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transfer amount must be positive");
        }

        // Retrieve both accounts
        Account fromAccount = accountService.getAccount(accountFromId);
        Account toAccount = accountService.getAccount(accountToId);

        // Synchronize in a fixed order to avoid deadlock
        Account firstLock = accountFromId.compareTo(accountToId) < 0 ? fromAccount : toAccount;
        Account secondLock = accountFromId.compareTo(accountToId) < 0 ? toAccount : fromAccount;

        synchronized (firstLock) {
            synchronized (secondLock) {
                // Perform balance checks and updates within synchronized blocks
                if (fromAccount.getBalance().compareTo(amount) < 0) {
                    throw new InsufficientFundsException("Insufficient balance for transfer");
                }

                fromAccount.debit(amount);
                toAccount.credit(amount);

                // Send notifications
                notificationService.notifyAboutTransfer(fromAccount,
                        "Transferred " + amount + " to account " + accountToId);
                notificationService.notifyAboutTransfer(toAccount,
                        "Received " + amount + " from account " + accountFromId);
            }
        }
    }
}

