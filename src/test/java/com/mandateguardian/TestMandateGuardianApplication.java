package com.mandateguardian;

import org.springframework.boot.SpringApplication;

public class TestMandateGuardianApplication {

	public static void main(String[] args) {
		SpringApplication.from(MandateGuardianApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
