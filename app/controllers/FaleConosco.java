/**
 * 
 */
package controllers;

import play.Logger;
import play.data.validation.Required;
import exception.SystemException;

/**
 * 
 * @author Felipe G. de Oliveira
 *
 */
public class FaleConosco extends BaseController {

	public static void index(String nome, String assunto, String email, String mensagem, String result) {
		render(nome, assunto, email, mensagem, result);
	}
	
	public static void enviarFaleConosco(@Required String nome, @Required String assunto,
									@Required String email, @Required String mensagem) throws SystemException {
		Logger.debug("#### Início - Fale Conosco: %s ####", nome);
		
		validation.valid(nome);
		validation.valid(assunto);
		validation.valid(email);
		validation.valid(mensagem);
		validation.email(email);
		
		if(validation.hasErrors()) {
			params.flash();
			validation.keep();
			
			index(nome, assunto, email, mensagem, null);
		}else {
			Logger.debug("#### Vai enviar o Fale Conosco: %s ####", nome);
			
			Mail.sendFaleConosco(assunto, "Contato<contato@vidasaudavelorganicos.com.br>", nome, email, mensagem);
			Mail.newFaleConosco(assunto, "Contato<contato@vidasaudavelorganicos.com.br>", nome, email, mensagem);
			
			Logger.debug("#### Fim enviar o Fale Conosco: %s ####", nome);
		}
		index(null,null,null,null, "message_success");
	}
	
}
