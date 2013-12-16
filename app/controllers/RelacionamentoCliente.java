/**
 * 
 */
package controllers;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import models.Cliente;
import models.CupomDesconto;
import models.Desconto;
import models.Pedido;
import models.Usuario;

import org.apache.commons.lang.StringUtils;

import play.Logger;
import play.data.validation.Required;
import play.db.jpa.JPA;
import play.db.jpa.Transactional;
import play.i18n.Messages;
import play.mvc.Before;
import exception.SystemException;

/**
 * <p>Controller para todas as funcionalidades de CRM</p>
 * @author Felipe Guerra
 * @version 1.0
 */
public class RelacionamentoCliente extends BaseController {
	
	@Before
	static void estaAutorizado() {
		Logger.debug("####### Verificar se o usuário autenticado é admin... ########");
		
		if( (StringUtils.isEmpty(session.get("isAdmin")) || Boolean.FALSE.equals(Boolean.valueOf(session.get("isAdmin")))) 
			&& (StringUtils.isEmpty(session.get("isEmployee")) && Boolean.FALSE.equals(Boolean.valueOf(session.get("isEmployee")))) ) 
		{
			Logger.debug("####### Usuário não autorizado a acessar essa funcionalidade...%s ########", session.get("usuarioAutenticado"));
			
			Home.index("Usuário não autorizado a acessar essa funcionalidade.");
		}
	}
	
	public static void index(String message, Desconto desconto) {
		if(desconto==null)
			desconto = new Desconto(BigDecimal.ZERO); 
		
		render(desconto, message);
	}
	
	@Transactional(readOnly=false)
	public static void gerarCuponsCliente(@Required(message="Por favor, especifique o desconto") Desconto desconto, 
										@Required(message="Por favor, preencha a validade do cupom") Integer validadeCupomEmDias,
										@Required(message="Por favor, preencha a qtde máxima de uso do cupom") Integer qtdMaxUsoCupom,
										Boolean todosClientes) {
		CupomDesconto cupom = null;
		String result = null;
		boolean hasSent = false;
		Query query = null;
		List<Cliente> clientes = null;
		String subject = null;
		int contador = 0;
		Usuario usuarioLogado = null;
		
		if(validation.hasErrors()) {
			params.flash();
			validation.keep();
			
			index(result, desconto);
			
		}else {
			if(todosClientes==null) {
				query = JPA.em().createNamedQuery("pesquisarClienteComAlgumPedidoValido");
				query.setParameter("parameters", Pedido.PedidoEstado.FINALIZADO);
				clientes = query.getResultList();
				
			}else {
				clientes = Cliente.find("usuario.recebeMail = ? and id in (select cliente.id from CupomDesconto where ativo = ?) order by id DESC", Boolean.TRUE, Boolean.FALSE).fetch();
				
			}
			usuarioLogado = Usuario.find("cliente.id = ?", Long.parseLong(session.get("clienteId"))).first();
			result = Messages.get("application.crm.cupom.desconto.success", "");
			subject = Messages.get("application.crm.cupom.desconto.email.subject", "");

			desconto.setDataDesconto(new Date());
			desconto.setUsuario(usuarioLogado);
			
			for(Cliente cliente : clientes) {
				contador++;
				cupom = CupomDesconto.gravarCodigoDescontoParaCliente(cliente, desconto, cliente.getCpf(), session.get("usuarioAutenticado"), validadeCupomEmDias, qtdMaxUsoCupom);
				
				try {
					Mail.enviarCupomDescontoCliente(subject, Mail.EMAIL_CONTACT, cupom, cliente.getUsuario().getEmail());
					hasSent = true;
					
				}catch(SystemException sysex) {
					Logger.error(sysex, "Erro ao enviar o e-mail com o cupom para o cliente: ", cliente.getNome());
				}
				if(contador%50==0) {
					JPA.em().flush();
					JPA.em().getTransaction().commit();
					JPA.em().getTransaction().begin();
				}
			}

			if(!hasSent)
				result = Messages.get("application.crm.cupom.desconto.fail", "");
			
			index(result, null);
		}
	}

}
