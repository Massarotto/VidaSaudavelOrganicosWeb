/**
 * 
 */
package util;

import java.io.File;
import java.io.FileOutputStream;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import models.Cliente;
import models.Pedido;

import org.apache.commons.io.IOUtils;

import play.Logger;
import controllers.Mail;
import controllers.Relatorios;
import exception.SystemException;

/**
 * <p>
 * 	Essa classe tem por objetivo abstrair o processamento pesado da 
 * aplicação utilizando o pacote <b>concurrent</b> {@link java.util.concurrent} do Java.
 * </p>
 * @author Felipe Guerra
 * @since 03/12/2013
 * @version 1.0
 */
public class ProcessamentoConcorrenteUtil {
	
	private final ExecutorService pool = Executors.newFixedThreadPool(10);
	
	private Pedido pedido = null;
	
	private Cliente cliente = null;

	public ProcessamentoConcorrenteUtil() {
		// TODO Auto-generated constructor stub
	}
	
	public ProcessamentoConcorrenteUtil(Pedido pedido, Cliente cliente) {
		this.pedido = pedido;
		this.cliente = cliente;
	}
	
	public Future<Boolean> gerarNotaPedidoEnviarPorEmail() {
		return pool.submit(new Callable<Boolean>() {
			@Override
			public Boolean call() throws Exception {
				String caminhoArquivo = null;
				FileOutputStream fis = null;
				File tempFile = null;
				
				try {
					StringBuffer email = new StringBuffer();
					email.append("Vida Saudável Orgânicos");
					email.append("<").append("contato@vidasaudavelorganicos.com.br").append(">");
					
					tempFile = File.createTempFile(Relatorios.REPORT_TITLE, ".pdf");
					tempFile.deleteOnExit();
					fis = new FileOutputStream(tempFile);
					
					IOUtils.copy(Relatorios.gerarNotaFiscalPedido(pedido.id), fis);
					caminhoArquivo = tempFile.getAbsolutePath();
					
					Logger.info("Valor Desconto %s", pedido.getValorDesconto());
					Logger.info("###### E-mail de confirmação do pedido para: %s #######", cliente.getUsuario().getEmail());
					
					Mail.pedidoFinalizado(
							"Pedido Finalizado",
							email.toString(), 
							pedido,
							caminhoArquivo,
							cliente.getUsuario().getEmail(), Mail.EMAIL_CONTACT);
		
					Logger.info("###### E-mail de confirmação do pedido para: %s enviado. #######", cliente.getUsuario().getEmail());
										
				} catch (SystemException ex) {
					Logger.error("Erro ao enviar o e-mail de confirmação para %s", cliente.getUsuario().getEmail(), ex);
					
				}finally {
					if(fis!=null)
						fis.close();
				}
				return Boolean.TRUE;
			}
		});
	}
}
