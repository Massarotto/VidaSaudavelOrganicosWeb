/**
 * 
 */
package jobs;

import java.util.List;

import models.ProdutoEstoque;

import org.apache.commons.lang.StringUtils;

import controllers.Mail;
import exception.SystemException;

import business.estoque.EstoqueControl;
import play.Logger;
import play.i18n.Messages;
import play.jobs.Every;
import play.jobs.Job;
import play.jobs.On;
import play.jobs.OnApplicationStart;
import relatorios.parse.EstoqueParse;

/**
 * 
 * @author Felipe Guerra
 * @since 29/05/2012
 */
@Every("6h")
public class EstoqueManagerJob extends Job {
	
	@Override
	public void doJob() throws SystemException {
		String minEstoque = Messages.get("application.min.estoque", "");
		String destinatarios = Messages.get("application.email.admin", "");
		String staticContent = Messages.get("application.static.content", "") + Messages.get("application.path.public.images", "");
		
		if(StringUtils.isEmpty(minEstoque))
			minEstoque = "1";
		
		List<ProdutoEstoque> estoque = EstoqueControl.findProdutoEstoque(Integer.parseInt(minEstoque));
		
		if(!estoque.isEmpty())
			Mail.estoqueControl("Relatório de Posição de Estoque", Mail.EMAIL_ADMIN, staticContent, EstoqueParse.buildHtmlLayout(estoque), destinatarios.split(","));
	}

	@Override
	public void onException(Throwable t) {
		Logger.error(t, "Ocorreu um erro no robô de controle do estoque.", "");
		
		Mail.sendError("Ocorreu um erro no robô de controle do estoque.", Mail.EMAIL_ADMIN, Mail.EMAIL_ADMIN, new Exception(t));
	}
	
}
