package com.teste.tecnico;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.teste.tecnico.reader.SincronizacaoReceita;

@SpringBootApplication
public class TesteTecnicoApplication implements ApplicationRunner {

	public static void main(String[] args) {
		SpringApplication.run(TesteTecnicoApplication.class, args);
	}
	
	@Bean
	public SincronizacaoReceita sincronizacaoReceita(){
		return new SincronizacaoReceita();
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {
//		sincronizacaoReceita().processarArquivo(args.getSourceArgs()[0]);
		sincronizacaoReceita().processarArquivo("E:\\Documentos\\receita.csv");
	}

}
