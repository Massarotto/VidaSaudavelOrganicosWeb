/**
 * 
 */
package controllers;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import jobs.CestaAssinaturaJob;
import models.Cesta;
import models.CestaAssinatura;
import models.CestaAssinatura.CestaAssinaturaPeriodo;
import models.CestaAssinaturaProduto;
import models.CestaProduto;
import models.Cliente;
import models.Endereco;
import models.FormaPagamento;
import models.Pagamento;
import models.Produto;
import play.Logger;
import play.cache.Cache;
import play.data.validation.Valid;
import play.db.jpa.JPA;
import play.db.jpa.Transactional;
import play.i18n.Messages;
import play.modules.paginate.ValuePaginator;
import play.mvc.Before;
import util.CestaAssinaturaProdutoComparator;
import util.CestaProdutoSerializer;
import util.PeriodoEntrega;


/**
 * @author guerrafe
 *
 */
public class Cestas extends BaseController {
	
	@Before(only={"salvar", "changeStatusCesta", "show", "cadastrar", "editar"}, priority=0)
	static void estaAutorizado() {
		Logger.debug("####### Verificar se o usuário autenticado é admin... ########");
		
		if(!session.contains("isAdmin") || Boolean.valueOf(session.get("isAdmin"))==Boolean.FALSE) {
			Logger.debug("####### Usuário não autorizado a acessar essa funcionalidade...%s ########", session.get("usuarioAutenticado"));
			
			Home.index("Usuário não autorizado a acessar essa funcionalidade.");
		}
	}
	
	@Before(only={"cestaAtiva", "consultaCestaCliente", "associar", "finalizar"}, priority=1)
	static void estaAutenticado() {
		Logger.debug("####### Verificar se o usuário está autenticado para assinar uma cesta... ########");
		
		if(session.get("usuarioAutenticado")==null) {
			session.put("assinatura", request.path);
			
			Login.index(null, null, Messages.get("application.message.notAuthenticated", ""));
		}
	}
	
	public static void visualizar() {
		render();
	}
	
	public static void consultaCestaCliente(Long idCliente) {
		List<CestaAssinatura> cestas = CestaAssinatura.find("cliente.id = ?", idCliente).fetch();
		
		render(cestas);
	}
	
	@Transactional(readOnly=false)
	public static void changeStatus(Long id) {
		CestaAssinatura cestaAssinatura = CestaAssinatura.findById(id);
		cestaAssinatura.setAtivo(!cestaAssinatura.getAtivo());
		
		cestaAssinatura.save();
		
		Query query = JPA.em().createQuery("update CestaAssinatura set ativo = 0 where id <>:id");
		query.setParameter("id", id);
		
		query.executeUpdate();
		
		consultaCestaCliente(cestaAssinatura.getCliente().id);
	}
	
	@Transactional(readOnly=false)
	public static void changeStatusAssinatura(Long id, Boolean assinaturas) {
		CestaAssinatura cestaAssinatura = CestaAssinatura.findById(id);
		cestaAssinatura.setAtivo(!cestaAssinatura.getAtivo());
		
		cestaAssinatura.save();
		
		assinaturas();
	}
	
	public static void cestaAtiva(String message) {
		CestaAssinatura cestaAssinatura = new CestaAssinatura();
		cestaAssinatura.assignDataInicio();
		
		Cesta cesta = Cesta.find("ativo = ?", Boolean.TRUE).first();
		
		render(cesta, cestaAssinatura, message);
	}
	
	public static void produtosDisponiveis(Long idCesta) {
		List<CestaProduto> produtoPrincipais = CestaProduto.find("cesta.id = ? AND opcional = ?", idCesta, Boolean.FALSE).fetch();
		List<CestaProduto> produtosOpcionais = CestaProduto.find("cesta.id = ? AND opcional = ?", idCesta, Boolean.TRUE).fetch();
		
		produtoPrincipais.addAll(produtosOpcionais);
		
		Collections.sort(produtoPrincipais);
		
		renderJSON(produtoPrincipais, new CestaProdutoSerializer());
	}
	
	public static void editar(Long id) {
		List<Produto> produtosAtivos = null;
		List<Produto> produtosDisponiveis = null;
		Cesta cesta = Cesta.findById(id);
		List<Produto> produtos = Produto.find("ativo = ? order by secao.descricao ASC", Boolean.TRUE).fetch();
		
		Query query = JPA.em().createQuery("select cp.produto from CestaProduto as cp where cp.cesta.id =:id AND cp.opcional =:opt");
		query.setParameter("id", cesta.id);
		query.setParameter("opt", Boolean.FALSE);
		
		produtosAtivos = query.getResultList();
		
		Query query2 = JPA.em().createQuery("select cp.produto from CestaProduto as cp where cp.cesta.id =:id AND cp.opcional =:opt");
		query2.setParameter("id", cesta.id);
		query2.setParameter("opt", Boolean.TRUE);
		
		produtosDisponiveis = query2.getResultList();
		
		renderTemplate("Cestas/cadastrar.html", cesta, produtosAtivos, produtosDisponiveis, produtos);
	}

	public static void cadastrar(Cesta cesta) {
		if(cesta==null)
			cesta = new Cesta();
		
		List<Produto> produtosAtivos = Produto.find("ativo = ? order by secao.descricao ASC", Boolean.TRUE).fetch();
		List<Produto> produtosDisponiveis = Produto.find("ativo = ? order by secao.descricao ASC", Boolean.TRUE).fetch();
		
		render(cesta, produtosAtivos, produtosDisponiveis);
	}
	
	public static void show() {
		Logger.debug("#### Consultar todas as cestas...%s ####", "");
		List<Cesta> _cestas = Cesta.findAll();
		ValuePaginator<Cesta> cestas = new ValuePaginator<Cesta>(_cestas);
		cestas.setPageSize(15);
		
		render(cestas);
	}
	
	@Transactional(readOnly=false)
	public static void changeStatusCesta(Long id) {
		Cesta cesta = Cesta.findById(id);
		cesta.setAtivo(!cesta.getAtivo());
		
		cesta.save();
		show();
	}
	
	@Transactional(readOnly=false)
	public static void salvar(@Valid Cesta cesta, List<Long> produtosAtivos, List<Long> produtosDisponiveis) {
		Logger.debug("#### Salvar nova cesta %s ####", cesta.getTitulo());
		
		if(validation.hasErrors()) {
			params.flash();
			validation.keep();
			
			cadastrar(cesta);
			
		}else {
			if(cesta.id==null) {
				cesta.setDataCadastro(new Date());
				cesta.setAtivo(Boolean.TRUE);
			
			}else {
				cesta.getProdutosAtivos().clear();
				cesta.merge();
				
				cesta.setDataAlteracao(new Date());
			}
			
			if(produtosAtivos!=null) {
				for(Long id : produtosAtivos) {
					Produto produto = Produto.findById(id);
					
					Logger.info("#### Produto %s adicionado à cesta? %s ####", produto.getNome(), cesta.addProdutoAtivo(produto, false)) ;
				}
			}
			
			if(produtosDisponiveis!=null) {
				for(Long id : produtosDisponiveis) {
					Produto produto = Produto.findById(id);
					
					Logger.info("#### Produto %s adicionado aos disponíveis? %s ####", produto.getNome(), cesta.addProdutoAtivo(produto, true)) ;
				}
			}
			cesta.save();
			Logger.debug("##### Cesta alterada: %s  #####", cesta.getTitulo());
			show();
		}
	}
	
	public static void associar(List<CestaAssinaturaProduto> produtos) {
		Logger.debug("### Início - Associar a cesta ao usuário %s###", session.get("usuarioAutenticado"));
		Double valorTotalAssinatura = new Double(0);
		
		CestaAssinatura cestaAssinatura = new CestaAssinatura();
		cestaAssinatura.assignDataInicio();
		cestaAssinatura.setDataEnvio(cestaAssinatura.getDataInicio());
		cestaAssinatura.setAtivo(Boolean.TRUE);
	
		for(CestaAssinaturaProduto assinaturaProduto : produtos) {
			CestaProduto cestaProduto = CestaProduto.find("produto.id = ?", assinaturaProduto.getProduto().id).first();
			
			if(!assinaturaProduto.getOpcional())
				valorTotalAssinatura += cestaProduto.getProduto().getValorVenda() * assinaturaProduto.getQuantidade();

			assinaturaProduto.setProduto(cestaProduto.getProduto());
			assinaturaProduto.setCestaAssinatura(cestaAssinatura);
			
			cestaAssinatura.getListCestaAssinaturaProduto().add(assinaturaProduto);
		}
		
		if(!Carrinho.validarValorCompra(valorTotalAssinatura))
			cestaAtiva(Messages.get("message.validation.order.minValue", ""));
		
		else {
			Cliente cliente = Cliente.findById(Long.valueOf(session.get("clienteId")));
			cestaAssinatura.setCliente(cliente);
			cestaAssinatura.setValorCesta(new BigDecimal(valorTotalAssinatura.doubleValue()));
			
			Collections.sort(cestaAssinatura.getListCestaAssinaturaProduto(), new CestaAssinaturaProdutoComparator(Boolean.TRUE));
			
			Cache.add("cesta."+session.get("clienteId"), cestaAssinatura, "40mn");
			
			finalizar(session.get("clienteId"));
		}
		Logger.debug("### Início - Fim a cesta ao usuário %s ###", session.get("usuarioAutenticado"));
	}

	public static void finalizar(String clienteId) {
		CestaAssinatura cestaAssinatura = Cache.get("cesta."+clienteId, CestaAssinatura.class);
		List<CestaAssinatura> cestasCliente = CestaAssinatura.find("ativo = ? AND cliente.id = ?", Boolean.TRUE, Long.valueOf(clienteId)).fetch();
		
		if(cestaAssinatura==null) {
			Cache.safeDelete("cesta."+clienteId);
			cestaAtiva(Messages.get("application.message.basket.empty", ""));
		}
			
		if(!cestasCliente.isEmpty()) {
			Cache.safeDelete("cesta."+clienteId);
			cestaAtiva(Messages.get("form.cesta.hasAssign", ""));
		}
		
		CestaAssinaturaPeriodo[] periodos = CestaAssinaturaPeriodo.values();
		Endereco endereco = Endereco.find("cliente.id = ?", cestaAssinatura.getCliente().id).first();
		FormaPagamento pagamento = FormaPagamento.DINHEIRO;
		Pagamento pagamentoCesta = new Pagamento();
		pagamentoCesta.setFormaPagamento(FormaPagamento.DINHEIRO);
		PeriodoEntrega[] periodosEntrega = PeriodoEntrega.values();
		cestaAssinatura.setPagamento(pagamentoCesta);
	
		Cache.set("cesta."+clienteId, cestaAssinatura, "40mn");
		
		render(cestaAssinatura, endereco, pagamento, periodos, clienteId, periodosEntrega);
	}
	
	@Transactional(readOnly=false)
	public static void fecharCesta(String clienteId, CestaAssinatura cestaAssinatura, FormaPagamento formaPagamento) {
		Logger.info("### Início - Salvar cesta para o cliente: %s ###", clienteId);
		CestaAssinatura _cestaAssinatura = Cache.get("cesta."+clienteId, CestaAssinatura.class);
		
		if(_cestaAssinatura==null)
			cestaAtiva(Messages.get("application.message.basket.empty", ""));
		
		else if(formaPagamento==null) {
			validation.addError("Pagamento", "form.payment.title", "");
			
			params.flash();
			validation.keep();
			
			finalizar(clienteId);
			
		}else {
			Pagamento pagamento = new Pagamento();
			pagamento.setFormaPagamento(formaPagamento);
			pagamento.setInformacoes("O pagamento será realizado no fim do mês");
			pagamento.setAssinatura(_cestaAssinatura);
			
			_cestaAssinatura.setPagamento(pagamento);
			_cestaAssinatura.setPeriodo(cestaAssinatura.getPeriodo());
			
			_cestaAssinatura.save();
			
			try {
				Collections.sort(_cestaAssinatura.getListCestaAssinaturaProduto());
				StringBuffer email = new StringBuffer();
				email.append(Mail.EMAIL_CONTACT);
				
				Mail.sendCestaAssinatura(_cestaAssinatura, email.toString(), "Sucesso", "Assinatura de Cesta Personalizada | Vida Saudável Orgânicos",
						_cestaAssinatura.getCliente().getUsuario().getEmail());
				
				Mail.sendCestaAssinatura(_cestaAssinatura, email.toString(), "Sucesso", "Assinatura de Cesta Personalizada | Vida Saudável Orgânicos",
						Mail.EMAIL_CONTACT);
				
			}catch(Exception e) {
				Logger.error(e, "Erro ao enviar o e-mail com a assinatura da cesta para o cliente %s.", _cestaAssinatura.getCliente().getNome());
			}
			
			Cache.safeDelete("cesta."+clienteId);
			Logger.info("### Fim - Salvar cesta para o cliente: %s ###", clienteId);
			render(_cestaAssinatura);
		}
	}
	
	
	public static void visualizarProdutos(Long idCestaAssinatura) {
		List<CestaAssinaturaProduto> listAssinaturaProduto = CestaAssinaturaProduto.find("cestaAssinatura.id = ?", idCestaAssinatura).fetch();
		
		Collections.sort(listAssinaturaProduto, new CestaAssinaturaProdutoComparator(Boolean.FALSE));
		
		render(listAssinaturaProduto);
	}
	
	public static void gerarPedidosCestas() {
		new CestaAssinaturaJob().gerarPedidos();

		Home.index(Messages.get("form.cesta.create.order.message", ""));
	}
	
	public static void assinaturas() {
		List<CestaAssinatura> assinaturaCestas = CestaAssinatura.find("order by cliente.nome ASC, ativo ASC", null).fetch();
		ValuePaginator<CestaAssinatura> cestas = new ValuePaginator<CestaAssinatura>(assinaturaCestas);
		
		render(cestas);
	}
	
}
