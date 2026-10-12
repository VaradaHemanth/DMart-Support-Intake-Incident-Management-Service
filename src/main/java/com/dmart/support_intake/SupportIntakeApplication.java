	package com.dmart.support_intake;
	
	import org.springframework.boot.SpringApplication;
	import org.springframework.boot.autoconfigure.SpringBootApplication;
	import org.springframework.scheduling.annotation.EnableScheduling;
	@EnableScheduling
	@SpringBootApplication
	public class SupportIntakeApplication {
	
		public static void main(String[] args) {
			SpringApplication.run(SupportIntakeApplication.class, args);
		}
	
	}
