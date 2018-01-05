package com.db.awmd.challenge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import java.math.BigDecimal;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.service.AccountsService;

@RunWith(SpringRunner.class)
@SpringBootTest
@WebAppConfiguration
public class AccountsControllerTest {

	private MockMvc mockMvc;

	@Autowired
	private AccountsService accountsService;

	@Autowired
	private WebApplicationContext webApplicationContext;

	@Before
	public void prepareMockMvc() throws Exception {
		this.mockMvc = webAppContextSetup(this.webApplicationContext).build();

		// Reset the existing accounts before each test.
		accountsService.getAccountsRepository().clearAccounts();
		this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
				.content("{\"accountId\":\"Id-124\",\"balance\":1000}")).andExpect(status().isCreated());
		this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
				.content("{\"accountId\":\"Id-125\",\"balance\":1000}")).andExpect(status().isCreated());

	}

	@Test
	public void when_accountId_and_balace_are_valid_then_account_is_created_successfully() throws Exception {
		this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
				.content("{\"accountId\":\"Id-123\",\"balance\":1000}")).andExpect(status().isCreated());

		Account account = accountsService.getAccount("Id-123");
		assertThat(account.getAccountId()).isEqualTo("Id-123");
		assertThat(account.getBalance()).isEqualByComparingTo("1000");
	}

	@Test
	public void when_accountId_is_duplicate_then_throw_DuplicateAccountIdException() throws Exception {
		this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
				.content("{\"accountId\":\"Id-123\",\"balance\":1000}")).andExpect(status().isCreated());

		this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
				.content("{\"accountId\":\"Id-123\",\"balance\":1000}")).andExpect(status().isBadRequest());
	}

	@Test
	public void When_accountId_isNull_return_validatonMessage() throws Exception {
		this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON).content("{\"balance\":1000}"))
				.andExpect(status().isBadRequest()).andReturn().getResponse().getContentAsString()
				.contains("Account Id cannot be null or empty");

	}

	@Test
	public void when_balance_is_null_return_validation_message() throws Exception {
		this.mockMvc
				.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
						.content("{\"accountId\":\"Id-123\"}"))
				.andExpect(status().isBadRequest()).andReturn().getResponse().getContentAsString()
				.contains("Initial balance can not be null");
	}

	@Test
	public void when_balance_is_zero_create_account_successfully() throws Exception {
		this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
				.content("{\"accountId\":\"Id-130\",\"balance\":0}")).andExpect(status().isCreated());
	}

	@Test
	public void when_request_is_empty_then_do_not_create_account() throws Exception {
		this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}

	@Test
	public void when_balance_is_negative_do_not_create_account_and_return_validation_message() throws Exception {
		this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
				.content("{\"accountId\":\"Id-123\",\"balance\":-1000}")).andExpect(status().isBadRequest());
	}

	@Test
	public void when_account_id_is_empty_return_validation_message_and_do_not_create_account() throws Exception {
		this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
				.content("{\"accountId\":\"\",\"balance\":1000}")).andExpect(status().isBadRequest());
	}

	@Test
	public void getAccount() throws Exception {
		String uniqueAccountId = "Id-" + System.currentTimeMillis();
		Account account = new Account(uniqueAccountId, new BigDecimal("123.45"));
		this.accountsService.createAccount(account);
		this.mockMvc.perform(get("/v1/accounts/" + uniqueAccountId)).andExpect(status().isOk())
				.andExpect(content().json("{\"accountId\":\"" + uniqueAccountId + "\",\"balance\":123.45}"));
	}

	@Test
	public void when_transfer_amount_with_valid_accounts_then_transfer_is_successful() throws Exception {
		this.mockMvc
				.perform(post("/v1/accounts/transfer").contentType(MediaType.APPLICATION_JSON)
						.content("{\"fromAccountId\":\"Id-124\",\"toAccountId\":\"Id-125\",\"amount\":500}"))
				.andExpect(status().isOk());

		assertEquals(new BigDecimal(500), this.accountsService.getAccount("Id-124").getBalance());
		assertEquals(new BigDecimal(1500), this.accountsService.getAccount("Id-125").getBalance());
	}

	@Test
	public void When_transfer_amount_from_non_existing_account_then_return_bad_request_code() throws Exception {

		String content = this.mockMvc
				.perform(post("/v1/accounts/transfer").contentType(MediaType.APPLICATION_JSON)
						.content("{\"fromAccountId\":\"Id-126\",\"toAccountId\":\"Id-125\",\"amount\":500}"))
				.andExpect(status().isBadRequest()).andReturn().getResponse().getContentAsString();
		assertEquals("Account Id-126 does not exist", content);

	}

	@Test
	public void When_transfer_amount_from_null_account_id_then_return_bad_request_code() throws Exception {

		this.mockMvc.perform(post("/v1/accounts/transfer").contentType(MediaType.APPLICATION_JSON)
				.content("{\"toAccountId\":\"Id-125\",\"amount\":500}")).andExpect(status().isBadRequest());

	}
}
