package com.db.awmd.challenge.domain;

import java.math.BigDecimal;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class Account implements Comparable<Account>{
	
	public Lock lock=new ReentrantLock(true);
	

  
  @NotEmpty(message="{account.accountId.NotEmpty}")
  private final String accountId;

  @NotNull(message = "{account.balance.notNull}")
  @Min(value = 0, message = "{account.balance.min}")
  private  BigDecimal balance;
  
  
  public Account(String accountId) {
    this.accountId = accountId;
    this.balance = BigDecimal.ZERO;
   
  }

  @JsonCreator
  public Account(@JsonProperty("accountId") String accountId,
    @JsonProperty("balance") BigDecimal balance) {
    this.accountId = accountId;
    this.balance = balance;
  }

@Override
public int compareTo(Account account2) {
	// TODO Auto-generated method stub
	return this.getAccountId().compareTo(account2.getAccountId());
}




  


}
