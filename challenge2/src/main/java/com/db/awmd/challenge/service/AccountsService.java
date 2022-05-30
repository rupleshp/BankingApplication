package com.db.awmd.challenge.service;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.domain.TransferMoneyModel;
import com.db.awmd.challenge.exception.InsufficientFundsException;
import com.db.awmd.challenge.exception.InvalidAccountDetails;
import com.db.awmd.challenge.repository.AccountsRepository;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AccountsService {

	@Getter
	private final AccountsRepository accountsRepository;

	@Autowired
	private NotificationService notificationService;

	@Autowired
	public AccountsService(AccountsRepository accountsRepository) {
		this.accountsRepository = accountsRepository;
	}

	public void createAccount(Account account) {
		this.accountsRepository.createAccount(account);
	}

	public void transferMoney(TransferMoneyModel account) throws InsufficientFundsException, InvalidAccountDetails {
		Account sourceAccount = accountsRepository.getAccount(account.getAccountFromId());
		Account destinationAccount = accountsRepository.getAccount(account.getAccountToId());
		if (sourceAccount == null || destinationAccount == null) {
			throw new InvalidAccountDetails("Invalid Account details");
		}
		if (sourceAccount.getAccountId().equals(destinationAccount.getAccountId())) {
			throw new InvalidAccountDetails("Can not transfer within same account");
		}
		Account first = sourceAccount;
		Account second = destinationAccount;
		//Choose account with smaller account id
		if (first.getAccountId().compareTo(second.getAccountId()) > 0) {
			first = destinationAccount;
			second = sourceAccount;
		}
		//Acquire the lock of the account with a less id first
		//To avoid deadlock you have to acquire locks in the same order always
		synchronized (first) {
			synchronized (second) {
				if (account.getAmount().compareTo(sourceAccount.getBalance()) <= 0) {
					sourceAccount.setBalance(sourceAccount.getBalance().subtract(account.getAmount()));
					destinationAccount.setBalance(destinationAccount.getBalance().add(account.getAmount()));
					this.accountsRepository.updateBalance(sourceAccount);
					this.accountsRepository.updateBalance(destinationAccount);
				} else {
					throw new InsufficientFundsException("Insufficient funds");
				}
			}
		}
		String transferMessage = "Successfully transfered amount " + account.getAmount() + "to "
				+ account.getAccountToId();
		String receivedMessage = "Account is credited with amount " + account.getAmount() + "from "
				+ account.getAccountFromId();
		this.notificationService.notifyAboutTransfer(sourceAccount, transferMessage);
		this.notificationService.notifyAboutTransfer(destinationAccount, receivedMessage);

	}

	public Account getAccount(String accountId) {
		return this.accountsRepository.getAccount(accountId);
	}
}
