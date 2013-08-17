package jobs;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.Days;

import controllers.Mail;

import models.CestaAssinatura;
import models.Pedido;

import play.Logger;
import play.jobs.Job;
import play.jobs.On;
import play.jobs.OnApplicationStart;

@SuppressWarnings("all")
public class CestaAssinaturaJob extends Job {

	public void doJob() throws Exception {
		gerarPedidos();
	}
	
	public void gerarPedidos() {
		Logger.info("### Início do processo de geração dos pedidos com os produtos das assinaturas das cestas ###", "");
		List<CestaAssinatura> cestasAtivas = CestaAssinatura.find("ativo = ?", Boolean.TRUE).fetch();
		
		if(!cestasAtivas.isEmpty()) {
			Calendar dataAtual = new GregorianCalendar();
			Pedido pedido = null;
			
			for(CestaAssinatura cesta : cestasAtivas) {
				//Só por garantia
				Calendar dataEnvioCesta = new GregorianCalendar();
				dataEnvioCesta.setTime(cesta.getDataEnvio());
				
				DateTime arg0 = new DateTime(dataAtual);
				DateTime arg1 = new DateTime(dataEnvioCesta);
				
				if(Days.daysBetween(arg0, arg1).getDays()<7) {
					pedido = Pedido.newPedido(cesta);
					
					pedido.save();
					
					cesta.addDiasDataEnvio(cesta.getPeriodo().getPeriodicidade());
					
					cesta.save();
					Logger.info("### Pedido gerado para a cesta. Cliente %s ###", cesta.getCliente().getNome());
				}
			}
		}
		Logger.info("### Fim do processo de geração dos pedidos com os produtos das assinaturas das cestas ###", "");
	}
	
	@Override
	public void onException(Throwable e) {
		Logger.error(e, "Erro no processo de geração dos pedidos com os produtos das assinaturas das cestas.");
		
		Mail.sendError("Erro no processo de geração dos pedidos com os produtos das assinaturas das cestas.", Mail.EMAIL_ADMIN, Mail.EMAIL_CONTACT, new Exception(e));
	}

}
