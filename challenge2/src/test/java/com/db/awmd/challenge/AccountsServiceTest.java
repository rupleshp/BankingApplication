package com.db.awmd.challenge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.fail;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.domain.TransferMoneyModel;
import com.db.awmd.challenge.exception.DuplicateAccountIdException;
import com.db.awmd.challenge.exception.InsufficientFundsException;
import com.db.awmd.challenge.exception.InvalidAccountDetails;
import com.db.awmd.challenge.service.AccountsService;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class AccountsServiceTest {

  @Autowired
  private AccountsService accountsService;

  @Test
  public void addAccount() throws Exception {
    Account account = new Account("Id-123");
    account.setBalance(new BigDecimal(1000));
    this.accountsService.createAccount(account);

    assertThat(this.accountsService.getAccount("Id-123")).isEqualTo(account);
  }

  @Test
  public void addAccount_failsOnDuplicateId() throws Exception {
    String uniqueId = "Id-" + System.currentTimeMillis();
    Account account = new Account(uniqueId);
    this.accountsService.createAccount(account);

    try {
      this.accountsService.createAccount(account);
      fail("Should have failed when adding duplicate account");
    } catch (DuplicateAccountIdException ex) {
      assertThat(ex.getMessage()).isEqualTo("Account id " + uniqueId + " already exists!");
    }
  }
  
  @Test
  public void transferMoney() throws Exception {
	Account account1 = new Account("John", new BigDecimal("1245"));
    this.accountsService.createAccount(account1);
    Account account2 = new Account("Ron", new BigDecimal("123.45"));
    this.accountsService.createAccount(account2);
    TransferMoneyModel accountTransferModel = new TransferMoneyModel("John","Ron", new BigDecimal("100"));
    this.accountsService.transferMoney(accountTransferModel);
    assertThat(this.accountsService.getAccount("John").getBalance()).isEqualTo(new BigDecimal("1145"));
  }
  
  @Test
  public void transferMoneyToNonExistingAccount() throws Exception {
	Account account1 = new Account("Johnn", new BigDecimal("1245"));
    this.accountsService.createAccount(account1);
    Account account2 = new Account("Ronn", new BigDecimal("123.45"));
    this.accountsService.createAccount(account2);
    TransferMoneyModel accountTransferModel = new TransferMoneyModel("John","RONN", new BigDecimal("100"));
    Throwable exceptionTest = assertThrows(InvalidAccountDetails.class, ()->this.accountsService.transferMoney(accountTransferModel));
    assertEquals(InvalidAccountDetails.class, exceptionTest.getClass());
  }
  
  @Test
  public void transferNegativeMoney() throws Exception {
	Account account1 = new Account("Jony", new BigDecimal("1245"));
    this.accountsService.createAccount(account1);
    Account account2 = new Account("Rony", new BigDecimal("123.45"));
    this.accountsService.createAccount(account2);
    TransferMoneyModel accountTransferModel = new TransferMoneyModel("Jony","Rony", new BigDecimal("10000"));
    Throwable exceptionTest = assertThrows(InsufficientFundsException.class, ()->this.accountsService.transferMoney(accountTransferModel));
    assertEquals(InsufficientFundsException.class, exceptionTest.getClass());
  }
  
}
