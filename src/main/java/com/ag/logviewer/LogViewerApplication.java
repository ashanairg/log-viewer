package com.ag.logviewer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class LogViewerApplication {

	public static void main(String[] args) {
		System.out.println("******** Server is up and ready to serve logs *******");
		SpringApplication.run(LogViewerApplication.class, args);
	}

}
