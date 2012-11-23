/**
 * 
 */
package models;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.br.BrazilianAnalyzer;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Store;

import play.data.validation.Min;
import play.data.validation.Required;
import play.db.jpa.Model;

/**
 * @author guerrafe
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name="Produto", propOrder={
		"codigoProduto",
		"nome",
		"descricao",
		"valorVenda",
		"imagemProduto",
		"dataCadastro",
		"ativo",
		"ehPromocao",
		"ehCesta",
		"dataValidade"
})
@XmlRootElement(name="Produto")
@Cache(usage=CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Cacheable
@Entity
@Table(name="PRODUTO")
@Indexed(index="Produto")
public class Produto extends Model implements Comparable<Produto> {
	
	public Produto() {
	}
	
	public Produto(Long id) {
		this.id = id;
	}
	
	public Produto(String nome, String descricao, String codigoProduto, Boolean ativo) {
		this.nome = nome;
		this.descricao = descricao;
		this.codigoProduto = codigoProduto;
		this.ativo = ativo;
	}
	
	
	public Produto(Long id, String nome, String descricao, 
					String codigoProduto, Double valorPago, Double valorVenda,
					String imagemProduto, String detalhe) {
		this.id = id;
		this.nome = nome;
		this.descricao = descricao;
		this.valorPago = valorPago;
		this.valorVenda = valorVenda;
		this.imagemProduto = imagemProduto;
		this.detalhe = detalhe;
	}
	
	public Produto(Long id, String nome, String descricao, 
					String codigoProduto, Double valorPago, Double valorVenda,
					String imagemProduto, File imagem, String detalhe) {
		this.id = id;
		this.nome = nome;
		this.descricao = descricao;
		this.valorPago = valorPago;
		this.valorVenda = valorVenda;
		this.imagemProduto = imagemProduto;
		this.imagem = imagem;
		this.detalhe = detalhe;
	}
	
	@XmlElement(name="CodigoProduto", required=true)
	@Required(message="message.required.product.codigo")
	@Column(name="CODIGO_PRODUTO", length=60, nullable=false, unique=true)
	private String codigoProduto;
	
	@Field(index=Index.TOKENIZED, store=Store.YES, analyzer=@org.hibernate.search.annotations.Analyzer(impl=BrazilianAnalyzer.class))
	@XmlElement(name="Nome", required=true)
	@Required(message="message.required.product.nome")
	@Column(name="NOME", length=80, nullable=false)
	private String nome;
	
	@Field(index=Index.TOKENIZED, store=Store.NO, analyzer=@org.hibernate.search.annotations.Analyzer(impl=BrazilianAnalyzer.class))
	@XmlElement(name="Descricao", required=false)
	@Required(message="message.required.product.descricao")
	@Column(name="DESCRICAO", length=150, nullable=false)
	private String descricao;
	
	@XmlTransient
	@Required(message="message.required.product.valorPago")
	@Column(name="VALOR_PAGO", nullable=false)
	private Double valorPago = 0.0;
	
	@Min(0.01)
	@XmlElement(name="Preco", required=true)
	@Required(message="message.required.product.valorVenda")
	@Column(name="VALOR_VENDA", nullable=false)
	private Double valorVenda;

	@XmlElement(name="ImagemProduto", required=false)
	@Column(name="IMAGEM_PRODUTO", length=60, nullable=false)
	private String imagemProduto;
	
	@XmlTransient
	@Transient
	private File imagem;
	
	@XmlElement(name="DataCadastro", required=true)
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="DATA_CADASTRO", nullable=false)
	private Date dataCadastro;
	
	@XmlElement(name="DataValidade", required=true)
	@Temporal(TemporalType.DATE)
	@Column(name="DATA_VALIDADE", nullable=true)
	private Date dataValidade;
	
	@Field(index=Index.UN_TOKENIZED, store=Store.YES)
	@XmlElement(name="Ativo", required=true)
	@Column(name="FLAG_ATIVO", nullable=false)
	private Boolean ativo;
	
	@XmlElement(name="EhPromocao", required=false)
	@Column(name="FLAG_PROMOCAO", nullable=false)
	private Boolean ehPromocao = Boolean.FALSE;
	
	@XmlTransient
	@OneToOne(fetch=FetchType.EAGER)
	private ProdutoEstoque produtoEstoque = null;
	
	@XmlTransient
	@ManyToOne(fetch=FetchType.EAGER)
	private Secao secao;
	
	@Field(index=Index.TOKENIZED, store=Store.YES, analyzer=@org.hibernate.search.annotations.Analyzer(impl=BrazilianAnalyzer.class))
	@XmlTransient
	@Column(name="DETALHE", length=800, nullable=true)
	private String detalhe;
	
	@XmlElement(name="EhCesta", required=false)
	@Column(name="FLAG_CESTA", nullable=true)
	private Boolean ehCesta = Boolean.FALSE;
	
	@XmlTransient
	@ManyToMany(mappedBy="produtos")
	private List<PedidoItem> listPedidoItem = null;

	@XmlTransient
	@ManyToMany(mappedBy="produtos")
	private List<CarrinhoItem> listCarrinhoItem = null;
	
	@XmlTransient
	@ManyToOne(fetch=FetchType.EAGER)
	private Fornecedor fornecedor;
	
	@XmlTransient
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="DATA_ALTERACAO", nullable=true)
	private Date dataAlteracao;
	
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
	 * @return the descricao
	 */
	public String getDescricao() {
		return descricao;
	}

	/**
	 * @param descricao the descricao to set
	 */
	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	/**
	 * @return the valorPago
	 */
	public Double getValorPago() {
		return valorPago;
	}

	/**
	 * @param valorPago the valorPago to set
	 */
	public void setValorPago(Double valorPago) {
		this.valorPago = valorPago;
	}

	/**
	 * @return the imagemProduto
	 */
	public String getImagemProduto() {
		return imagemProduto;
	}

	/**
	 * @param imagemProduto the imagemProduto to set
	 */
	public void setImagemProduto(String imagemProduto) {
		this.imagemProduto = imagemProduto;
	}

	/**
	 * @return the imagem
	 */
	public File getImagem() {
		return imagem;
	}

	/**
	 * @param imagem the imagem to set
	 */
	public void setImagem(File imagem) {
		this.imagem = imagem;
	}

	/**
	 * @return the valorVenda
	 */
	public Double getValorVenda() {
		return valorVenda;
	}

	/**
	 * @param valorVenda the valorVenda to set
	 */
	public void setValorVenda(Double valorVenda) {
		this.valorVenda = valorVenda;
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
	 * @return the produtoEstoque
	 */
	public ProdutoEstoque getProdutoEstoque() {
		return produtoEstoque;
	}

	/**
	 * @param produtoEstoque the produtoEstoque to set
	 */
	public void setProdutoEstoque(ProdutoEstoque produtoEstoque) {
		this.produtoEstoque = produtoEstoque;
	}
	
	/**
	 * @return the secao
	 */
	public Secao getSecao() {
		return secao;
	}

	/**
	 * @param secao the secao to set
	 */
	public void setSecao(Secao secao) {
		this.secao = secao;
	}

	@Override
	public int compareTo(Produto o) {
		if(o==null)
			return 0;
		else
			return this.id.compareTo(o.id);
	}

	/**
	 * @return the detalhe
	 */
	public String getDetalhe() {
		return detalhe;
	}

	/**
	 * @param detalhe the detalhe to set
	 */
	public void setDetalhe(String detalhe) {
		this.detalhe = detalhe;
	}

	/**
	 * @return the listPedidoItem
	 */
	public List<PedidoItem> getListPedidoItem() {
		if(listPedidoItem==null)
			listPedidoItem = new ArrayList<PedidoItem>();
		
		return listPedidoItem;
	}

	/**
	 * @param listPedidoItem the listPedidoItem to set
	 */
	public void setListPedidoItem(List<PedidoItem> listPedidoItem) {
		this.listPedidoItem = listPedidoItem;
	}

	/**
	 * @return the fornecedor
	 */
	public Fornecedor getFornecedor() {
		return fornecedor;
	}

	/**
	 * @param fornecedor the fornecedor to set
	 */
	public void setFornecedor(Fornecedor fornecedor) {
		this.fornecedor = fornecedor;
	}

	/**
	 * @return the codigoProduto
	 */
	public String getCodigoProduto() {
		return codigoProduto;
	}

	/**
	 * @param codigoProduto the codigoProduto to set
	 */
	public void setCodigoProduto(String codigoProduto) {
		this.codigoProduto = codigoProduto;
	}

	public Boolean getEhPromocao() {
		return ehPromocao;
	}
	
	public void setEhPromocao(Boolean ehPromocao) {
		this.ehPromocao = ehPromocao;
	}

	/**
	 * @return the ehCesta
	 */
	public Boolean getEhCesta() {
		if(ehCesta==null)
			ehCesta = Boolean.FALSE;
		
		return ehCesta;
	}

	/**
	 * @param ehCesta the ehCesta to set
	 */
	public void setEhCesta(Boolean ehCesta) {
		this.ehCesta = ehCesta;
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
	 * @return the dataValidade
	 */
	public Date getDataValidade() {
		return dataValidade;
	}

	/**
	 * @param dataValidade the dataValidade to set
	 */
	public void setDataValidade(Date dataValidade) {
		this.dataValidade = dataValidade;
	}

	/**
	 * @return the listCarrinhoItem
	 */
	public List<CarrinhoItem> getListCarrinhoItem() {
		if(listCarrinhoItem==null)
			listCarrinhoItem = new ArrayList<CarrinhoItem>();
		
		return listCarrinhoItem;
	}

	/**
	 * @param listCarrinhoItem the listCarrinhoItem to set
	 */
	public void setListCarrinhoItem(List<CarrinhoItem> listCarrinhoItem) {
		this.listCarrinhoItem = listCarrinhoItem;
	}

	/**
	 * Se não possui estoque, produto <i>on demand</i>, sempre ativo.
	 * Se possui estoque ativo e a quantidade é maior que 0, então está disponível, senão, se possui estoque 
	 * ativo, indisponível.
	 * @return
	 */
	@Transient
	public Boolean isAvailable() {
		Boolean result = Boolean.FALSE;
		
		if(this.getProdutoEstoque()==null || !this.getProdutoEstoque().getAtivo()) {
			result = Boolean.TRUE;
		
		}else if(this.getProdutoEstoque().getAtivo() && this.getProdutoEstoque().getQuantidade()>0) {
			result = Boolean.TRUE;
		}
		return result;
	}
	
}