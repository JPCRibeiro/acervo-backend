package br.app.acervo;

import org.springframework.boot.SpringApplication;

public class TestAcervoBackendApplication {

	public static void main(String[] args) {
		SpringApplication.from(AcervoBackendApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
