/**
 * 
 */
package models;

import java.math.BigDecimal;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import play.data.validation.Required;
import play.db.jpa.Model;

/**
 * @author Felipe G. de Oliveira
 *
 */
@Cache(usage=CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Cacheable
@Entity
@Table(name="FRETE")
public class Frete extends Model {
	
	public Frete() {}
	
	public Frete(BigDecimal valor) {
		this.valor = valor;
	}
	
	@Required(message="validation.required")
	@Column(name="VALOR", nullable=false, scale=2, precision=8)
	private BigDecimal valor;

	/**
	 * @return the valor
	 */
	public BigDecimal getValor() {
		return valor;
	}

	/**
	 * @param valor the valor to set
	 */
	public void setValor(BigDecimal valor) {
		this.valor = valor;
	}
}
