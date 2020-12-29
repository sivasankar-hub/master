package com.db.awmd.challenge.repository;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.db.awmd.challenge.domain.TransferAccountRequest;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Repository
@Slf4j
public class TransferAmountsRepositoryInMemory implements TransferAmountsRepository {

	@Getter
	private final AccountsRepository accountsRepository;

	@Autowired
	TransferAmountsRepositoryInMemory(AccountsRepository accountsRepository)
	{
		this.accountsRepository= accountsRepository;
	}
	@Override
	public Object transferAmount(TransferAccountRequest transferAccountRequest) {
		log.info("Retrieving transaction information from TransferAccountRequest json object");
		String fromAccountNumber = transferAccountRequest.getFromAccountNumber();
		String toAccountNumber = transferAccountRequest.getToAccountNumber();
		BigDecimal amount = transferAccountRequest.getAmount();
		return new TransferAmountsRepositoryImpl(accountsRepository).transferAmount(fromAccountNumber,toAccountNumber,amount);
	}
}
