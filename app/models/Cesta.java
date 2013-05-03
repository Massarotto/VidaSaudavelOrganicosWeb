/**
 * 
 */
package models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

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
@Table(name="CESTA")
public class Cesta extends Model {

	private static final long serialVersionUID = 198765234567889L;
	
	@Required(message="Por favor, preencha o título")
	@Column(name="TITULO_CESTA", nullable=false, length=300)
	private String titulo;
	
	@OneToMany(cascade=CascadeType.ALL, fetch=FetchType.LAZY, mappedBy="cesta", orphanRemoval=true)
	private List<CestaProduto> produtosAtivos;
	
	@Column(name="DATA_CADASTRO", nullable=false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date dataCadastro;
	
	@Column(name="DATA_ALTERACAO", nullable=true)
	@Temporal(TemporalType.TIMESTAMP)
	private Date dataAlteracao;

	@Column(name="FLAG_ATIVO", nullable=false)
	private Boolean ativo;
	
	public Date getDataCadastro() {
		return dataCadastro;
	}

	public void setDataCadastro(Date dataCadastro) {
		this.dataCadastro = dataCadastro;
	}

	public List<CestaProduto> getProdutosAtivos() {
		if(this.produtosAtivos==null)
			this.produtosAtivos = new ArrayList<CestaProduto>();
			
		return produtosAtivos;
	}
	
	/**
	 * Adiciona um produto à uma cesta
	 * @param produto
	 * @return true or false
	 */
	public boolean addProdutoAtivo(Produto produto, Boolean opcional) {
		if(produto==null)
			return false;
		
		final CestaProduto cestaProduto = new CestaProduto();
		cestaProduto.setProduto(produto);
		cestaProduto.setOpcional(opcional);
		cestaProduto.setCesta(this);
		
		return getProdutosAtivos().add(cestaProduto); 
	}

	public void setProdutosAtivos(List<CestaProduto> produtosAtivos) {
		this.produtosAtivos = produtosAtivos;
	}
	
	public List<Produto> getTodosProdutosAtivos() {
		List<Produto> produtos = new ArrayList<Produto>();
		
		for(CestaProduto cestaProduto : getProdutosAtivos()) {
			produtos.add(cestaProduto.getProduto());
		}
		
		return produtos;
	}

	public Date getDataAlteracao() {
		return dataAlteracao;
	}

	public void setDataAlteracao(Date dataAlteracao) {
		this.dataAlteracao = dataAlteracao;
	}

	public String getTitulo() {
		return titulo;
	}

	public void setTitulo(String titulo) {
		this.titulo = titulo;
	}

	public Boolean getAtivo() {
		return ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}
	
}
