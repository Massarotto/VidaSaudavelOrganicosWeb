/**
 * 
 */
package controllers;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import models.Cliente;
import models.Pedido;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.mail.EmailAttachment;

import play.Logger;
import play.mvc.Mailer;
import exception.SystemException;

/**
 * @author guerrafe
 *
 */
public class Mail extends Mailer {
	
	public static final String EMAIL_ADMIN = "Administrador<administrador@vidasaudavelorganicos.com.br>";
	
	public static final String EMAIL_CONTACT = "Contato<contato@vidasaudavelorganicos.com.br>";
	
	public static void sendEmail(String subject,
								String from,
								String to, 
								Cliente cliente) throws SystemException {
		try {
			setFrom(from);
			setSubject(subject);
			addRecipient(to);
			send(cliente);
		
		}catch(Exception e) {
			Logger.error("Erro no envio do e-mail.", e);
			throw new SystemException(e);
		}
	}
	
	public static void sendCadastroAprovado(String subject,
								String from,
								String to, 
								Cliente cliente) throws SystemException {
		try {
			setFrom(from);
			setSubject(subject);
			addRecipient(to);
			
			cliente.getUsuario().decryptPassword();
			
			send(cliente);
			
		}catch(Exception e) {
			Logger.error(e, "Erro no envio do e-mail de cadastro aprovado.");
			throw new SystemException(e);
		}
	}
	
	public static void pedidoFinalizado(String subject,
										String from,
										Pedido pedido,
										String... to) throws SystemException {
		try {
			setFrom(from);
			setSubject(subject);
			addRecipient(to[0]);
			addBcc(to[to.length-1]);
			setReplyTo(EMAIL_CONTACT);
			
			send(pedido);
			
		}catch(Exception e) {
			Logger.error(e, "Erro no envio do e-mail de pedido finalizado.");
			throw new SystemException(e);
		}

	}
	
	public static void sendError(String subject, String from, String to, Exception e) {
		try {
			setFrom(from);
			setSubject(subject);
			addRecipient(to);
			
			send(e);
		
		}catch(Throwable ex) {
			Logger.error(ex, "Erro ao enviar e-mail com a exceção de sistema.");
		}
		
	}
	
	public static void sendFaleConosco(String subject, String from, String nome, String email, String message) throws SystemException {
		try {
			setFrom(from);
			setSubject(subject);
			setReplyTo(from);
			addRecipient(email);
			
			send(message, nome);
			
		}catch(Exception e) {
			Logger.error(e, "Erro ao enviar e-mail do Fale Conosco.");
			throw new SystemException(e);
		}
	}
	
	public static void newFaleConosco(String subject, String from, String nome, String email, String message) throws SystemException {
		try {
			setFrom(from);
			setSubject(subject);
			setReplyTo(from);
			addRecipient(from);
			
			send(message, nome, email);
			
		}catch(Exception e) {
			Logger.error(e, "Erro ao enviar e-mail do Fale Conosco.");
			throw new SystemException(e);
		}
	}
	
	public static void sendRelatorioPedidosAguardandoEntrega(String subject, String from, 
															String fileAbsolutePath,
															String message,
															String... email) throws SystemException {
		try {
			setFrom(from);
			
			EmailAttachment attachments = new EmailAttachment();
			attachments.setDescription("Relatório Pedidos Aguardanto Entrega.");
			attachments.setPath(fileAbsolutePath);
			
			addAttachment(attachments);
			
			setSubject(subject);
			addRecipient(email);
			
			send(message);
			
		}catch(Exception e) {
			Logger.error(e, "Erro ao enviar e-mail com o relatório de Pedidos Aguardando Entrega.");
			throw new SystemException(e);
		}
	}
	
	public static void emailMarketVidaSaudavelOrganicos(String subject, String from, 
														String message, String email,
														String urlImagem) throws SystemException {
		try {
			setFrom(from);
			setSubject(subject);
			addRecipient(email);
			setReplyTo(from);
			
			Future<Boolean> sent = send(message, urlImagem);
			
			Logger.debug("Atingiu o timeout? %s",  sent.get(5, TimeUnit.SECONDS) );
			
		}catch(Exception e) {
			Logger.error(e, "Erro ao enviar e-mail com o Marketing.");
			throw new SystemException(e);
		}
	}
	
	public static void estoqueControl(String subject, 
									String from,  
									String staticContent,
									String content,
									String... email) throws SystemException {
		try {
			setFrom(from);
			setSubject(subject);
			addRecipient(email);
			setReplyTo(from);
			send(staticContent, content);
			
		}catch(Exception e) {
			Logger.error(e, "Erro ao tentar enviar e-mail com o Relatório de Posição de Estoque.");
			throw new SystemException(e);
		}
		
	}
	
	public static void sendProdutosNaoEncontrados(String subject, String from, 
											String fileAbsolutePath,
											String message,
											String... email) throws SystemException {
		try {
			setFrom(from);
			
			if(!StringUtils.isEmpty(fileAbsolutePath)) {
				EmailAttachment attachments = new EmailAttachment();
				attachments.setDescription("Relatório Pedidos Aguardanto Entrega.");
				attachments.setPath(fileAbsolutePath);
				
				addAttachment(attachments);
			}
			setSubject(subject);
			addRecipient(email);
			
			send(message);
			
		}catch(Exception e) {
			Logger.error(e, "Erro ao enviar e-mail com os produtos não encontrados.");
			throw new SystemException(e);
		}
	}

}
