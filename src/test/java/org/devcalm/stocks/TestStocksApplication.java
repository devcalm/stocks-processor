package org.devcalm.stocks;

import org.springframework.boot.SpringApplication;

public class TestStocksApplication {

	public static void main(String[] args) {
		SpringApplication.from(App::main).with(TestcontainersConfiguration.class).run(args);
	}

}
