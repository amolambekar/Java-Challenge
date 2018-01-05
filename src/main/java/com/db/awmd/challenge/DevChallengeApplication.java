package com.db.awmd.challenge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
@PropertySource("classpath:ValidationMessages.properties")
public class DevChallengeApplication {
	
	@Bean
	public static PropertySourcesPlaceholderConfigurer configs() {
	        return new PropertySourcesPlaceholderConfigurer();
	    }
	
  public static void main(String[] args) {
    SpringApplication.run(DevChallengeApplication.class, args);
  }
}
