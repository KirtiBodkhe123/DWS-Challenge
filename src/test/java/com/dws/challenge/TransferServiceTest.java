package com.dws.challenge;

import com.dws.challenge.domain.Account;
import com.dws.challenge.exception.InsufficientFundsException;
import com.dws.challenge.service.AccountsService;
import com.dws.challenge.service.NotificationService;
import com.dws.challenge.service.TransferService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
@ExtendWith(SpringExtension.class)
@SpringBootTest
class TransferServiceTest {

    @Mock
    private AccountsService accountsService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private TransferService transferService;

    private Account fromAccount;
    private Account toAccount;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        fromAccount = new Account("Id-123", new BigDecimal("1000"));
        toAccount = new Account("Id-456", new BigDecimal("500"));
    }

    @Test
    void transferMoney_successful() {
        // Arrange
        when(accountsService.getAccount("Id-123")).thenReturn(fromAccount);
        when(accountsService.getAccount("Id-456")).thenReturn(toAccount);

        // Act
        transferService.transferMoney("Id-123", "Id-456", BigDecimal.valueOf(200));

        // Assert
        verify(accountsService, times(1)).getAccount("Id-123");
        verify(accountsService, times(1)).getAccount("Id-456");
        verify(notificationService).notifyAboutTransfer(fromAccount, "Transferred 200 to account Id-456");
        verify(notificationService).notifyAboutTransfer(toAccount, "Received 200 from account Id-123");

        // Check balances
//        assertThat(fromAccount.getBalance()).isEqualByComparingTo("800");
//        assertThat(toAccount.getBalance()).isEqualByComparingTo("700");
    }

    @Test
    void transferMoney_insufficientFunds() {
        // Arrange
        when(accountsService.getAccount("Id-123")).thenReturn(fromAccount);
        when(accountsService.getAccount("Id-456")).thenReturn(toAccount);

        // Act & Assert
        assertThrows(InsufficientFundsException.class, () ->
                transferService.transferMoney("Id-123", "Id-456", BigDecimal.valueOf(1200)));

        // Verify that notifications were not sent
        verify(notificationService, never()).notifyAboutTransfer(any(), anyString());
    }

    @Test
    void transferMoney_negativeAmount() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
                transferService.transferMoney("Id-123", "Id-456", BigDecimal.valueOf(-100)));

        // Verify that no accounts were retrieved or notifications sent
        verify(accountsService, never()).getAccount(anyString());
        verify(notificationService, never()).notifyAboutTransfer(any(), anyString());
    }

    @Test
    void transferMoney_zeroAmount() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
                transferService.transferMoney("Id-123", "Id-456", BigDecimal.ZERO));

        // Verify that no accounts were retrieved or notifications sent
        verify(accountsService, never()).getAccount(anyString());
        verify(notificationService, never()).notifyAboutTransfer(any(), anyString());
    }
}
