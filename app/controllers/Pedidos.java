/**
 * 
 */
package controllers;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.apache.commons.lang.StringUtils;

import business.estoque.EstoqueControl;
import business.pagamento.service.PayPalService;

import models.CarrinhoProduto;
import models.CarrinhoItem;
import models.Cliente;
import models.FormaPagamento;
import models.Pagamento;
import models.Pedido;
import models.PedidoItem;
import models.Produto;
import models.Usuario;
import models.Pedido.PedidoEstado;
import play.Logger;
import play.cache.Cache;
import play.data.validation.Required;
import play.data.validation.Valid;
import play.db.jpa.JPA;
import play.db.jpa.Transactional;
import play.i18n.Messages;
import play.modules.paginate.ValuePaginator;
import play.mvc.Before;
import ebay.api.paypalapi.DoExpressCheckoutPaymentResponseType;
import ebay.api.paypalapi.GetExpressCheckoutDetailsResponseType;
import ebay.apis.eblbasecomponents.AckCodeType;
import form.ProdutoQuantidadeForm;

/**
 * @author guerrafe
 *
 */
public class Pedidos extends BaseController {
	
	@Before(unless={"view", "pedidoProdutos", "finalizarPedidoPayPal", "novoPedido"})
	static void estaAutorizado() {
		Logger.debug("####### Verificar se o usuário autenticado é admin... ########");
		
		if(!session.contains("isAdmin") || Boolean.valueOf(session.get("isAdmin"))==Boolean.FALSE) {
			Logger.debug("####### Usuário não autorizado a acessar essa funcionalidade...%s ########", session.get("usuarioAutenticado"));
			
			Home.index("Usuário não autorizado a acessar essa funcionalidade.");
		}
	}
		
	public static void view(Long id) {
		Logger.debug("########## Vai consultar os pedidos para o cliente: %s ##########", id);
		Long idCliente = Long.parseLong(session.get("clienteId"));
		
		if(id.equals(idCliente)) {
			List<Pedido> pedidos = Pedido.find("cliente.id = ? order by id desc", id).fetch(30);
			
			render(pedidos);
			
		}else {
			Home.index(null);
		}
	}

	public static void showAll() {
		Logger.debug("########## Vai consultar todos os pedidos... ###########", "");
		
		PedidoEstado[] status = PedidoEstado.values();
		
		List<Pedido> _pedidos = Pedido.find("order by dataPedido desc, codigoEstadoPedido desc").fetch();
		ValuePaginator<Pedido> vPedidos = new ValuePaginator<Pedido>(_pedidos);
		vPedidos.setPageSize(50);
		
		render(vPedidos, status);
	}
	
	public static void edit(Long id, String message) {
		Logger.debug("########## Vai consultar o pedido de id: %s ###########", id);
		Pedido pedido = Pedido.findById(id);
		
		PedidoEstado[] status = PedidoEstado.values();
		
		render(pedido, status, message);
	}
	
	@Transactional(readOnly=false)
	public static void atualizar(@Valid Pedido pedido) {
		Logger.debug("######### Vai atualizar o pedido id: %s #########", pedido.id);
		
		if(pedido.getDesconto().getValorDesconto().doubleValue()>pedido.getValorPedido().doubleValue())
			validation.addError("pedido.desconto.valorDesconto", "message.error.value.desconto", "");
		
		if(pedido.getDataEntrega()!=null && pedido.getDataPedido().after(pedido.getDataEntrega()))
			validation.addError("dataEntrega", "form.order.dateDelivery.wrong", "");
		
		if(validation.hasErrors()) {
			Logger.debug("######## Não foi possível salvar o pedido: %s  #########", validation.errors());
			params.flash();
			validation.keep();
			
			edit(pedido.id, null);
			
		}else {
			if(pedido.getCodigoEstadoPedido().equals(PedidoEstado.CANCELADO) 
					&& !PedidoEstado.CANCELADO.equals(pedido.getUltimoStatusEstadoPedido())) {
				Logger.info("#### Início - Atualizar o estoque com os produtos do pedido: %s ####", pedido.id);
				CarrinhoProduto carrinho = CarrinhoProduto.findById(Long.parseLong(pedido.getCodigoPedido()));
				
				if(carrinho!=null) {
					EstoqueControl.reporEstoque(pedido.getItens(), session.get("usuarioAutenticado"));
				
					pedido.setUltimoStatusEstadoPedido(PedidoEstado.CANCELADO);
				}
				Logger.info("#### Fim - Atualizar o estoque com os produtos do pedido: %s ####", pedido.id);
			}
			
			Usuario usuario = Usuario.findById(Long.parseLong(session.get("clienteId")));
			
			pedido.setDataAlteracao(new Date());
			pedido.getDesconto().setPedido(pedido);
			pedido.getDesconto().setUsuario(usuario);
			pedido.getDesconto().setDataDesconto(new Date());

			pedido.calcularDesconto();
			
			pedido.save();
		}
		Logger.debug("######### Pedido salvo com sucesso: %s #########", pedido.id);

		infoPedidoCliente(pedido.id);
	}
	
	private static List<PedidoItem> getProdutosByPedido(Long id, Boolean somenteAtivos) {
		Logger.debug("###### Vai consultar os produtos para o pedido id: %s ######", id);
		
		List<Object> parametros = new ArrayList<Object>();
		
		StringBuffer query = new StringBuffer();
		query.append("pedido.id = ? ");
		
		parametros.add(id);
		
		if(somenteAtivos!=null) {
			query.append("AND excluido = ? ");
			parametros.add(!somenteAtivos);
		}

		List<PedidoItem> itens = PedidoItem.find(query.toString(), parametros.toArray()).fetch();
		
		return itens;
	}
	
	public static void pedidoProdutos(Long id, Boolean somenteAtivos) {
		Logger.debug("#### Carregar os produtos para o pedido: %s ####", id);
		Pedido pedido = Pedido.findById(id);
		List<PedidoItem> itens = getProdutosByPedido(id, somenteAtivos); 
		
		render(itens, pedido);
	}
	
	public static void viewProducts(Long id, Boolean somenteAtivos) {
		Pedido pedido = Pedido.findById(id);
		List<PedidoItem> _itens = getProdutosByPedido(id, somenteAtivos); 
		ValuePaginator<PedidoItem> itens = new ValuePaginator<PedidoItem>(_itens);
		itens.setPageSize(7);
		
		render("Pedidos/visualizarProdutos.html", itens, pedido);
	}
	
	public static void showProdutos(Long id) {
		Pedido pedido = Pedido.findById(id);
		List<PedidoItem> itens = getProdutosByPedido(id, Boolean.TRUE);
		
		render(itens, pedido);
	}
	
	@Transactional(readOnly=false)
	public static void atualizarProdutos(Long id, List<ProdutoQuantidadeForm> produtoQuantidade, Boolean excluir) {
		Logger.debug("###### Vai alterar os produtos para o pedido id: %s ######", "");
		BigDecimal valorPedidoAlterado = new BigDecimal(0);
		BigDecimal vlrProdQtde = null;
		Pedido pedido = Pedido.findById(id);
		
		Logger.info("###### Pedido: %s: - Valor: %s ######", pedido.id, pedido.getValorPedido());
		BigDecimal vlrFrete = Carrinho.calcularFrete(pedido.getValorPedido());
		
		for(ProdutoQuantidadeForm itemProdutoQuantidade : produtoQuantidade) {
			laco_raiz:	
			for(PedidoItem item : getProdutosByPedido(id, null)) {
				for(Produto prod : item.getProdutos()) {
					if(prod.id.equals(itemProdutoQuantidade.getId())) {
						vlrProdQtde = new BigDecimal(itemProdutoQuantidade.getQuantidade() * prod.getValorVenda()).setScale(2, BigDecimal.ROUND_HALF_UP);
						
						if(itemProdutoQuantidade.getExcluir()) {
							if(excluir) {
								item.delete();
								
							}else {
								item.setExcluido(Boolean.TRUE);
								
								item.save();
							}
							Logger.info("###### Excluiu o ítem do pedido: %s: - produto: %s / valor: %s ######", id, prod.getDescricao(), prod.getValorVenda());
							
						}else {
							item.setQuantidade(itemProdutoQuantidade.getQuantidade());
							
							valorPedidoAlterado = valorPedidoAlterado.add(vlrProdQtde);
							
							Logger.info("###### Salvou o ítem do pedido: %s: - produto: %s - quantidade: %s ######", id, prod.getDescricao(), itemProdutoQuantidade.getQuantidade());
							
							item.save();
						}
						break laco_raiz;
					}
				}
			}	
		}
		pedido.getDesconto().setValorDesconto(new BigDecimal(0));
		
		valorPedidoAlterado = valorPedidoAlterado.add(vlrFrete);
		pedido.setValorPedido(valorPedidoAlterado);
		Logger.info("###### Novo valor do frete %s para o pedido: %s. Total: %s  ######", vlrFrete, id, valorPedidoAlterado);
		pedido.setDataAlteracao(new Date());
		pedido.setObservacao("Alterado por " + session.get("usuarioAutenticado"));
		pedido.save();
		
		infoPedidoCliente(pedido.id);
	}
	
	/**
	 * Adiciona ao carrinho os produtos do pedido informado e envia a tela de confirmação.
	 * @param sessionId
	 * @param idPedido
	 */
	public static void novoPedido(String sessionId, Long idPedido) {
		Logger.debug("###### Início - Gerar um novo pedido com os produtos do pedido %s ######", idPedido);
		BigDecimal valorTotalCompra = new BigDecimal(0);
		Boolean temEstoque = Boolean.TRUE;
		
		Cache.safeDelete("valorTotal."+sessionId);
		Cache.safeDelete(sessionId);
		
		List<PedidoItem> itens = PedidoItem.find("pedido.id = ?", idPedido).fetch();
		
		if(!itens.isEmpty()) {
			CarrinhoProduto carrinho = new CarrinhoProduto();
			
			for(PedidoItem pedidoItem : itens) {
				Produto produto = pedidoItem.getProdutos().get(0);
				
				//Analisar o estoque também
				if(produto.getProdutoEstoque()!=null) {
					temEstoque = EstoqueControl.loadEstoque(null, produto.id).getQuantidade() >= pedidoItem.getQuantidade();
				}
				
				if(produto.getAtivo() && temEstoque) {
					CarrinhoItem item = new CarrinhoItem();
					
					item.getProdutos().addAll(pedidoItem.getProdutos());
					item.setQuantidade(pedidoItem.getQuantidade());
					
					valorTotalCompra = valorTotalCompra.add(new BigDecimal( pedidoItem.getQuantidade() * pedidoItem.getProdutos().get(0).getValorVenda()) ).setScale(2, BigDecimal.ROUND_HALF_UP);
					
					carrinho.getItens().add(item);
				}
			}
			carrinho.setValorTotalCompra(valorTotalCompra);
			
			Cache.set("valorTotal."+sessionId, valorTotalCompra, "40mn");
			Cache.add(sessionId, carrinho, "40mn");
		}
		
		Logger.debug("###### Fim - Gerar um novo pedido com os produtos do pedido %s ######", idPedido);
		Carrinho.pagamento(sessionId, null);
	}

	/**
	 * Pesquisar os produtos
	 */
	public static void findByParams(@Required(message="Digite ao menos um valor") String paramPedido, 
									@Required(message="Selecione um parâmetro") String param) {
		Logger.debug("######## Início - Pesquisar pedidos pelo parâmetro: %s########", param); 
		Object parametro = null;
		StringBuffer query = new StringBuffer();
		
		parametro = buildQuery(query, param, paramPedido);
		
		List<Pedido> pedidos = Pedido.find(query.toString(), parametro).fetch();
		
		ValuePaginator<Pedido> vPedidos = new ValuePaginator<Pedido>(pedidos);
		vPedidos.setPageSize(50);
		
		Logger.debug("######## Fim - Pesquisar pedidos pelo parâmetro: %s########", param);
		
		flash.put("paramPedido", paramPedido);
		flash.put("param", param);
		
		render("Pedidos/showAll.html", vPedidos);
	}

	public static void order(String campo, Boolean asc) {
		Logger.debug("###### Início - Ordernar por %s ######", campo);
		StringBuffer query = new StringBuffer();
		
		Object[] parametro = new Object[]{};
		
		String criterioPedido = params.get("criterioPedido", String.class);
		String valorCriterioPedido = params.get("valorCriterioPedido", String.class);
		
		if(!StringUtils.isEmpty(criterioPedido) && !StringUtils.isEmpty(valorCriterioPedido))
			parametro[0] = buildQuery(query, criterioPedido, valorCriterioPedido);
		
		query.append("order by ").append(campo).append(" ").append(asc ? "ASC" :"DESC");
		
		List<Pedido> pedidos = Pedido.find(query.toString(), parametro).fetch();
		
		ValuePaginator<Pedido> vPedidos = new ValuePaginator<Pedido>(pedidos);
		vPedidos.setPageSize(50);
		
		Logger.debug("###### Fim - Ordernar por %s ######", campo);
		render("Pedidos/showAll.html", vPedidos);
	}
	
	@Transactional(readOnly=false)
	public static void finalizarPedidoPayPal(String token, String PayerID) {
		Logger.info("#### Início - Gateway de Pagamento PayPal confirmando transação [token: %s - payerId: %s] ####", token, PayerID);
		Pagamento pagamentoFeito = null;
		Cliente cliente = null;
		Pedido pedido = null;
		Boolean responderQuestionario = Boolean.FALSE;
		
		try {
			pagamentoFeito = Pagamento.find("informacoes = ? AND formaPagamento = ?", token, FormaPagamento.PAYPAL).first();
			
			if(pagamentoFeito!=null) {
				Logger.info("#### PayPal confirmando transação [token: %s - payerId: %s] - pagamento encontrado? ####", token, PayerID, pagamentoFeito.id!=null);
				cliente = pagamentoFeito.getPedido().getCliente();
				pedido = pagamentoFeito.getPedido();
				
				PayPalService payPalService = PayPalService.newInstance(Messages.get("application.service.url.paypal", ""), 
																					Messages.get("application.username.url.paypal", ""), 
																					Messages.get("application.password.url.paypal", ""), 
																					Messages.get("application.signature.url.paypal", ""));
				
				GetExpressCheckoutDetailsResponseType confirmacaoTransacao = payPalService.confirmarPagamento(token);
				
				if(payPalService.foiExecutadoComSucesso(confirmacaoTransacao.getAck(), confirmacaoTransacao.getErrors())) {
					DoExpressCheckoutPaymentResponseType efetivacaoTransacao = payPalService.efetivarPagamento(token, 
																												confirmacaoTransacao.getGetExpressCheckoutDetailsResponseDetails().getPayerInfo().getPayerID(),
																												pedido.getValorPedido().doubleValue());
					
					if(payPalService.foiExecutadoComSucesso(efetivacaoTransacao.getAck(), efetivacaoTransacao.getErrors())) {
						pedido.setUltimoStatusEstadoPedido(pedido.getCodigoEstadoPedido());
						pedido.setCodigoEstadoPedido(PedidoEstado.AGUARDANDO_ENTREGA);
						pedido.setDataAlteracao(new Date());
						pedido.setObservacao("Pagamento confirmado!");
						
					}else {
						pedido.getPagamento().setErrors(PayPalService.getInformacoesErro(efetivacaoTransacao.getErrors()));
						
						Logger.info("#### PayPal transação não efetivada [token: %s - payerId: %s - status: %s] ####", token, 
								confirmacaoTransacao.getGetExpressCheckoutDetailsResponseDetails().getPayerInfo().getPayerID(), efetivacaoTransacao.getAck());
					}
					
				}else {
					Logger.info("#### PayPal transação não confirmada [token: %s - payerId: %s - status: %s]  ####", token, 
										confirmacaoTransacao.getGetExpressCheckoutDetailsResponseDetails().getPayerInfo().getPayerID(), confirmacaoTransacao.getAck());
					
					pedido.getPagamento().setErrors(PayPalService.getInformacoesErro(confirmacaoTransacao.getErrors()));
				}
				pedido.save();
				
				Logger.info("####Fim - PayPal confirmando transação [token: %s - payerId: %s] - Pedido: %s. ####", token, PayerID, pedido.id);
				
				responderQuestionario = Questionarios.haQuestionarioPendente(cliente.getUsuario().getId());
				
				render("Carrinho/finalizar.html", pedido.id, cliente, responderQuestionario);
				
			}else {
				Home.index("Dados não encontrados para o token: " + token);
			}
			
		}catch(Exception e) {
			Logger.error(e, "Erro - PayPal confirmando transação [token: %s - payerId: %s]", token, PayerID);
			throw new RuntimeException(e);
			
		}finally {
			Logger.info("#### Fim - Gateway de Pagamento PayPal confirmando transação [token: %s - payerId: %s] ####", token, PayerID);
		}
		
	}
	
	@Transactional(readOnly=false)
	public static void cancelarPedidoPayPal(String token, String PayerID) {
		Logger.info("#### Início - Gateway de Pagamento PayPal cancelando transação [token: %s - payerId: %s] ####", token, PayerID);
		Pagamento pagamentoFeito = null;
		Cliente cliente = null;
		Pedido pedido = null;
		String message = null;
		
		try {
			pagamentoFeito = Pagamento.find("informacoes = ? AND formaPagamento = ?", token, FormaPagamento.PAYPAL).first();
			
			if(pagamentoFeito!=null) {
				Logger.info("#### PayPal cancelando transação [token: %s - payerId: %s] - pagamento encontrado? ####", token, PayerID, pagamentoFeito.id!=null);
				cliente = pagamentoFeito.getPedido().getCliente();
				pedido = pagamentoFeito.getPedido();
				
				pedido.setUltimoStatusEstadoPedido(pedido.getCodigoEstadoPedido());
				//pedido.setCodigoEstadoPedido(PedidoEstado.CANCELADO);
				pedido.setDataAlteracao(new Date());
				pedido.setObservacao("Pagamento cancelado[token: "+token+"]");
				
				pedido.merge();
				
				message = Messages.get("form.paypal.cancel.order", "");
				
				Logger.info("#### PayPal cancelando transação [token: %s - payerId: %s] - Pedido: %s atualizado. ####", token, PayerID, pedido.id);
				
			}else {
				 message =  Messages.get("form.paypal.error.token.notfound", token);
			}
			
			Logger.info("#### Fim - Gateway de Pagamento PayPal cancelando transação [token: %s - payerId: %s] ####", token, PayerID);
			Home.index(message);
			
		}catch(Exception e) {
			Logger.error(e, "Erro - PayPal cancelando transação [token: %s - payerId: %s]", token, PayerID);
			throw new RuntimeException(e);
		}
	}
	
	public static void payPalTransaction(Long id) {
		Pedido pedido = Pedido.findById(id);
		Pagamento pagamento = pedido.getPagamento(); 
		
		render(pagamento);
	}
	
	public static void alterarMetodoPagamento(Long id) {
		FormaPagamento[] metodosPagamento = FormaPagamento.values();
		Pedido pedido = Pedido.findById(id);
		FormaPagamento formaPagamentoAtual = pedido.getPagamento().getFormaPagamento();
		
		render(pedido, formaPagamentoAtual, metodosPagamento);
	}
	
	@Transactional(readOnly=false)
	public static void atualizarMetodoPagamento(Pedido pedido) {
		Logger.info("#### Início - Alterar Método Pagamento [Pedido: %s; User: %s] ####", pedido.id, session.get("usuarioAutenticado"));
		
		pedido.save();
		
		Logger.info("#### Fim - Alterar Método Pagamento [Pedido: %s; User: %s] ####", pedido.id, session.get("usuarioAutenticado"));
		
		infoPedidoCliente(pedido.id);
	}
	
	public static void infoPedidoCliente(Long id) {
		Pedido pedido = Pedido.findById(id);
		PedidoEstado[] status = PedidoEstado.values();
		
		render(pedido, status);
	}
	
	public static void findUltimosPedidos(Long id, Long idCliente, Integer rows) {
		Logger.debug("#### Carregar os últimos %s pedidos, para o cliente id %  #####", rows, idCliente);
		List<Pedido> pedidos = Pedido.find("cliente.id = ? order by id desc", idCliente).fetch(rows);
		BigDecimal debito = new BigDecimal(0);
		Pedido pedido = Pedido.findById(id);

		//Pedidos com débitos
		List<Pedido> pedidosAbertos = Pedido.find("cliente.id = ? AND codigoEstadoPedido = ? order by id desc", idCliente, PedidoEstado.AGUARDANDO_PAGAMENTO).fetch();
		
		for(Pedido _ped : pedidosAbertos) {
			Logger.info("##### Encontrado pedido %s aberto para o cliente % - valor: %s #####", _ped.id, _ped.getCliente().getNome(), _ped.getValorComDesconto());
			
			debito = debito.add(_ped.getValorComDesconto()).setScale(BigDecimal.ROUND_HALF_UP);
		}
		render(pedidos, debito, pedido);
	}
	
	private static Object buildQuery(StringBuffer queryAppend, String param, String value) {
		Object parametro = null;
		
		if("id".equalsIgnoreCase(param)) {
			parametro = Long.parseLong(value.trim());
			queryAppend.append("id = ? ");
			
		}else if("dataPedido".equalsIgnoreCase(param)) {
			SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
			try {
				parametro = dateFormat.parse(value);
				
			}catch(Exception e) {
				validation.addError("dataPedido", "Digite uma data válida.", "");
				Logger.error(e, "Não foi possível converter a data: %s", value);
			}
			
			queryAppend.append("dataPedido >= ? order by codigoEstadoPedido DESC");
			
		}else if("cliente".equalsIgnoreCase(param)) {
			parametro = "%"+value.toUpperCase().trim()+"%";
			queryAppend.append("UPPER(cliente.nome) LIKE ? ");			
		}
		
		return parametro;
	}
	
}