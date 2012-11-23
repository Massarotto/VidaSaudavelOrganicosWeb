/**
 * 
 */
package models;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import play.Logger;
import play.data.validation.Max;
import play.data.validation.MaxSize;
import play.data.validation.Required;
import play.data.validation.Valid;
import play.db.jpa.Model;

/**
 * @author guerrafe
 *
 */
@Cache(usage=CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Cacheable
@Entity
@Table(name="PEDIDO")
public class Pedido extends Model {
	
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
	
}
