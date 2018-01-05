package com.db.awmd.challenge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.math.BigDecimal;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.exception.DuplicateAccountIdException;
import com.db.awmd.challenge.exception.InsufficientFundsException;
import com.db.awmd.challenge.exception.InvalidAccountException;
import com.db.awmd.challenge.service.AccountsService;
import com.db.awmd.challenge.service.NotificationService;

@RunWith(SpringRunner.class)
@SpringBootTest
public class AccountsServiceTest {

	@Mock
	NotificationService notificationService;

	@Autowired
	@InjectMocks
	private AccountsService underTest;

	private static boolean isSetupRun = false;

	@Before
	public void setup() {
		if (isSetupRun == false) {

			underTest.createAccount(new Account("Id-126", new BigDecimal("1000")));
			underTest.createAccount(new Account("Id-127", new BigDecimal("500")));
			isSetupRun = true;
		}
	}

	@Test
	public void addAccount() throws Exception {
		Account account = new Account("Id-123");
		account.setBalance(new BigDecimal(1000));
		underTest.createAccount(account);

		assertThat(underTest.getAccount("Id-123")).isEqualTo(account);
	}

	@Test
	public void addAccount_failsOnDuplicateId() throws Exception {
		String uniqueId = "Id-" + System.currentTimeMillis();
		Account account = new Account(uniqueId);
		underTest.createAccount(account);

		try {
			underTest.createAccount(account);
			fail("Should have failed when adding duplicate account");
		} catch (DuplicateAccountIdException ex) {
			assertThat(ex.getMessage()).isEqualTo("Account id " + uniqueId + " already exists!");
		}

	}

	@Test
	public void transferAmountSuccessfully()
			throws InsufficientFundsException, InterruptedException, InvalidAccountException {

		boolean isTransferSuccessful = underTest.transferAmount("Id-126", "Id-127", new BigDecimal("500"));
		assertTrue(isTransferSuccessful == true);
		assertTrue(underTest.getAccount("Id-127").getBalance().equals(new BigDecimal("1000")));

	}

	@Test
	public void when_transfer_amount_with_null_from_account_then_return_false()
			throws InsufficientFundsException, InterruptedException, InvalidAccountException {

		boolean isTransferSuccesful = underTest.transferAmount(null, "Id-125", new BigDecimal("500"));
		assertTrue(isTransferSuccesful == false);

	}

	@Test
	public void when_transfer_amount_larger_than_balance_then_throw_InsufficientFundsException()
			throws InsufficientFundsException, InterruptedException, InvalidAccountException {
		try {
			underTest.transferAmount("Id-126", "Id-127", new BigDecimal("5000"));
			fail("should fail as amount is greater than balance");
		} catch (InsufficientFundsException ex) {
			assertThat(ex.getMessage()).isEqualTo("Available balance is less that amount to transfer" + 5000);
		}

	}

}
