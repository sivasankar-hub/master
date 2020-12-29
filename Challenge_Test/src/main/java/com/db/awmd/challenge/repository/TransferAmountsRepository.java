package com.db.awmd.challenge.repository;

import com.db.awmd.challenge.domain.TransferAccountRequest;

public interface TransferAmountsRepository {
	Object transferAmount(TransferAccountRequest transferAccountRequest);
}
