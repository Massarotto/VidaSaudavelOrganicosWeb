/**
 * 
 */
package models;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import play.data.validation.Required;
import play.db.jpa.Model;

/**
 * @author guerrafe
 *
 */
@Cache(usage=CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Cacheable
@Entity
@Table(name="PEDIDO_ITEM")
public class PedidoItem extends Model {
	
	@ManyToOne(fetch=FetchType.EAGER)
	private Pedido pedido;
	
	@Required
	@Column(name="QUANTIDADE", nullable=false)
	private Integer quantidade;
	
	@ManyToMany(fetch=FetchType.LAZY)
	private List<Produto> produtos;
	
	@Column(name="ITEM_EXCLUIDO", nullable=true)
	private Boolean excluido = Boolean.FALSE;

	public PedidoItem() {
	}
	
	public PedidoItem(Pedido pedido, Integer qtde, List<Produto> produtos) {
		this.pedido = pedido;
		this.quantidade = qtde;
		getProdutos().addAll(produtos);
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

	/**
	 * @return the produtos
	 */
	public List<Produto> getProdutos() {
		if(this.produtos==null)
			this.produtos = new ArrayList<Produto>();
		
		return produtos;
	}

	/**
	 * @param produtos the produtos to set
	 */
	public void setProdutos(List<Produto> produtos) {
		this.produtos = produtos;
	}
	
	public void addQuantidade() {
		if(getQuantidade()==null)
			setQuantidade(0);

		this.quantidade++;
	}

	/**
	 * @return the excluido
	 */
	public Boolean getExcluido() {
		return excluido;
	}

	/**
	 * @param excluido the excluido to set
	 */
	public void setExcluido(Boolean excluido) {
		this.excluido = excluido;
	}
	
}
