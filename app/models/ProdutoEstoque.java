/**
 * 
 */
package models;

import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import play.db.jpa.Model;

/**
 * @author guerrafe
 *
 */
@Entity
@Table(name="PRODUTO_ESTOQUE")
public class ProdutoEstoque extends Model {
	
	private static final long serialVersionUID = -1444835031615910043L;

	@Column(name="QUANTIDADE", length=3, nullable=false)
	private Integer quantidade;
	
	@OneToOne(cascade=CascadeType.ALL, fetch=FetchType.EAGER, mappedBy="produtoEstoque")
	private Produto produto = null;
	
	@Column(name="DATA_CADASTRO", nullable=false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date dataCadastro;

	@Column(name="DATA_ALTERACAO", nullable=true)
	@Temporal(TemporalType.TIMESTAMP)
	private Date dataAlteracao;
	
	@Column(name="USUARIO_ALTERACAO", nullable=true, length=80)
	private String usuarioAlteracao;
	
	@Column(name="PRECO", nullable=false, scale=2)
	private Double preco = 0.0;
	
	@Column(name="FLAG_ENVIARELATORIO", nullable=true)
	private Boolean enviarRelatorio = Boolean.TRUE;
	
	@Column(name="FLAG_ATIVO", nullable=true)
	private Boolean ativo = Boolean.TRUE;
	
	/**
	 * @return the quantidade
	 */
	public Integer getQuantidade() {
		if(quantidade==null)
			quantidade = Integer.valueOf(0);
		
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
	public Produto getProduto() {
		return produto;
	}

	/**
	 * @param produtos the produtos to set
	 */
	public void setProduto(Produto produto) {
		this.produto = produto;
	}

	/**
	 * @return the dataCadastro
	 */
	public Date getDataCadastro() {
		return dataCadastro;
	}

	/**
	 * @param dataCadastro the dataCadastro to set
	 */
	public void setDataCadastro(Date dataCadastro) {
		this.dataCadastro = dataCadastro;
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
	 * @return the usuarioAlteracao
	 */
	public String getUsuarioAlteracao() {
		return usuarioAlteracao;
	}

	/**
	 * @param usuarioAlteracao the usuarioAlteracao to set
	 */
	public void setUsuarioAlteracao(String usuarioAlteracao) {
		this.usuarioAlteracao = usuarioAlteracao;
	}

	/**
	 * @return the preco
	 */
	public Double getPreco() {
		return preco;
	}

	/**
	 * @param preco the preco to set
	 */
	public void setPreco(Double preco) {
		this.preco = preco;
	}

	/**
	 * @return the enviarRelatorio
	 */
	public Boolean getEnviarRelatorio() {
		return enviarRelatorio;
	}

	/**
	 * @param enviarRelatorio the enviarRelatorio to set
	 */
	public void setEnviarRelatorio(Boolean enviarRelatorio) {
		this.enviarRelatorio = enviarRelatorio;
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

}
