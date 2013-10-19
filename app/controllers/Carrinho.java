/**
 * 
 */
package controllers;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import models.CarrinhoItem;
import models.CarrinhoProduto;
import models.CestaPronta;
import models.Cliente;
import models.Desconto;
import models.Endereco;
import models.FormaPagamento;
import models.Frete;
import models.Pagamento;
import models.Pedido;
import models.Pedido.PedidoEstado;
import models.Produto;
import play.Logger;
import play.cache.Cache;
import play.db.jpa.Transactional;
import play.i18n.Messages;
import play.mvc.Before;
import play.mvc.Catch;
import play.mvc.Controller;
import business.estoque.EstoqueControl;
import business.pagamento.service.PayPalService;
import ebay.api.paypalapi.SetExpressCheckoutResponseType;
import exception.PayPalServiceException;
import exception.ProdutoEstoqueException;
import exception.SystemException;
import form.ProdutoQuantidadeForm;

/**
 * @author guerrafe
 *
 */
public class Carrinho extends Controller {
	
	@Before(only={"pagamento", "finalizar", "loading"}) 
	static void isAuthenticated(){
		if(session.get("usuarioAutenticado")==null) {
			session.put("carrinho", request.path);
			
			Login.index(null, null, Messages.get("application.message.notAuthenticated", ""));
		}
	}
	
	@Catch(value=ProdutoEstoqueException.class, priority=0)
	public static void erroCarrinho(Exception e) {
		Logger.error(e, "Houve um erro na posição do estoque.");

		Mail.sendError("Insuficiência no Estoque", Mail.EMAIL_ADMIN, Mail.EMAIL_ADMIN, e);
		
		Home.index(Messages.get("application.error.estoque", e.getMessage()));
	}
	
	private static void addProduto(String sessionId, Long idProduto, Integer quantidade, Double valorProduto) {
		Logger.debug("######## Vai adicionar produtos ao Cache... #########");
		CarrinhoProduto carrinho = Cache.get(sessionId, CarrinhoProduto.class);
		Produto produto = null;

		List<Produto> produtos = new ArrayList<Produto>();
		List<Produto> tempProdutos = new ArrayList<Produto>();
		int i = 0;
		
		if(carrinho==null) {
			carrinho = new CarrinhoProduto();
		}
		else
			for(CarrinhoItem item : carrinho.getItens())
				produtos.addAll(item.getProdutos());
		
		while(i<quantidade) {
			produto = Produto.findById(idProduto);
			
			tempProdutos.add(produto);
			
			i++;
		}
		produtos.addAll(tempProdutos);
		carrinho.getItens().clear();
		carrinho.createItens(produtos);
		
		Cache.add(sessionId, carrinho, "40mn");
		Logger.debug("######## Produtos adicionados ao Cache... #########");
	}
	
	private static BigDecimal adicionarProdutoCarrinho(String sessionId, Long idProduto, Integer quantidade, Double valorProduto) {
		Logger.debug("########## Adicionar sessionId: %s, idProduto: %s, quantidade: %s, valorProduto: %s ##########", sessionId, idProduto, quantidade, valorProduto);
		BigDecimal valorTotalCache = null;
		
		if(quantidade>0) {
			BigDecimal valorTotal = new BigDecimal( quantidade*valorProduto ).setScale(2, BigDecimal.ROUND_HALF_UP);
			
			/*
			 * Adicionar os dados no Cache do Play! para posterior recuperação...
			 */
			addProduto(sessionId, idProduto, quantidade, valorProduto);
			
			valorTotalCache = (Cache.get("valorTotal."+sessionId, BigDecimal.class)==null) ? new BigDecimal(0): Cache.get("valorTotal."+sessionId, BigDecimal.class);
			
			valorTotalCache = valorTotalCache.add( valorTotal );
			
			Cache.set("valorTotal."+sessionId, valorTotalCache, "40mn");
		}
		
		Logger.debug("########## Produtos adicionados para a sessão: valor: %s ##########", valorTotalCache);
		
		return valorTotalCache;
	}
	
	/**
	 * @param sessionId
	 * @param idProduto
	 * @param quantidade
	 * @param valorProduto
	 * @return valor total carrinho
	 */
	private static BigDecimal atualizarProdutoCarrinho(String sessionId, Long idProduto, Integer quantidade, Double valorProduto) {
		Logger.debug("########## Adicionar sessionId: %s, idProduto: %s, quantidade: %s, valorProduto: %s ##########", sessionId, idProduto, quantidade, valorProduto);
		BigDecimal valorTotal = null;
		
		if(quantidade>0) {
			valorTotal = new BigDecimal( quantidade*valorProduto ).setScale(2, BigDecimal.ROUND_HALF_UP);
			
			atualizarProduto(sessionId, idProduto, quantidade, valorProduto);
		}
		
		Logger.debug("########## Produtos adicionados para a sessão: valor: %s ##########", valorTotal);
		
		return valorTotal;
	}
	
	private static void atualizarProduto(String sessionId, Long idProduto, Integer quantidade, Double valorProduto) {
		Logger.debug("######## Vai adicionar produtos ao Cache... #########");
		CarrinhoProduto carrinho = Cache.get(sessionId, CarrinhoProduto.class);
		Produto produto = null;
		int i = 0;
		
		CarrinhoItem item = carrinho.getItemListProduto(idProduto);
		item.getProdutos().clear();
		item.setQuantidade(quantidade);
		
		while(i<quantidade) {
			produto = Produto.findById(idProduto);
			
			item.getProdutos().add(produto);
			
			i++;
		}
		Cache.add(sessionId, carrinho, "40mn");
		Logger.debug("######## Produtos adicionados ao Cache... #########");
	}
	
	/**
	 * Trabalhando com o Cache do Play! 
	 * @param idProduto
	 * @param quantidade
	 * @param valorProduto
	 */
	public static void adicionarProduto(String sessionId, Long idProduto, Integer quantidade, Double valorProduto) {
		renderJSON(adicionarProdutoCarrinho(sessionId, idProduto, quantidade, valorProduto));
	}
	
	/**
	 * <p>Metodo para inserir a cesta no carrinho.</p>
	 * @param sessionId
	 * @param idProduto
	 * @param quantidade
	 * @param valorProduto
	 */
	public static void adicionarCesta(String sessionId, Long idProduto, Integer quantidade, Double valorProduto) {
		CarrinhoProduto carrinho = Cache.get(sessionId, CarrinhoProduto.class);
		CestaPronta cesta = CestaPronta.findById(idProduto);
		BigDecimal valorTotalCache = BigDecimal.ZERO;
		
		if(carrinho==null)
			carrinho = new CarrinhoProduto();
		
		if(!carrinho.contains(cesta)) {
			if(Cache.get("valorTotal."+sessionId, BigDecimal.class)!=null)
				valorTotalCache = Cache.get("valorTotal."+sessionId, BigDecimal.class);
			
			valorTotalCache = valorTotalCache.add( BigDecimal.valueOf(valorProduto*quantidade).setScale(2, BigDecimal.ROUND_HALF_UP) );
			
			Cache.set("valorTotal."+sessionId, valorTotalCache, "40mn");
			
			carrinho.addCestaPronta(cesta);
			
			Cache.set(sessionId, carrinho, "40mn");
		}
		renderJSON(valorTotalCache);
	}
	
	public static void valorTotalCompra(String sessionId) {
		BigDecimal result = null;
		
		result = Cache.get("valorTotal."+sessionId, BigDecimal.class);
		
		renderJSON(result);
	}
	
	public static void view(String sessionId, String message) {
		Logger.debug("######## Início - Ver Produtos do Carrinho... ########");
		CarrinhoProduto carrinho = Cache.get(sessionId, CarrinhoProduto.class);
		
		if(carrinho!=null && (!carrinho.getItens().isEmpty() || !carrinho.getCestas().isEmpty())) {
			BigDecimal valorTotalCompra = Cache.get("valorTotal."+sessionId, BigDecimal.class);
			
			carrinho.setDataCompra(new Date());
			
			carrinho.setValorTotalCompra(valorTotalCompra);
			
			render(carrinho, sessionId, message);
			
		}else {
			Home.index(Messages.get("application.message.basket.empty", ""));
			Cache.clear();
		}
		Logger.debug("######## Fim - Ver Produtos do Carrinho... ########");
	}
	
	public static void limparCarrinho(String sessionId) {
		Logger.debug("####### Vai limpar o carrinho para a sessão %s ########", sessionId);
		
		Cache.delete("valorTotal."+sessionId);
		Cache.delete(sessionId);
		
		request.cookies.clear();
		session.remove("sessionId");
		
		redirect("/index");
	}
	
	@Transactional(readOnly=false)
	public static void finalizar(String sessionId) throws ProdutoEstoqueException, PayPalServiceException {
		CarrinhoProduto carrinho = Cache.get(sessionId, CarrinhoProduto.class);
		Boolean isAssessor = null;
		Boolean responderQuestionario = Boolean.FALSE;
		String formaPagamento = null;
		SetExpressCheckoutResponseType resultPayPalService = null;
		String urlCheckoutPayPal = null;
		PedidoEstado statusPedido = null;
		
		if(carrinho!=null && session.get("clienteId")!=null) {
			Frete frete = new Frete(Pedido.calcularFrete(carrinho.getValorTotalCompra()));
			
			validarValorCompra(carrinho.getValorTotalCompra());
			
			if(Boolean.parseBoolean(session.get("isAdmin")) && "-1".equalsIgnoreCase(params.get("cliente"))) {
				validation.addError("cliente", "message.basket.order.assessor.required", "");
				isAssessor = Boolean.TRUE;
			}
			
			formaPagamento = params.get("formaPagamento");
			
			if(validation.hasErrors()) {
				params.flash();
				validation.keep();
				
				Logger.debug("###### Não foi possível finalizar o pedido: %s ######", validation.errors());
				
				pagamento(sessionId, isAssessor);
				
			}else {
				//Validar o Estoque	
				EstoqueControl.atualizarEstoque(carrinho.getItens());
				
				Pagamento pagamento = new Pagamento();
				
				Long idCliente = params.get("cliente")==null ? Long.parseLong(session.get("clienteId")) : Long.parseLong(params.get("cliente"));
				
				Cliente cliente = Cliente.findById(idCliente);
				
				carrinho.setCliente(cliente);
				
				if(carrinho.id==null)
					carrinho.save();
				else
					carrinho.merge();
				
				Pedido pedido = new Pedido();
				
				if(FormaPagamento.PAYPAL.equals(FormaPagamento.getFormaPagamento(formaPagamento))) {
					StringBuffer infosPedido = new StringBuffer();
					pagamento.setFormaPagamento(FormaPagamento.PAYPAL);
					
					PayPalService payPalService = PayPalService.newInstance(Messages.get("application.service.url.paypal", ""), 
																			Messages.get("application.username.url.paypal", ""), 
																			Messages.get("application.password.url.paypal", ""), 
																			Messages.get("application.signature.url.paypal", ""));
					infosPedido.append("Pedido gerado em: ").append(new Date()).append(".");
					infosPedido.append("Cliente: ").append(cliente.getNome()).append(".");
					infosPedido.append("Código Pedido: ").append(carrinho.id);
					infosPedido.append("Total Pedido: ").append(carrinho.getValorTotalCompra());
					
					resultPayPalService = payPalService.solicitarPagamento(cliente.getNome(), carrinho.getValorTotalCompra().doubleValue(), carrinho.id, infosPedido.toString());
					
					if(payPalService.foiExecutadoComSucesso(resultPayPalService.getAck(), resultPayPalService.getErrors())) {
						pagamento.setInformacoes(resultPayPalService.getToken());
						
						urlCheckoutPayPal = Messages.get("application.checkout.url.paypal", "");
						urlCheckoutPayPal += resultPayPalService.getToken();
						
						statusPedido = PedidoEstado.AGUARDANDO_PAGAMENTO;
						
					}else {
						Logger.info("#### O pedido de pagamento no Paypal não foi aprovado: %s ####", PayPalService.getInformacoesErro(resultPayPalService.getErrors()));
					}
					
				}else if(FormaPagamento.DINHEIRO.equals(FormaPagamento.getFormaPagamento(formaPagamento))){
					BigDecimal valorPedidoComDesconto = new BigDecimal(Messages.get("application.minValue.paypal", ""));
					
					pagamento.setFormaPagamento(FormaPagamento.DINHEIRO);
					
					if(carrinho.getValorTotalCompra().doubleValue()>valorPedidoComDesconto.doubleValue()) {
						Desconto desconto = new Desconto(new BigDecimal(Messages.get("application.pedido.paypal.desconto", "")));
						desconto.setDataDesconto(new Date());
						desconto.setPedido(pedido);
						
						pedido.setDesconto(desconto);
					}
					
					statusPedido = PedidoEstado.AGUARDANDO_ENTREGA;
				}
				// Gerar Pedido
				pedido.addCesta(carrinho.getCestas());
				
				if(!carrinho.getCestas().isEmpty())
					pedido.setObservacao(Messages.get("form.product.isBasket", ""));
				
				pedido.setCodigoPedido(String.valueOf(carrinho.getId()));
				pedido.addPedidoItem(carrinho.getItens());
				pedido.setCliente(cliente);
				pedido.setDataPedido(new Date());
				pedido.setValorPedido(carrinho.getValorTotalCompra());
				pedido.setCodigoEstadoPedido(statusPedido);
				pedido.setArquivado(Boolean.FALSE);
				pagamento.setPedido(pedido);
				pagamento.setValorPagamento(pedido.getValorPedido());
				pedido.setPagamento(pagamento);
				
				frete.addPedido(pedido);
				frete.save();
				pedido.save();
				
				Logger.info("Valor Desconto %s", pedido.getDesconto().getValorDesconto());
				Logger.info("###### E-mail de confirmação do pedido para: %s #######", cliente.getUsuario().getEmail());
				try {
					StringBuffer email = new StringBuffer();
					email.append("Vida Saudável Orgânicos");
					email.append("<").append("contato@vidasaudavelorganicos.com.br").append(">");
					
					Mail.pedidoFinalizado(
							"Pedido Finalizado",
							email.toString(), 
							pedido,
							cliente.getUsuario().getEmail(), Mail.EMAIL_CONTACT);

					Logger.info("###### E-mail de confirmação do pedido para: %s enviado. #######", cliente.getUsuario().getEmail());
										
				} catch (SystemException ex) {
					Logger.error("Erro ao enviar o e-mail de confirmação para %s", cliente.getUsuario().getEmail(), ex);
					
				}
				Cache.safeDelete(sessionId);
				Cache.safeDelete("valorTotal." + sessionId);

				Logger.debug("######## Cache limpo e Pedido gerado: %s ########", pedido.id);

				if(urlCheckoutPayPal==null) {
					String pedidoFinalizado = String.valueOf(pedido.id);

					responderQuestionario = Questionarios.haQuestionarioPendente(cliente.getUsuario().getId());
					
					render(pedidoFinalizado, cliente, responderQuestionario);
				}
				redirect(urlCheckoutPayPal);
			}
			
		}else {
			Home.index(Messages.get("application.message.basket.empty", ""));
		}

	}	
	
	public static void excluirProdutos(String sessionId, List<ProdutoQuantidadeForm> produtoQuantidade, List<CestaPronta> cestas) {
		Logger.debug("##### Início - Excluir produtos do carrinho. #####");
		
		CarrinhoProduto carrinho = Cache.get(sessionId, CarrinhoProduto.class);
		String message = null;
		
		if( (produtoQuantidade==null || produtoQuantidade.isEmpty()) && (cestas==null || cestas.isEmpty()) ) {
			validation.addError("", Messages.get("message.product.required", ""));
			
			validation.keep();
			
		}else {
			if(produtoQuantidade!=null) {
				for(ProdutoQuantidadeForm prod : produtoQuantidade) {
					Produto tempProduto = new Produto(prod.getId());
					
					laco_carrinho:
					for(CarrinhoItem item : carrinho.getItens()) {
						if(item.getProdutos().get(0).id.equals(tempProduto.id)) {
							//Subtrair o valor dos produtos
							BigDecimal result = Cache.get("valorTotal."+sessionId, BigDecimal.class);
							BigDecimal newValue = result.subtract(new BigDecimal( item.getQuantidade() * item.getProdutos().get(0).getValorVenda() ).setScale(2, BigDecimal.ROUND_HALF_UP));
							
							carrinho.setValorTotalCompra(newValue);
							Logger.debug("#### Novo valor do carrinho: %s ####", newValue);
							
							carrinho.getItens().remove(item);
							
							Cache.set("valorTotal."+sessionId, newValue, "40mn");
							break laco_carrinho;
						}
					}
				}
			}
			if(cestas!=null) {
				BigDecimal result = Cache.get("valorTotal."+sessionId, BigDecimal.class);
				
				for(CestaPronta cesta : cestas) {
					BigDecimal valorCesta = CestaPronta.find("select valorVenda from CestaPronta where id = ?", cesta.id).first();
					
					BigDecimal newValue = result.subtract(valorCesta).setScale(2, BigDecimal.ROUND_HALF_UP);
					
					if(carrinho.getCestas().remove(cesta)) {
						carrinho.setValorTotalCompra(newValue);
						
						Cache.set("valorTotal."+sessionId, newValue, "40mn");					
					}
				}
			}
			message = Messages.get("validation.data.success", "");
		
			if(carrinho.getCestas().isEmpty() && carrinho.getItens().isEmpty())
				limparCarrinho(sessionId);
		}
		
		Logger.debug("##### Fim - Excluir produtos do carrinho. #####");
		
		view(sessionId, message);
	}
	
	public static void pagamento(String sessionId, Boolean isAssessor) {
		Logger.debug("###### Início - Selecionar forma de pagamento... ######");
		Endereco endereco = null;
		CarrinhoProduto carrinho = Cache.get(sessionId, CarrinhoProduto.class);
		Frete frete = null;
		List<Cliente> clientes = null;
		Boolean pedidoAssessor = Boolean.TRUE.equals(isAssessor) && Boolean.parseBoolean(session.get("isAdmin"));
		BigDecimal valorMinPagPayPal = null;
		BigDecimal valorComDesconto = null;
		
		if(carrinho!=null && session.get("clienteId")!=null) {
			valorMinPagPayPal = new BigDecimal(Messages.get("application.minValue.paypal", ""));
			frete = new Frete(Pedido.calcularFrete(carrinho.getValorTotalCompra()));
			
			if(pedidoAssessor)
				clientes = Cliente.find("ativo = ?", Boolean.TRUE).fetch();
			
			validarValorCompra(carrinho.getValorTotalCompra());
			
			if(validation.hasErrors()) {
				validation.keep();
				
				Logger.info("###### Não foi possível finalizar o pedido: %s ######", validation.errors());
				
			}else {
				carrinho.setDataCompra(new Date());
				
				if(!pedidoAssessor) {
					Cliente cliente = Cliente.findById( Long.parseLong(session.get("clienteId")) );
					
					endereco = cliente.getEnderecos().get(0);
					
					carrinho.setCliente(cliente);
				}
				
				valorComDesconto = Pedido.calcularDesconto(carrinho.getValorTotalCompra(), new BigDecimal(Messages.get("application.pedido.paypal.desconto", ""))).setScale(2, BigDecimal.ROUND_HALF_DOWN);
			}
			
		}else {
			Home.index(Messages.get("application.message.basket.empty", ""));
		}
		
		Logger.debug("###### Fim - Selecionar forma de pagamento... ######");
		
		FormaPagamento pagamento = FormaPagamento.DINHEIRO; 
		
		render(carrinho, sessionId, frete, endereco, clientes, isAssessor, pagamento, valorMinPagPayPal, valorComDesconto);
	}
	
	public static void atualizar(String sessionId, List<ProdutoQuantidadeForm> produtoQuantidade, List<CestaPronta> cestas) {
		Logger.debug("###### Início - Atualizar Produtos...%s ######", sessionId);
		BigDecimal valorTotalCache = Cache.get("valorTotal."+sessionId, BigDecimal.class);
		String message = null;

		if(produtoQuantidade==null || produtoQuantidade.isEmpty()) {
			validation.addError("", Messages.get("message.product.required", ""));
			
			validation.keep();
			
		}else {
			CarrinhoProduto carrinho = Cache.get(sessionId, CarrinhoProduto.class);
			
			for(ProdutoQuantidadeForm prod : produtoQuantidade) {
				Produto tempProduto = new Produto(prod.getId());
				
				laco_1:
				for(CarrinhoItem item : carrinho.getItens()) {
					if(item.getProdutos().get(0).id.equals(tempProduto.id)) {
						if(ProdutoQuantidadeForm.findQuantidade(produtoQuantidade, tempProduto.id)!=null) {
							//Retirar o valor anterior do Total guardado no Cache
							BigDecimal valorAnterior = new BigDecimal( item.getProdutos().get(0).getValorVenda() * item.getQuantidade() ).setScale(2, BigDecimal.ROUND_HALF_UP); 
							valorTotalCache = valorTotalCache.subtract(valorAnterior); 
							valorTotalCache = valorTotalCache.add(atualizarProdutoCarrinho(sessionId, tempProduto.id, 
												ProdutoQuantidadeForm.findQuantidade(produtoQuantidade, tempProduto.id), item.getProdutos().get(0).getValorVenda())
												);	
						}
						break laco_1;
					}
				}
			}
			Cache.set("valorTotal."+sessionId, valorTotalCache, "40mn");
			
			message = Messages.get("validation.data.success", "");
		}
		Logger.debug("###### Fim - Atualizar Produtos...%s ######", sessionId);
		
		view(sessionId, message);
	}
	
	public static void loading(String sessionId) {
		render(sessionId);
	}
	
	private static void validarValorCompra(BigDecimal valorCompra) {
		if(Double.parseDouble(Messages.get("application.order.minValue", ""))>valorCompra.doubleValue())
			validation.addError("Valor Compra", Messages.get("message.validation.order.minValue", ""), "");
	}
	
	public static Boolean validarValorCompra(Double valorCompra) {
		if(Double.parseDouble(Messages.get("application.order.minValue", ""))>valorCompra.doubleValue())
			return Boolean.FALSE;
		
		return Boolean.TRUE;
	}
}
