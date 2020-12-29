package com.db.awmd.challenge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.domain.TransferAccountRequest;
import com.db.awmd.challenge.exception.DuplicateAccountIdException;
import com.db.awmd.challenge.exception.TransactionExceptionMessage;
import com.db.awmd.challenge.service.AccountsService;
import com.db.awmd.challenge.service.TransferAmountService;

import java.math.BigDecimal;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class AccountsServiceTest {

	@Autowired
	private AccountsService accountsService;
	@Autowired
	TransferAmountService transferAmountService;

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
	public void amountTransfer() throws Exception {
		Account account = new Account("Id-124");
		account.setBalance(new BigDecimal(1000));
		Account account1 = new Account("Id-125");
		account1.setBalance(new BigDecimal(1000));
		accountsService.createAccount(account);
		accountsService.createAccount(account1);
		TransferAccountRequest transferAccountRequest = new TransferAccountRequest(account.getAccountId(),
				account1.getAccountId(), new BigDecimal(500));
		assertThat(this.transferAmountService.transferAmount(transferAccountRequest)).isEqualTo("Transaction Sucessfull");

	}

	
	  @Test
	  public void insufficentBalanceInFromAccount() throws Exception 
	  { 
		  Account account = new Account("Id-126"); account.setBalance(new BigDecimal(1000)); 
		  Account account1 = new Account("Id-127"); account1.setBalance(new BigDecimal(1000));
		  this.accountsService.createAccount(account);
		  this.accountsService.createAccount(account1); 
		  TransferAccountRequest transferAccountRequest = new TransferAccountRequest(account.getAccountId(),account1.getAccountId(),new  BigDecimal(999999));
			try {
				this.transferAmountService.transferAmount(transferAccountRequest);
			} catch (TransactionExceptionMessage ex) {
				System.out.println(ex.getMessage());
				
				assertThat(ex.getMessage()).isEqualTo("Account number "+account.getAccountId()+" does not have enough balance.");
			}
	  }
	  
	  
	  
	  @Test
	  public void providingSourceORDestinationAccountsWrong() throws Exception 
	  { 
		  Account account = new Account("Id-128"); account.setBalance(new BigDecimal(1000)); 
		  Account account1 = new Account("Id-129"); account1.setBalance(new BigDecimal(1000));
		  this.accountsService.createAccount(account);
		  this.accountsService.createAccount(account1);
		  TransferAccountRequest transferAccountRequest = new TransferAccountRequest(account.getAccountId(),"Id678",new  BigDecimal(500));
			try {
				this.transferAmountService.transferAmount(transferAccountRequest);
			} catch (TransactionExceptionMessage ex) {
				if(!(transferAccountRequest.getFromAccountNumber()).equals(account.getAccountId()))
				assertThat(ex.getMessage()).isEqualTo("Source account number "+transferAccountRequest.getFromAccountNumber()+" does not exist.");
				if(!(transferAccountRequest.getToAccountNumber()).equals(account1.getAccountId()))
					assertThat(ex.getMessage()).isEqualTo("Destination account number "+transferAccountRequest.getToAccountNumber()+" does not exist.");
			}
	  }
	  
	  @Test
	  public void negativeAmountTransfer() throws Exception 
	  { 
		  Account account = new Account("Id-132"); account.setBalance(new BigDecimal(1000)); 
		  Account account1 = new Account("Id-133"); account1.setBalance(new BigDecimal(1000));
		  this.accountsService.createAccount(account);
		  this.accountsService.createAccount(account1);
		  TransferAccountRequest transferAccountRequest = new TransferAccountRequest(account.getAccountId(),account1.getAccountId(),new  BigDecimal(-1));
			try {
				this.transferAmountService.transferAmount(transferAccountRequest);
			} catch (TransactionExceptionMessage ex) {
				assertThat(ex.getMessage()).isEqualTo("Transaction Amount should be Positive value");
			}
	  }
	  
	  @Test
	  public void duplicateAccountsTransfer() throws Exception 
	  { 
		  Account account = new Account("Id-130"); account.setBalance(new BigDecimal(1000)); 
		  Account account1 = new Account("Id-131"); account1.setBalance(new BigDecimal(1000));
		  this.accountsService.createAccount(account);
		  TransferAccountRequest transferAccountRequest = new TransferAccountRequest(account.getAccountId(),account.getAccountId(),new  BigDecimal(500));
			try {
				this.transferAmountService.transferAmount(transferAccountRequest);
			} catch (TransactionExceptionMessage ex) {
				assertThat(ex.getMessage()).isEqualTo("Source and Destination accounts should not be same");
			}
	  }
	  
	 
	  
	  
	  
}
