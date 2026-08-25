package com.tlali.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class TlaliApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(TlaliApiApplication.class, args);
	}

}
