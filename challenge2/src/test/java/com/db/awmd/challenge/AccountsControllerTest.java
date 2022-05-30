package com.db.awmd.challenge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.domain.TransferMoneyModel;
import com.db.awmd.challenge.service.AccountsService;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@WebAppConfiguration
public class AccountsControllerTest {

  private MockMvc mockMvc;

  @Autowired
  private AccountsService accountsService;

  @Autowired
  private WebApplicationContext webApplicationContext;

  @BeforeEach
  public void prepareMockMvc() {
    this.mockMvc = webAppContextSetup(this.webApplicationContext).build();

    // Reset the existing accounts before each test.
    accountsService.getAccountsRepository().clearAccounts();
  }

  @Test
  public void createAccount() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountId\":\"Id-123\",\"balance\":1000}")).andExpect(status().isCreated());

    Account account = accountsService.getAccount("Id-123");
    assertThat(account.getAccountId()).isEqualTo("Id-123");
    assertThat(account.getBalance()).isEqualByComparingTo("1000");
  }

  @Test
  public void createDuplicateAccount() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountId\":\"Id-123\",\"balance\":1000}")).andExpect(status().isCreated());

    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountId\":\"Id-123\",\"balance\":1000}")).andExpect(status().isBadRequest());
  }

  @Test
  public void createAccountNoAccountId() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"balance\":1000}")).andExpect(status().isBadRequest());
  }

  @Test
  public void createAccountNoBalance() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountId\":\"Id-123\"}")).andExpect(status().isBadRequest());
  }

  @Test
  public void createAccountNoBody() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON))
      .andExpect(status().isBadRequest());
  }

  @Test
  public void createAccountNegativeBalance() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountId\":\"Id-123\",\"balance\":-1000}")).andExpect(status().isBadRequest());
  }

  @Test
  public void createAccountEmptyAccountId() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountId\":\"\",\"balance\":1000}")).andExpect(status().isBadRequest());
  }

  @Test
  public void getAccount() throws Exception {
    String uniqueAccountId = "Id-" + System.currentTimeMillis();
    Account account = new Account(uniqueAccountId, new BigDecimal("123.45"));
    this.accountsService.createAccount(account);
    this.mockMvc.perform(get("/v1/accounts/" + uniqueAccountId))
      .andExpect(status().isOk())
      .andExpect(
        content().string("{\"accountId\":\"" + uniqueAccountId + "\",\"balance\":123.45}"));
  }
  
  @Test
  public void transferMoney() throws Exception {
	Account account1 = new Account("John", new BigDecimal("1245"));
    this.accountsService.createAccount(account1);
    Account account2 = new Account("Ron", new BigDecimal("123.45"));
    this.accountsService.createAccount(account2);
    this.mockMvc.perform(put("/v1/accounts/").contentType(MediaType.APPLICATION_JSON)
    		.content("{\"accountFromId\":\"John\",\"accountToId\":\"Ron\",\"amount\": 10}"))
    		.andExpect(status().isOk());
  }
  
  @Test
  public void transferMoneyWithNegativeAmount() throws Exception {
	Account account1 = new Account("John", new BigDecimal("1245"));
    this.accountsService.createAccount(account1);
    Account account2 = new Account("Ron", new BigDecimal("123.45"));
    this.accountsService.createAccount(account2);
    this.mockMvc.perform(put("/v1/accounts/").contentType(MediaType.APPLICATION_JSON)
    		.content("{\"accountFromId\":\"John\",\"accountToId\":\"Ron\",\"amount\": -10}"))
    		.andExpect(status().isBadRequest());
  }
  
  @Test
  public void transferMoneyWithAmountExceedingAccountBalance() throws Exception {
	Account account1 = new Account("John", new BigDecimal("1245"));
    this.accountsService.createAccount(account1);
    Account account2 = new Account("Ron", new BigDecimal("123.45"));
    this.accountsService.createAccount(account2);
    this.mockMvc.perform(put("/v1/accounts/").contentType(MediaType.APPLICATION_JSON)
    		.content("{\"accountFromId\":\"John\",\"accountToId\":\"Ron\",\"amount\": 100000}"))
    		.andExpect(status().isBadRequest());
  }
  
  @Test
  public void transferMoneyToNonExistingAccount() throws Exception {
	Account account1 = new Account("John", new BigDecimal("1245"));
    this.accountsService.createAccount(account1);
    Account account2 = new Account("Ron", new BigDecimal("123.45"));
    this.accountsService.createAccount(account2);
    this.mockMvc.perform(put("/v1/accounts/").contentType(MediaType.APPLICATION_JSON)
    		.content("{\"accountFromId\":\"John\",\"accountToId\":\"RON\",\"amount\": 10}"))
    		.andExpect(status().isBadRequest());
  }
  
  
}
