package com.db.awmd.challenge.domain;

import java.math.BigDecimal;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import org.hibernate.validator.constraints.NotEmpty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class TransferAccountRequest {

	@NotNull
	@NotEmpty
	private String fromAccountNumber;
	@NotNull
	@NotEmpty
	private String toAccountNumber;

	@NotNull
	@Min(value = 1 , message = "Transfer Amount must be positive value.")
	private BigDecimal amount;
	@JsonCreator
	public TransferAccountRequest(@JsonProperty("fromAccountNumber") String fromAccountNumber,
			@JsonProperty("toAccountNumber") String toAccountNumber,
			@JsonProperty("amount") BigDecimal amount) {
		this.fromAccountNumber = fromAccountNumber;
		this.toAccountNumber = toAccountNumber;
		this.amount = amount;
	}
}
