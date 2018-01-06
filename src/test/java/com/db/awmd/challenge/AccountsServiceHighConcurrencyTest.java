package com.db.awmd.challenge;

import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.exception.InsufficientFundsException;
import com.db.awmd.challenge.exception.InvalidAccountException;
import com.db.awmd.challenge.service.AccountsService;
import com.db.awmd.challenge.service.NotificationService;

import lombok.extern.slf4j.Slf4j;

/**
 * The purpose of the this test class is to demonstrate that no deadlock occurs
 * during account transfer when multiple threads are invoked. All the
 * transactions may not be completed successfully sometime with higher number of
 * threads and few threads may exit gracefully when a lock is not obtained and
 * amount transfer fails as per implementation of transferAmount method in
 * AccountsService class.
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class AccountsServiceHighConcurrencyTest {

	NotificationService emailNotificationService = Mockito.mock(NotificationService.class);

	@Autowired
	@InjectMocks
	private AccountsService underTest;

	private static AtomicInteger transferSuccessThreadCount = new AtomicInteger(0);
	private static AtomicInteger thransferFailureThreadCount = new AtomicInteger(0);

	private static AtomicBoolean isSetupDone = new AtomicBoolean(false);

	@Before
	public void setup() throws InvalidAccountException {
		if (isSetupDone.get() == false) {
			isSetupDone.set(true);
		}
		log.debug("setup is run***************************************");

		underTest.createAccount(new Account("Id-124", new BigDecimal("3000")));
		log.info("intial balance in Id-124 :" + underTest.getAccount("Id-124").getBalance());
		underTest.createAccount(new Account("Id-125", new BigDecimal("4000")));
		log.info("intial balance in Id-124 :" + underTest.getAccount("Id-125").getBalance());

	}

	@Test
	public void testConcurrentTransactions() {
		Runnable task1 = () -> {
			try {

				setSuccessFailureCounters(underTest.transferAmount("Id-124", "Id-125", new BigDecimal("1")));
			} catch (InsufficientFundsException | InterruptedException | InvalidAccountException e) {
				log.error("" + e);
			}
		};

		Runnable task2 = () -> {
			try {
				setSuccessFailureCounters(underTest.transferAmount("Id-125", "Id-124", new BigDecimal("2")));
			} catch (InsufficientFundsException | InterruptedException | InvalidAccountException e) {
				log.error("" + e);
			}
		};
		ExecutorService service = Executors.newCachedThreadPool();
		IntStream.range(0, 1000).parallel().forEach(counter -> {
			service.execute(task1);
			service.execute(task2);

		});
		service.shutdown();
		try {
			service.awaitTermination(20, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	@After
	public void tearDown() throws InvalidAccountException, InterruptedException {
		Thread.sleep(20000);

		log.info("number of successful transfers" + transferSuccessThreadCount);
		log.info("number of failed transfers" + thransferFailureThreadCount);
		log.info("the balance in Id-124 is " + underTest.getAccount("Id-124").getBalance());
		log.info("the balance in Id-125 is " + underTest.getAccount("Id-125").getBalance());
		assertTrue(underTest.getAccount("Id-124").getBalance().intValue() == 4000);
		assertTrue(underTest.getAccount("Id-125").getBalance().intValue() == 3000);

	}

	private void setSuccessFailureCounters(boolean isTransferSuccessful) {
		if (isTransferSuccessful) {
			transferSuccessThreadCount.getAndIncrement();
		} else {
			thransferFailureThreadCount.getAndIncrement();
		}
	}
}
