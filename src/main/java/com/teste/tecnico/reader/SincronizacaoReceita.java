/*
Cenário de Negócio:
Todo dia útil por volta das 6 horas da manhã um colaborador da retaguarda do Sicredi recebe e organiza as informações de contas para enviar ao Banco Central. Todas agencias e cooperativas enviam arquivos Excel à Retaguarda. Hoje o Sicredi já possiu mais de 4 milhões de contas ativas.
Esse usuário da retaguarda exporta manualmente os dados em um arquivo CSV para ser enviada para a Receita Federal, antes as 10:00 da manhã na abertura das agências.

Requisito:
Usar o "serviço da receita" (fake) para processamento automático do arquivo.

Funcionalidade:
0. Criar uma aplicação SprintBoot standalone. Exemplo: java -jar SincronizacaoReceita <input-file>
1. Processa um arquivo CSV de entrada com o formato abaixo.
2. Envia a atualização para a Receita através do serviço (SIMULADO pela classe ReceitaService).
3. Retorna um arquivo com o resultado do envio da atualização da Receita. Mesmo formato adicionando o resultado em uma nova coluna.


Formato CSV:
agencia;conta;saldo;status
0101;12225-6;100,00;A
0101;12226-8;3200,50;A
3202;40011-1;-35,12;I
3202;54001-2;0,00;P
3202;00321-2;34500,00;B
...

*/
package com.teste.tecnico.reader;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.NumberFormat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;

import com.teste.tecnico.service.ReceitaService;

//TODO: Seria interessante dividir a rotina em dois processos, o primeiro que seria somente a leitura e os dados fossem enviados à um outro processo com processamento em stream
// que realizaria o processamento na classe da ReceitaService e armazenaria no arquivo de resposta.
public class SincronizacaoReceita {

	private static final String ARQUIVO_RESPOSTA = "E:\\Documentos\\receita_resposta.csv";
	
	@Autowired
	private ReceitaService receitaService;
	
	private NumberFormat nf;
	
	
	public SincronizacaoReceita() {
		nf = NumberFormat.getInstance();
	}
	
	/**
	 * Realiza a leitura do arquivo e cria o arquivo de resposta.
	 * 
	 * OBS: Melhorar implementando processamento com Stream, em que após a leitura do arquivo , outro processo realizaria o processamento com a receita e o incremento no arquivo de resposta
	 * @param fileName
	 * @throws IOException
	 */
	public void processarArquivo(String fileName) throws IOException {
			Path path = Paths.get(fileName);
		try (AsynchronousFileChannel asyncChannel = AsynchronousFileChannel.open(path, StandardOpenOption.READ)){
			int fileSize = (int) asyncChannel.size();
			ByteBuffer buffer = ByteBuffer.allocate(fileSize);
			 asyncChannel.read(buffer, 0, buffer, new CompletionHandler<Integer, ByteBuffer>() {
			        @Override
			        public void completed(Integer result, ByteBuffer attachment) {
			        	
						try (PrintWriter f0 = new PrintWriter(new FileWriter(ARQUIVO_RESPOSTA))) {
							f0.println("agencia;conta;saldo;status;resultado");
							attachment.flip();
							byte[] data = new byte[attachment.limit()];
							attachment.get(data);
							String[] transactions = new String(data).split("\r\n");
							attachment.clear();
							for (int i = 1; i < transactions.length; i++) {
								processarTransacao(transactions[i], f0);
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
			        }

			        @Override
			        public void failed(Throwable ex, ByteBuffer attachment) {

			        }            
			      });
		}

	}

	@Async
	private void processarTransacao(String line, PrintWriter f0) {
		if (!line.isEmpty()) {
			String[] infoConta = line.split(";");
			System.out.println(infoConta[0]);
			try {
				boolean resultado = receitaService.atualizarConta(infoConta[0], infoConta[1].replace("-", ""),
						nf.parse(infoConta[2]).doubleValue(), infoConta[3]);
				f0.println(line.concat(";").concat(resultado ? "A" : "R"));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
}
