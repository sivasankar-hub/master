package com.db.awmd.challenge.service;

import com.db.awmd.challenge.domain.TransferAccountRequest;
import com.db.awmd.challenge.repository.TransferAmountsRepository;
import lombok.Getter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TransferAmountService {

	@Getter
	private final TransferAmountsRepository transferAmountsRepository;

	@Autowired
	public TransferAmountService(TransferAmountsRepository transferAmountsRepository) {
		this.transferAmountsRepository = transferAmountsRepository;
	}

	public Object transferAmount(TransferAccountRequest transferAccountRequest) {
		return this.transferAmountsRepository.transferAmount(transferAccountRequest);
	}

}
