package com.coderscampus.backgammon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BackgammonApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackgammonApplication.class, args);
	}

}
