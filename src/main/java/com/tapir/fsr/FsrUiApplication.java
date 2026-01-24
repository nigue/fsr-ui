package com.tapir.fsr;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class FsrUiApplication {

	public static void main(String[] args) {
		SpringApplication.run(FsrUiApplication.class, args);
	}

}
