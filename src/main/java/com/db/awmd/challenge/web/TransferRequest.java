package com.db.awmd.challenge.web;

import java.math.BigDecimal;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class TransferRequest {
	
	 @NotNull(message="{fromAccountId cannot be null or empty}")
	  @NotEmpty(message="{fromAccountId cannot be null or empty}")
	  private final String fromAccountId;
	 
	 @NotNull(message="{toAccountId cannot be null or empty}")
	  @NotEmpty(message="{toAccountId cannot be null or empty}")
	  private final String toAccountId;

	  @NotNull
	  @Min(value = 0, message = "{Initial balance must be positive}")
	  private BigDecimal amount;



	  @JsonCreator
	  public TransferRequest(@JsonProperty("fromAccountId") String fromAccountId,@JsonProperty("toAccountId") String toAccoountId,@JsonProperty("amount") BigDecimal amount){
	  this.fromAccountId = fromAccountId;
	    this.toAccountId=toAccoountId;
	    this.amount = amount;
	  }
	

}
