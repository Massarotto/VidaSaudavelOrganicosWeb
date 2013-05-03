/**
 * 
 */
package models;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import play.data.validation.Min;
import play.data.validation.Required;
import play.db.jpa.Model;

/**
 * @author guerrafe
 *
 */
@Entity
@Cache(usage=CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Cacheable
@Table(name="DESCONTO")
public class Desconto extends Model {

	private static final long serialVersionUID = 2230720664447243872L;

	public final static BigDecimal CEM_PORCENTO = new BigDecimal("100.0");
	
	@Required
	@Min(value=0, message="message.minsize.value.desconto")
	@Column(name="PORCENTAGEM", nullable=false, scale=2, precision=4)
	private BigDecimal porcentagem;
	
	@Required
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="DATA_DESCONTO", nullable=true)
	private Date dataDesconto;
	
	@Required
	@Min(value=0, message="message.minsize.value.desconto")
	@Column(name="VALOR_DESCONTO", nullable=false, scale=2, precision=8)
	private BigDecimal valorDesconto;
	
	@Required
	@ManyToOne(fetch=FetchType.LAZY, cascade=CascadeType.REFRESH)
	private Usuario usuario;
	
	@OneToOne(mappedBy="desconto", cascade=CascadeType.ALL)
	private Pedido pedido;
	
	public Desconto() {
		this.porcentagem = new BigDecimal(0);
		this.valorDesconto = new BigDecimal(0);
	}
	
	public Desconto(BigDecimal porcentagem) {
		this.porcentagem = porcentagem;
		this.valorDesconto = new BigDecimal(0);
	}

	/**
	 * @return the porcentagem
	 */
	public BigDecimal getPorcentagem() {
		if(this.porcentagem==null)
			this.porcentagem = BigDecimal.ZERO;
		
		return porcentagem;
	}

	/**
	 * @param porcentagem the porcentagem to set
	 */
	public void setPorcentagem(BigDecimal porcentagem) {
		if(porcentagem.equals(BigDecimal.ZERO))
			this.valorDesconto = BigDecimal.ZERO;
		
		this.porcentagem = porcentagem;
	}

	/**
	 * @return the dataDesconto
	 */
	public Date getDataDesconto() {
		return dataDesconto;
	}

	/**
	 * @param dataDesconto the dataDesconto to set
	 */
	public void setDataDesconto(Date dataDesconto) {
		this.dataDesconto = dataDesconto;
	}

	/**
	 * @return the usuario
	 */
	public Usuario getUsuario() {
		return usuario;
	}

	/**
	 * @param usuario the usuario to set
	 */
	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

	/**
	 * @return the pedido
	 */
	public Pedido getPedido() {
		return pedido;
	}

	/**
	 * @param pedido the pedido to set
	 */
	public void setPedido(Pedido pedido) {
		this.pedido = pedido;
	}

	/**
	 * @return the valorDesconto
	 */
	public BigDecimal getValorDesconto() {
		if((valorDesconto==null || valorDesconto.doubleValue()<=0) && this.getPedido()!=null)
			return getPorcentagem().divide(CEM_PORCENTO).multiply(this.getPedido().getValorPedido()).setScale(2, BigDecimal.ROUND_HALF_DOWN);
		
		return valorDesconto;
	}

	/**
	 * @param valorDesconto the valorDesconto to set
	 */
	public void setValorDesconto(BigDecimal valorDesconto) {
		if(valorDesconto.equals(BigDecimal.ZERO))
			this.porcentagem = BigDecimal.ZERO;
		
		this.valorDesconto = valorDesconto;
	}
	
}
