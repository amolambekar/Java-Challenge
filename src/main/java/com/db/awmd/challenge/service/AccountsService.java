package com.db.awmd.challenge.service;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.exception.InsufficientFundsException;
import com.db.awmd.challenge.exception.InvalidAccountException;
import com.db.awmd.challenge.repository.AccountsRepository;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AccountsService {

	@Getter
	private final AccountsRepository accountsRepository;

	private NotificationService notificationService;

	private static final long TIME_OUT = 6000L;

	private static final TimeUnit TIME_UNIT_MILISECONDS = TimeUnit.MILLISECONDS;

	@Autowired
	public AccountsService(final AccountsRepository accountsRepository, final NotificationService notificationService) {
		this.accountsRepository = accountsRepository;
		this.notificationService = notificationService;

	}

	public void createAccount(final Account account) {
		this.accountsRepository.createAccount(account);
	}

	public Account getAccount(final String accountId) throws InvalidAccountException {
		Account account = accountsRepository.getAccount(accountId);
		if (account == null)
			throw new InvalidAccountException("Account " + accountId + " does not exist");
		return account;
	}

	public boolean transferAmount(final String fromAccountId, final String toAccountId, BigDecimal amount)
			throws InsufficientFundsException, InterruptedException, InvalidAccountException {
		boolean isTransferSuccessful = false;
		if (isNotEmpty(fromAccountId) && isNotEmpty(toAccountId) && amount.compareTo(BigDecimal.ZERO) == 1) {

			Account fromAccount = this.getAccount(fromAccountId);
			Account toAccount = this.getAccount(toAccountId);
			isTransferSuccessful = this.transfer(fromAccount, toAccount, amount);
		}
		return isTransferSuccessful;

	}

	private boolean transfer(final Account fromAcct, final Account toAcct, final BigDecimal amount)
			throws InsufficientFundsException, InterruptedException, InvalidAccountException {

		final Account[] accounts = new Account[] { fromAcct, toAcct };
		Arrays.sort(accounts);
		if (accounts[0].getLock().tryLock(TIME_OUT, TIME_UNIT_MILISECONDS)) {
			try {
				if (accounts[1].getLock().tryLock(TIME_OUT, TIME_UNIT_MILISECONDS)) {
					try {
						return transferMoney(fromAcct, toAcct, amount);

					} finally {

						accounts[1].getLock().unlock();
					}
				}
			} finally {
				accounts[0].getLock().unlock();
			}
		}

		log.warn("Lock not acquired,Treansaction could not be completed.Exiting gracefully");
		return false;

	}

	private boolean transferMoney(final Account fromAcct, final Account toAcct, final BigDecimal amount)
			throws InsufficientFundsException, InvalidAccountException {
		if (fromAcct.getBalance().compareTo(amount) < 0)
			throw new InsufficientFundsException("Available balance is less that amount to transfer" + amount);
		else {

			fromAcct.setBalance(fromAcct.getBalance().subtract(amount));
			toAcct.setBalance(toAcct.getBalance().add(amount));
			log.info("transferred amount "+amount+" successfuly from account "+fromAcct.getAccountId()+" to acoount "+toAcct.getAccountId());
			notificationService.notifyAboutTransfer(fromAcct,
					"Account " + fromAcct.getAccountId() + " debited with amount " + amount);
			notificationService.notifyAboutTransfer(toAcct,
					"Account " + toAcct.getAccountId() + " debited with amount " + amount);
			
			return true;
		}
	}

}
