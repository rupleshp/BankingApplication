package com.db.awmd.challenge.domain;

import java.math.BigDecimal;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransferMoneyModel {
	
	@NotNull
	@NotEmpty
	String accountFromId;
	
	@NotNull
	@NotEmpty
	String accountToId;
	
	@Min(value = 1, message = "Amount to transfer should be greter than 0.")
	BigDecimal amount;
}
