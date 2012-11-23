/**
 * 
 */
package jobs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

import play.Logger;
import play.jobs.Job;
import play.jobs.On;
import controllers.Mail;
import controllers.Relatorios;

/**
 * @author guerrafe
 *
 */
@On("0 10 23 ? * Sun")
public class RelatorioSantosOrganicos extends Job {
	
	public void doJob() {
		enviarRelatorioPedidos();
		enviarRelatorioProdutosPorFornecedor();
	}
	
	public void enviarRelatorioProdutosPorFornecedor() {
		InputStream csv = null;
		String pathReport = null;
		OutputStream outputStream = null;
		File report = null;
		
		try {
			Logger.info("### Início do processo de envio de e-mail automático com o Relatório de Produtos por Fornecedor. ###", "");
			csv = Relatorios.generateRelatorioProdutoFornecedorCSV();
			
			Logger.info("### Relatório gerado? [%s - %s] ###", this.getClass().getName(), csv);
			
			if(csv!=null) {
				pathReport = System.getProperty("java.io.tmpdir") + File.separatorChar + Relatorios.RELATORIO_PRODUTO_FORNECEDOR + "_" + new Date().getTime()+ ".csv";
				outputStream = new FileOutputStream(pathReport);

				int read;
				
				byte[] bytes = new byte[1024];
	
				while((read = csv.read(bytes))>-1) {
					outputStream.write(bytes, 0, read);
				}
				
				report = new File(pathReport);
				
				Logger.warn("### Gerou o Relatório CSV? %s | path: %s ###", report.exists(), report.getAbsolutePath());
	
				csv.close();
				outputStream.flush();
				outputStream.close();
				
				Mail.sendRelatorioPedidosAguardandoEntrega("Relatório de Produtos por Fornecedor", 
															"Vida Saudável Orgânicos<administrador@vidasaudavelorganicos.com.br>", 
															report.getAbsolutePath(), report.getName(),
															"alexandre@vidasaudavelorganicos.com.br" 
															//"klauss@santosorganicos.com.br"
															);
			}
			
		}catch(Exception e) {
			Logger.error(e, "Erro no processo de envio de e-mail automático com o Relatório de Produtos Fornecedor.");
			
		}finally {
			Logger.info("### Fim do processo de envio de e-mail automático com o Relatório de Produtos Fornecedor. ###", "");
		}
		
	}
	
	private void enviarRelatorioPedidos() {
		String pathReport = null;
		InputStream pdfReader = null;
		OutputStream outputStream = null;
		File report = null;
		
		try {
			Logger.info("### Início do processo de envio de e-mail automático com o Relatório de Pedidos Aguardando Entrega. ###", "");
			pdfReader = Relatorios.exportarRelatorioPdf();
			pathReport = System.getProperty("java.io.tmpdir") + File.separatorChar + "RELATORIO_PEDIDOSPRODUTO_ENTREGA_" + new Date().getTime()+ ".pdf";
			outputStream = new FileOutputStream(pathReport);
			
			int read;
			
			byte[] bytes = new byte[1024];

			while((read = pdfReader.read(bytes))>-1) {
				outputStream.write(bytes, 0, read);
			}
			
			report = new File(pathReport);
			
			Logger.warn("### Gerou o PDF? %s ###", report.exists());

			pdfReader.close();
			outputStream.flush();
			outputStream.close();
			
			Mail.sendRelatorioPedidosAguardandoEntrega("Relatório de Pedidos Aguardando Entrega", Mail.EMAIL_ADMIN, 
														report.getAbsolutePath(), report.getName(),
														"alexandre@vidasaudavelorganicos.com.br");
			
		}catch(Throwable e) {
			Logger.error(e, "Erro no processo de envio de e-mail automático com o Relatório de Pedidos Aguardando Entrega.");
			
		}finally {
			Logger.info("### Fim do processo de envio de e-mail automático com o Relatório de Pedidos Aguardando Entrega. ###", "");
		}
	}

}
