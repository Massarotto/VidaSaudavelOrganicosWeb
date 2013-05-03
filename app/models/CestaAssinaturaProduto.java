/**
 * 
 */
package models;

import java.io.Serializable;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import play.db.jpa.Model;

/**
 * @author guerrafe
 *
 */
@Cache(usage=CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Cacheable
@Entity
@Table(name="CESTA_ASSINATURA_PRODUTO")
public class CestaAssinaturaProduto extends Model implements Serializable, Comparable<CestaAssinaturaProduto> {

	private static final long serialVersionUID = 6956196392033887347L;

	@ManyToOne(fetch=FetchType.EAGER)
	private CestaAssinatura cestaAssinatura;
	
	@ManyToOne(fetch=FetchType.EAGER)
	private Produto produto;
	
	@Column(name="PRODUTO_OPCIONAL", nullable=false)
	private Boolean opcional;
	
	@Column(name="QUANTIDADE", nullable=false)
	private Integer quantidade;
	
	public CestaAssinaturaProduto() {
		// TODO Auto-generated constructor stub
	}
	
	public CestaAssinaturaProduto(CestaAssinatura cestaAssinatura, Produto produto, Boolean opcional) {
		this.cestaAssinatura = cestaAssinatura;
		this.produto = produto;
		this.opcional = opcional;
	}

	/**
	 * @return the cestaAssinatura
	 */
	public CestaAssinatura getCestaAssinatura() {
		return cestaAssinatura;
	}

	/**
	 * @param cestaAssinatura the cestaAssinatura to set
	 */
	public void setCestaAssinatura(CestaAssinatura cestaAssinatura) {
		this.cestaAssinatura = cestaAssinatura;
	}

	/**
	 * @return the produto
	 */
	public Produto getProduto() {
		return produto;
	}

	/**
	 * @param produto the produto to set
	 */
	public void setProduto(Produto produto) {
		this.produto = produto;
	}

	/**
	 * @return the opcional
	 */
	public Boolean getOpcional() {
		return opcional;
	}

	/**
	 * @param opcional the opcional to set
	 */
	public void setOpcional(Boolean opcional) {
		this.opcional = opcional;
	}

	/**
	 * @return the quantidade
	 */
	public Integer getQuantidade() {
		return quantidade;
	}

	/**
	 * @param quantidade the quantidade to set
	 */
	public void setQuantidade(Integer quantidade) {
		this.quantidade = quantidade;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((cestaAssinatura == null) ? 0 : cestaAssinatura.hashCode());
		result = prime * result
				+ ((opcional == null) ? 0 : opcional.hashCode());
		result = prime * result + ((produto == null) ? 0 : produto.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (!(obj instanceof CestaAssinaturaProduto))
			return false;
		CestaAssinaturaProduto other = (CestaAssinaturaProduto) obj;
		if (cestaAssinatura == null) {
			if (other.cestaAssinatura != null)
				return false;
		} else if (!cestaAssinatura.equals(other.cestaAssinatura))
			return false;
		if (opcional == null) {
			if (other.opcional != null)
				return false;
		} else if (!opcional.equals(other.opcional))
			return false;
		if (produto == null) {
			if (other.produto != null)
				return false;
		} else if (!produto.equals(other.produto))
			return false;
		return true;
	}

	@Override
	public int compareTo(CestaAssinaturaProduto o) {
		if(o==null || o.getProduto()==null)
			return 0;
		
		return getProduto().id.compareTo(o.getProduto().id);
	}
	
}
