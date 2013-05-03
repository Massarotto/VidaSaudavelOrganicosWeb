/**
 * 
 */
package models;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Query;
import javax.persistence.QueryHint;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import play.Logger;
import play.data.validation.MaxSize;
import play.data.validation.Required;
import play.db.jpa.JPA;
import play.db.jpa.Model;
import play.i18n.Messages;
import util.CestaAssinaturaProdutoComparator;

/**
 * @author guerrafe
 *
 */
@Cache(usage=CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Cacheable
@Entity
@Table(name="PEDIDO")
@NamedQuery(name="findAllOrderByDataPedidoAndCodigoEstado", query="from Pedido order by dataPedido desc, codigoEstadoPedido desc", 
		hints={@QueryHint(value="true", name="org.hibernate.cacheable")})
public class Pedido extends Model {

	private static final long serialVersionUID = -69049043535585581L;

	public enum PedidoEstado {
		ENTREGUE(1, "Entregue"),
		CANCELADO(2, "Cancelado"),
		AGUARDANDO_PAGAMENTO(3, "Aguardando Pagamento"),
		FINALIZADO(4, "Finalizado"),
		ARQUIVADO(5, "Arquivado"),
		PAGO(6, "Pago"),
		AGUARDANDO_ENTREGA(7, "Aguardando Entrega");
		
		private Integer codigo;
		private String estado;
		
		private PedidoEstado(Integer codigo, String estado) {
			this.codigo = codigo;
			this.estado = estado;
		}

		/**
		 * @return the codigo
		 */
		public Integer getCodigo() {
			return codigo;
		}

		/**
		 * @param codigo the codigo to set
		 */
		public void setCodigo(Integer codigo) {
			this.codigo = codigo;
		}

		/**
		 * @return the estado
		 */
		public String getEstado() {
			return estado;
		}

		/**
		 * @param estado the estado to set
		 */
		public void setEstado(String estado) {
			this.estado = estado;
		}
		
	}
	
	@Required
	@Column(name="CODIGO_ESTADO_PEDIDO", length=1, nullable=false)
	private PedidoEstado codigoEstadoPedido;
	
	@Column(name="ULTIMO_ESTADO_PEDIDO", length=1, nullable=true)
	private PedidoEstado ultimoStatusEstadoPedido;
	
	@Required
	@Column(name="VALOR_PEDIDO", nullable=false, scale=2, precision=8)
	private BigDecimal valorPedido;
	
	/**
	 * À partir de 29/05/2012, o valor do código do pedido será o id do carrinho, 
	 * para questões de rastreabilidade. 
	 */
	@MaxSize(value=80)
	@Column(name="CODIGO_PEDIDO", nullable=true, length=80)
	private String codigoPedido;

	@Required
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="DATA_PEDIDO", nullable=false)
	private Date dataPedido;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="DATA_ENTREGA")
	private Date dataEntrega;
	
	@Column(name="OBSERVACAO", nullable=true, length=180)
	private String observacao;
	
	@Required
	@ManyToOne(fetch=FetchType.EAGER, cascade=CascadeType.REFRESH)
	private Cliente cliente;

	@OneToMany(cascade=CascadeType.ALL, fetch=FetchType.LAZY, mappedBy="pedido")
	private List<PedidoItem> itens = null;
	
	@OneToOne(cascade=CascadeType.ALL, fetch=FetchType.EAGER, orphanRemoval=true)
	private Pagamento pagamento = null;
	
	@Required
	@OneToOne(cascade=CascadeType.ALL, fetch=FetchType.LAZY, orphanRemoval=true)
	private Desconto desconto = null;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="DATA_ALTERACAO", nullable=true)
	private Date dataAlteracao;
	
	@Column(name="FLAG_PEDIDO_CESTA_ASSINATURA", nullable=true)
	private Boolean ehPedidoDeCesta;
	
	@Required(message="validation.required")
	@ManyToOne(fetch=FetchType.EAGER, cascade=CascadeType.ALL)
	private Frete frete;
	
	/**
	 * @return the codigoEstadoPedido
	 */
	public PedidoEstado getCodigoEstadoPedido() {
		return codigoEstadoPedido;
	}

	/**
	 * @param codigoEstadoPedido the codigoEstadoPedido to set
	 */
	public void setCodigoEstadoPedido(PedidoEstado codigoEstadoPedido) {
		this.codigoEstadoPedido = codigoEstadoPedido;
	}

	/**
	 * @return the valorPedido
	 */
	public BigDecimal getValorPedido() {
		return valorPedido;
	}

	/**
	 * @param valorPedido the valorPedido to set
	 */
	public void setValorPedido(BigDecimal valorPedido) {
		this.valorPedido = valorPedido;
	}

	/**
	 * @return the codigoPedido
	 */
	public String getCodigoPedido() {
		return codigoPedido;
	}

	/**
	 * @param codigoPedido the codigoPedido to set
	 */
	public void setCodigoPedido(String codigoPedido) {
		this.codigoPedido = codigoPedido;
	}

	/**
	 * @return the dataPedido
	 */
	public Date getDataPedido() {
		return dataPedido;
	}

	/**
	 * @param dataPedido the dataPedido to set
	 */
	public void setDataPedido(Date dataPedido) {
		this.dataPedido = dataPedido;
	}

	/**
	 * @return the cliente
	 */
	public Cliente getCliente() {
		return cliente;
	}

	/**
	 * @param cliente the cliente to set
	 */
	public void setCliente(Cliente cliente) {
		this.cliente = cliente;
	}

	/**
	 * @return the dataEntrega
	 */
	public Date getDataEntrega() {
		return dataEntrega;
	}

	/**
	 * @param dataEntrega the dataEntrega to set
	 */
	public void setDataEntrega(Date dataEntrega) {
		this.dataEntrega = dataEntrega;
	}

	/**
	 * @return the itens
	 */
	public List<PedidoItem> getItens() {
		if(this.itens==null)
			this.itens = new ArrayList<PedidoItem>();
		
		return itens;
	}

	/**
	 * @param itens the itens to set
	 */
	public void setItens(List<PedidoItem> itens) {
		this.itens = itens;
	}

	/**
	 * @return the pagamento
	 */
	public Pagamento getPagamento() {
		return pagamento;
	}

	/**
	 * @param pagamento the pagamento to set
	 */
	public void setPagamento(Pagamento pagamento) {
		this.pagamento = pagamento;
	}
	
	/**
	 * Adiciona os ítens com os respectivos produtos
	 * @param itens
	 */
	public void addPedidoItem(List<CarrinhoItem> itens) {
		if(itens!=null) {
			PedidoItem pedidoItem = null;
			
			for(CarrinhoItem item : itens) {
				pedidoItem = new PedidoItem(this, item.getQuantidade(), item.getProdutos());
				
				getItens().add(pedidoItem);
			}
		}
	}

	/**
	 * @return the desconto
	 */
	public Desconto getDesconto() {
		if(desconto==null)
			this.desconto = new Desconto();
		
		return desconto;
	}

	/**
	 * @param desconto the desconto to set
	 */
	public void setDesconto(Desconto desconto) {
		this.desconto = desconto;
	}
	
	/**
	 * @return the observacao
	 */
	public String getObservacao() {
		return observacao;
	}

	/**
	 * @param observacao the observacao to set
	 */
	public void setObservacao(String observacao) {
		this.observacao = observacao;
	}

	/**
	 * Calcula o valor do desconto. 
	 * Se o valor for preenchido, o mesmo terá prioridade sobre a porcentagem, caso contrário, é calculado através da porcentagem.
	 */
	@Transient
	public void calcularDesconto() {
		if(this.getDesconto().getValorDesconto().doubleValue()>0) {
			this.getDesconto().setPorcentagem( new BigDecimal(this.getDesconto().getValorDesconto().doubleValue() / this.getValorPedido().doubleValue()).multiply(Desconto.CEM_PORCENTO).setScale(2, BigDecimal.ROUND_HALF_UP));
			
		}else if(this.getDesconto().getPorcentagem().doubleValue()>0) {
			this.valorPedido.subtract( this.desconto.getPorcentagem().divide(Desconto.CEM_PORCENTO).multiply(this.valorPedido) ).setScale(2, BigDecimal.ROUND_HALF_UP);
		}
	}
	
	/**
	 * Calcula o valor do pedido aplicando um desconto. 
	 */
	@Transient
	public BigDecimal calcularDesconto(BigDecimal porcentagem) {
		return this.valorPedido.subtract( porcentagem.divide(Desconto.CEM_PORCENTO).multiply(this.valorPedido) ).setScale(2, BigDecimal.ROUND_HALF_DOWN);
	}
	
	@Transient
	public static BigDecimal calcularDesconto(BigDecimal valorPedido, BigDecimal porcentagem) {
		return valorPedido.subtract( porcentagem.divide(Desconto.CEM_PORCENTO).multiply(valorPedido) ).setScale(2, BigDecimal.ROUND_HALF_DOWN);
	}
	
	@Transient
	public BigDecimal getValorComDesconto() {
		if(getDesconto().getValorDesconto().doubleValue()>0)
			return this.valorPedido.subtract( this.desconto.getValorDesconto() ).setScale(2, BigDecimal.ROUND_HALF_DOWN);
		else
			return this.valorPedido.subtract( this.desconto.getPorcentagem().divide(Desconto.CEM_PORCENTO).multiply(this.valorPedido) ).setScale(2, BigDecimal.ROUND_HALF_DOWN);
	}
	
	@Transient
	public static BigDecimal calcularValorTotalPedidos(List<Pedido> pedidos) {
		BigDecimal result = null;
		
		if(pedidos!=null && !pedidos.isEmpty()) {
			result = new BigDecimal(0.0d);
			
			for(Pedido pedido : pedidos)
				result = result.add(pedido.getValorPedido());
		}
		return result;
	}

	/**
	 * @return the dataAlteracao
	 */
	public Date getDataAlteracao() {
		return dataAlteracao;
	}

	/**
	 * @param dataAlteracao the dataAlteracao to set
	 */
	public void setDataAlteracao(Date dataAlteracao) {
		this.dataAlteracao = dataAlteracao;
	}

	/**
	 * @return the ultimoStatusEstadoPedido
	 */
	public PedidoEstado getUltimoStatusEstadoPedido() {
		return ultimoStatusEstadoPedido;
	}

	/**
	 * @param ultimoStatusEstadoPedido the ultimoStatusEstadoPedido to set
	 */
	public void setUltimoStatusEstadoPedido(PedidoEstado ultimoStatusEstadoPedido) {
		this.ultimoStatusEstadoPedido = ultimoStatusEstadoPedido;
	}
	
	@Transient
	public Boolean podeAlterarMetodoPagamento() {
		Boolean result = Boolean.FALSE;
		
		if(!this.codigoEstadoPedido.equals(PedidoEstado.ENTREGUE) 
				&& !this.codigoEstadoPedido.equals(PedidoEstado.FINALIZADO)
				&& !this.codigoEstadoPedido.equals(PedidoEstado.CANCELADO)) {
			result = Boolean.TRUE;
		}
		return result;
	}
	
	public static BigDecimal calcularFrete(BigDecimal valorTotalCompra) {
		BigDecimal result = new BigDecimal(0);
		
		Double valorCompraSemFrete = Double.valueOf(Messages.get("application.frete.valor", ""));
		
		if(valorCompraSemFrete!=null && valorTotalCompra.doubleValue()<valorCompraSemFrete) {
			Frete frete = Frete.findById(1L);
			result = frete.getValor();
		}
		return result;
	}
	
	public void addFrete() {
		this.valorPedido = this.getValorPedido().add(calcularFrete(this.getValorPedido())).setScale(2, BigDecimal.ROUND_HALF_UP);
	}
	
	/**
	 * <p>
	 * 	Gerar um pedido de acordo com uma assinatura de cesta
	 * </p>
	 * @param cestaAssign
	 * @return novo pedido (necessário persistir)
	 */
	public static Pedido newPedido(CestaAssinatura cestaAssign) {
		Pedido pedido = null;
		int size = 0;
		
		try {
			//A cesta precisa estar ativa
			if(cestaAssign!=null && cestaAssign.getAtivo()) {
				Secao secao = null;
				List<CestaAssinaturaProduto> produtosCestaPedido = new ArrayList<CestaAssinaturaProduto>();
				Integer produtosPrimeiraOpcao = Integer.valueOf(0);

				Collections.sort(cestaAssign.getListCestaAssinaturaProduto(), new CestaAssinaturaProdutoComparator(Boolean.FALSE));
				
				pedido = new Pedido();
				pedido.setCodigoEstadoPedido(Pedido.PedidoEstado.AGUARDANDO_ENTREGA);
				pedido.setDataPedido(new Date());
				pedido.setCliente(cestaAssign.getCliente());
				pedido.setValorPedido(cestaAssign.getValorCesta());

				BigDecimal valorPedidoComDesconto = new BigDecimal(Messages.get("application.minValue.paypal", ""));
				
				if(pedido.getValorPedido().doubleValue()>valorPedidoComDesconto.doubleValue()) {
					Desconto desconto = new Desconto(new BigDecimal(Messages.get("application.pedido.paypal.desconto", "")));
					desconto.setDataDesconto(new Date());
					desconto.setPedido(pedido);
					
					pedido.setDesconto(desconto);
				}
				
				Pagamento _pag = new Pagamento();
				_pag.setFormaPagamento(cestaAssign.getPagamento().getFormaPagamento());
				_pag.setValorPagamento(pedido.getValorComDesconto());
				
				pedido.setPagamento(_pag);
				
				pedido.setCodigoPedido("CESTA_"+cestaAssign.id + "_" + new Date().getTime());
				pedido.setObservacao("Pedido gerado automaticamente para assinatura de cesta " + cestaAssign.getPeriodo().getDescricao());
				pedido.setEhPedidoDeCesta(Boolean.TRUE);

				secao = cestaAssign.getListCestaAssinaturaProduto().get(0).getProduto().getSecao();
				
				for(CestaAssinaturaProduto cestaAssinaturaProduto : cestaAssign.getListCestaAssinaturaProduto()) {
					size++;
					
					if(size==1 || !secao.equals(cestaAssinaturaProduto.getProduto().getSecao())) {
						produtosPrimeiraOpcao = getQuantidadeProdutosSecao(cestaAssign.id, cestaAssinaturaProduto.getProduto().getSecao());
						List<CestaAssinaturaProduto> listProdutosPrimeiraOpcao = produtosAssinaturaPrimeiraOpcao(cestaAssign.id, cestaAssinaturaProduto.getProduto().getSecao());
						
						if(!listProdutosPrimeiraOpcao.isEmpty() && produtosPrimeiraOpcao>listProdutosPrimeiraOpcao.size())
							produtosCestaPedido.addAll(produtosAssinaturaOpcional(cestaAssign.id, cestaAssinaturaProduto.getProduto().getSecao(), produtosPrimeiraOpcao-listProdutosPrimeiraOpcao.size()));
						
						produtosCestaPedido.addAll(listProdutosPrimeiraOpcao);
					}
					secao = cestaAssinaturaProduto.getProduto().getSecao();
				}
				pedido.getItens().addAll(PedidoItem.buildItemPedido(produtosCestaPedido, pedido));
				
				pedido.getFrete().setValor(Pedido.calcularFrete(pedido.getValorPedido()));
			}
			
		}catch(Exception e) {
			Logger.error(e, "Erro ao tentar gerar um novo pedido para a cesta: %s", cestaAssign.id);
			throw new RuntimeException(e);
		}
		return pedido;
	}
	
	private static Integer getQuantidadeProdutosSecao(Long idCestaAssinatura, Secao secao) {
		List<Map<Long, String>> listCestaAssinaturaProduto = new ArrayList<Map<Long,String>>();
		Integer result = null;
		
		Query query = JPA.em().createQuery("select new map(count(sec.id) as qtd, sec.descricao as secao) from Produto as prd, Secao as sec, CestaAssinaturaProduto as cap " +
				"where prd.secao.id = sec.id AND prd.id = cap.produto.id AND " +
				"cap.cestaAssinatura.id =:id AND cap.opcional =:isOpcional " +
				"AND sec.id =:idSecao " +
				"group by sec.id, sec.descricao " +
				"order by sec.descricao ASC");
		query.setParameter("id", idCestaAssinatura);
		query.setParameter("isOpcional", Boolean.FALSE);
		query.setParameter("idSecao", secao.id);
	
		listCestaAssinaturaProduto = query.getResultList();

		if(!listCestaAssinaturaProduto.isEmpty())
			result = Integer.valueOf( listCestaAssinaturaProduto.get(0).values().toArray()[0].toString() );
		
		Logger.info("#### Quantidade de produtos por seção %s, idCestaAssinatura%s: %s  #####", secao.getDescricao(), idCestaAssinatura, listCestaAssinaturaProduto.size());
		
		return result;
	}
	
	private static List<CestaAssinaturaProduto> produtosAssinaturaOpcional(Long idCestaAssinaturaProduto, Secao secao, Integer results) {
		List<CestaAssinaturaProduto> result = new ArrayList<CestaAssinaturaProduto>();
		
		Query query = JPA.em().createQuery("select cap from Produto as prd, Secao as sec, CestaAssinaturaProduto as cap " +
				"where prd.secao.id = sec.id AND prd.id = cap.produto.id AND " +
				"cap.cestaAssinatura.id =:id AND cap.opcional =:isOpcional " +
				"AND sec.id =:idSecao");
		
		query.setParameter("id", idCestaAssinaturaProduto);
		query.setParameter("isOpcional", Boolean.TRUE);
		query.setMaxResults(results);
		query.setParameter("idSecao", secao.id);
		
		Logger.info("#### Quantidade de produtos segunda opção para seção %s, idCestaAssinatura%s: %s  #####", secao.getDescricao(), idCestaAssinaturaProduto, result.size());
	
		result = query.getResultList();
		
		return result;
	}
	
	private static List<CestaAssinaturaProduto> produtosAssinaturaPrimeiraOpcao(Long idCestaAssinatura, Secao secao) {
		List<CestaAssinaturaProduto> result = new ArrayList<CestaAssinaturaProduto>();
		
		Query query = JPA.em().createQuery("select cap from Produto as prd, Secao as sec, CestaAssinaturaProduto as cap " +
				"where prd.secao.id = sec.id AND prd.id = cap.produto.id AND " +
				"cap.cestaAssinatura.id =:id AND cap.opcional =:isOpcional " +
				"AND sec.id =:idSecao AND prd.ativo = 1");
		
		query.setParameter("id", idCestaAssinatura);
		query.setParameter("isOpcional", Boolean.FALSE);
		query.setParameter("idSecao", secao.id);
	
		result = query.getResultList();
		
		Logger.info("#### Quantidade de produtos primeira opção para seção %s, idCestaAssinatura%s: %s  #####", secao.getDescricao(), idCestaAssinatura, result.size());
		
		return result;
	}

	/**
	 * @return the ehPedidoDeCesta
	 */
	public Boolean getEhPedidoDeCesta() {
		if(ehPedidoDeCesta==null)
			ehPedidoDeCesta = Boolean.FALSE;
		
		return ehPedidoDeCesta;
	}

	/**
	 * @param ehPedidoDeCesta the ehPedidoDeCesta to set
	 */
	public void setEhPedidoDeCesta(Boolean ehPedidoDeCesta) {
		this.ehPedidoDeCesta = ehPedidoDeCesta;
	}

	/**
	 * @return the frete
	 */
	public Frete getFrete() {
		return frete;
	}

	/**
	 * @param frete the frete to set
	 */
	public void setFrete(Frete frete) {
		this.frete = frete;
	}
	
	public BigDecimal getValorTotal() {
		BigDecimal valorFrete = (this.getFrete()==null || this.getFrete().getValor()==null) ? BigDecimal.ZERO : this.getFrete().getValor();
		
		return valorFrete.add(getValorComDesconto());
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime
				* result
				+ ((codigoEstadoPedido == null) ? 0 : codigoEstadoPedido
						.hashCode());
		result = prime * result
				+ ((codigoPedido == null) ? 0 : codigoPedido.hashCode());
		result = prime * result
				+ ((dataPedido == null) ? 0 : dataPedido.hashCode());
		result = prime * result
				+ ((observacao == null) ? 0 : observacao.hashCode());
		result = prime * result
				+ ((valorPedido == null) ? 0 : valorPedido.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (!(obj instanceof Pedido)) {
			return false;
		}
		Pedido other = (Pedido) obj;
		
		if (this.id != other.id) {
			return false;
		}
		if (codigoEstadoPedido != other.codigoEstadoPedido) {
			return false;
		}
		if (codigoPedido == null) {
			if (other.codigoPedido != null) {
				return false;
			}
		} else if (!codigoPedido.equals(other.codigoPedido)) {
			return false;
		}
		if (dataPedido == null) {
			if (other.dataPedido != null) {
				return false;
			}
		} else if (!dataPedido.equals(other.dataPedido)) {
			return false;
		}
		if (observacao == null) {
			if (other.observacao != null) {
				return false;
			}
		} else if (!observacao.equals(other.observacao)) {
			return false;
		}
		if (valorPedido == null) {
			if (other.valorPedido != null) {
				return false;
			}
		} else if (!valorPedido.equals(other.valorPedido)) {
			return false;
		}
		return true;
	}

	
}
