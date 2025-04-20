package com.java.kokodi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
public class KokodiApplication {

	public static void main(String[] args) {
		SpringApplication.run(KokodiApplication.class, args);
	}

}
