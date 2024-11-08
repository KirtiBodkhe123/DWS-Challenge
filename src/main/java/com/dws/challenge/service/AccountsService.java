package com.dws.challenge.service;

import com.dws.challenge.domain.Account;
import com.dws.challenge.repository.AccountsRepository;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AccountsService {

  @Getter
  private final AccountsRepository accountsRepository;

  private final Map<String, Account> accounts = new ConcurrentHashMap<>();
  @Autowired
  public AccountsService(AccountsRepository accountsRepository) {
    this.accountsRepository = accountsRepository;
  }

  public void createAccount(Account account) {
    this.accountsRepository.createAccount(account);
  }

  public Account getAccount(String accountId) {
    Account account = this.accountsRepository.getAccount(accountId);
    if (account == null) {
      throw new IllegalArgumentException("Account with ID " + accountId + " not found.");
    }
    return account;
  }


}
