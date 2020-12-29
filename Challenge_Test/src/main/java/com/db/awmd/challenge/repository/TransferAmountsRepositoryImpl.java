package com.db.awmd.challenge.repository;

import static java.lang.String.format;

import java.math.BigDecimal;
import java.util.Random;
import java.util.concurrent.locks.Lock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.exception.TransactionExceptionMessage;
import com.db.awmd.challenge.service.EmailNotificationService;

import lombok.extern.slf4j.Slf4j;
@Slf4j
@Repository
public class TransferAmountsRepositoryImpl  {

	private final static String SOURCE_ACCOUNT_NOT_EXIST = "Source account number %s does not exist.";

	private final static String DESTINATION_ACCOUNT_NOT_EXIST = "Destination account number %s does not exist.";

	private final static String NOT_ENOUGH_BALANCE = "Account number %s does not have enough balance.";
	private final static String ACCOUNT_MATCH = "Source and Destination accounts should not be same";
	private final static String TRANSACTION_FAILED = "Transaction got Failed";
	private final static String TRANSACTION_SUCESSFULL ="Transaction Sucessfull";
	private final static String TRANS_AMOUNT_VALUE ="Transaction Amount should be Positive value";
	boolean accuntsValidationFlag=false;



	private final AccountsRepository accountsRepository;

	@Autowired
	public TransferAmountsRepositoryImpl(AccountsRepository accountsRepository) {
		this.accountsRepository= accountsRepository;
	}
	public Object transferAmount(String accountNumberFrom, String  accountNumberTo, BigDecimal amountToTransfer) {

		log.info("transferAmount {} : ");
		log.info("accountNumberFrom : "+accountNumberFrom);
		log.info("accountNumberTo : "+accountNumberTo);
		log.info("amountToTransfer : "+amountToTransfer);

		if(accountValidations(accountNumberFrom, accountNumberTo, amountToTransfer)) {
			Random rand = new Random();
			Account accountFrom = accountsRepository.getAccount(accountNumberFrom);
			Account accountTo = accountsRepository.getAccount(accountNumberTo);
			Lock fromAccountTransferLock = accountFrom.getTransferLock();
			Lock toAccountTransferLock = accountFrom.getTransferLock();
			while (true) {
				if (fromAccountTransferLock.tryLock()) {
					accountFrom.getLock().writeLock();
					if (toAccountTransferLock.tryLock()) {
						try {
							if(withdrawal(accountFrom,accountTo,amountToTransfer)){
								if(deposit(accountFrom,accountTo,amountToTransfer)) {
									log.info(TRANSACTION_SUCESSFULL);
									fromAccountTransferLock.unlock();
									break;
								}
								else
									throw new TransactionExceptionMessage(format(TRANSACTION_FAILED));
							}
						} catch (Exception e) {
							throw new TransactionExceptionMessage(format(TRANSACTION_FAILED));
						} finally {
							toAccountTransferLock.unlock();
						}
					}
					fromAccountTransferLock.unlock();
					try {
						Thread.sleep(rand.nextInt(1001));
					} catch (InterruptedException e) {
						throw new TransactionExceptionMessage(format(TRANSACTION_FAILED));
					}
				}
			}
			return TRANSACTION_SUCESSFULL;
		}
		return TRANSACTION_FAILED;
	}

	public boolean accountValidations(String accountNumberFrom, String  accountNumberTo, BigDecimal amountToTransfer)
	{
		if(amountToTransfer.doubleValue()>=1) {
			getAccount(accountNumberFrom, SOURCE_ACCOUNT_NOT_EXIST);
			if(checkBalance(accountNumberFrom).compareTo(amountToTransfer)  <= - 1){
				log.info(format(NOT_ENOUGH_BALANCE, accountNumberFrom));
				throw new TransactionExceptionMessage(format(NOT_ENOUGH_BALANCE, accountNumberFrom));}
			getAccount(accountNumberTo, DESTINATION_ACCOUNT_NOT_EXIST);
			if(accountNumberFrom.compareTo(accountNumberTo)==0)
			{
				log.info(format(format(ACCOUNT_MATCH)));
				throw new TransactionExceptionMessage(format(ACCOUNT_MATCH));
			}
			return accuntsValidationFlag = true;
		}
		else {
			throw new TransactionExceptionMessage(format(TRANS_AMOUNT_VALUE,amountToTransfer));
		}
	}

	private Account getAccount(String accountNumber, String errorReason) {
		Account ret = accountsRepository.getAccount((accountNumber));
		if (ret == null) {
			log.info(format(errorReason, accountNumber));
			throw new TransactionExceptionMessage(format(errorReason, accountNumber));
		}
		return ret;
	}

	public BigDecimal checkBalance(String accountNumberFrom){
		Account accountFrom = accountsRepository.getAccount(accountNumberFrom);
		Lock accountReadLock = accountFrom.getLock().readLock();
		accountReadLock.lock();
		BigDecimal balance = accountFrom.getBalance();
		accountReadLock.unlock();
		return balance;
	}

	boolean withdrawal(Account accountFrom,Account accountTo,BigDecimal amountToTransfer)
	{ 
		Lock accountWithdrwalLock = accountFrom.getLock().writeLock();
		accountWithdrwalLock.lock();
		if(accountFrom.getBalance().compareTo(amountToTransfer)  <= - 1){
			accountWithdrwalLock.unlock();
			log.info("withdrawal {} : withdrawal Failed");
			return false;
		}
		accountFrom.setBalance(accountFrom.getBalance().subtract(amountToTransfer));
		log.info("withdrawal {} : Frome account balance after withdrawal " +accountFrom.getBalance());
		new EmailNotificationService().notifyAboutTransfer(accountFrom,
				"amount of "+amountToTransfer+" got debited from " +accountFrom.getAccountId()+
				" and credited to "+ accountTo.getAccountId());
		accountWithdrwalLock.unlock();
		log.info("withdrawal {} : withdrawal Completed");
		return true;

	}

	boolean deposit(Account accountFrom,Account accountTo,BigDecimal amountToTransfer)
	{
		Lock accountDepositLock = accountTo.getLock().writeLock();
		accountDepositLock.lock();
		accountTo.setBalance(accountTo.getBalance().add(amountToTransfer));
		log.info("deposit {} : To account balance after deposit " +accountTo.getBalance());
		new EmailNotificationService().notifyAboutTransfer(accountTo,
				"amount of "+amountToTransfer+" got credited to "+accountTo.getAccountId()
				+" from account " +accountFrom.getAccountId());
		accountDepositLock.unlock();
		log.info("deposit {} : deposit Completed");
		return true;
	}

}
