/**
 * 
 */
package models;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import business.produto.layout.LayoutArquivo;

import play.data.validation.MinSize;
import play.data.validation.Required;
import play.db.jpa.Model;

/**
 * @author guerrafe
 *
 */
@Cache(usage=CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Cacheable
@Table(name="FORNECEDOR")
@Entity
public class Fornecedor extends Model {
	
	@Required(message="message.required.fornecedor.nome")
	@Column(name="NOME", length=180, nullable=false)
	private String nome;
	
	@MinSize(value=11, message="message.minsize.fornecedor.cnpj")
	@Column(name="CNPJ", length=14, nullable=true)
	private String cnpj;
	
	@Column(name="FLAG_ATIVO", nullable=false)
	private Boolean ativo;
	
	@OneToMany(fetch=FetchType.LAZY, cascade=CascadeType.REFRESH, mappedBy="fornecedor")
	private List<Produto> produtos;
	
	@Column(name="LAYOUT_ARQUIVO_PRODUTO", nullable=true)
	private LayoutArquivo layoutArquivo;
	
	@Column(name="EMAIL_CONTATO", nullable=true, length=300)
	private String emailContato;
	
	/**
	 * @return the nome
	 */
	public String getNome() {
		return nome;
	}

	/**
	 * @param nome the nome to set
	 */
	public void setNome(String nome) {
		this.nome = nome;
	}

	/**
	 * @return the cnpj
	 */
	public String getCnpj() {
		return cnpj;
	}

	/**
	 * @param cnpj the cnpj to set
	 */
	public void setCnpj(String cnpj) {
		this.cnpj = cnpj;
	}

	/**
	 * @return the produtos
	 */
	public List<Produto> getProdutos() {
		if(produtos==null)
			produtos = new ArrayList<Produto>();
		
		return produtos;
	}

	/**
	 * @param produtos the produtos to set
	 */
	public void setProdutos(List<Produto> produtos) {
		this.produtos = produtos;
	}
	
	public void addProduto(Produto produto) {
		if(produto!=null) {
			getProdutos().add(produto);
			
			produto.setFornecedor(this);
		}
	}

	/**
	 * @return the ativo
	 */
	public Boolean getAtivo() {
		return ativo;
	}

	/**
	 * @param ativo the ativo to set
	 */
	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}

	/**
	 * @return the layoutArquivo
	 */
	public LayoutArquivo getLayoutArquivo() {
		return layoutArquivo;
	}

	/**
	 * @param layoutArquivo the layoutArquivo to set
	 */
	public void setLayoutArquivo(LayoutArquivo layoutArquivo) {
		this.layoutArquivo = layoutArquivo;
	}

	/**
	 * @return the emailContato
	 */
	public String getEmailContato() {
		return emailContato;
	}

	/**
	 * @param emailContato the emailContato to set
	 */
	public void setEmailContato(String emailContato) {
		this.emailContato = emailContato;
	}

}
