package vo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import controllers.Relatorios;

import util.PedidoFornecedorComparator;

import models.Frete;
import models.Pedido;
import models.PedidoItem;
import models.Produto;

public class PedidoProdutoEntregaReportVO implements Serializable, Comparable<PedidoProdutoEntregaReportVO> {

	private Long id;
	private String nome;
	private String logradouro;
	private String complemento;
	private String bairro;
	private String cidade;
	private String cep;
	private String uf;
	private Integer numero;
	private Date dataPedido;
	private String prefixoTelefone;
	private String telefone;
	
	private String observacao;
	
	private List<ProdutoPedidoReportVO> produtos;
	
	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}



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
	 * @return the logradouro
	 */
	public String getLogradouro() {
		return logradouro;
	}



	/**
	 * @param logradouro the logradouro to set
	 */
	public void setLogradouro(String logradouro) {
		this.logradouro = logradouro;
	}



	/**
	 * @return the complemento
	 */
	public String getComplemento() {
		return complemento;
	}



	/**
	 * @param complemento the complemento to set
	 */
	public void setComplemento(String complemento) {
		this.complemento = complemento;
	}



	/**
	 * @return the bairro
	 */
	public String getBairro() {
		return bairro;
	}



	/**
	 * @param bairro the bairro to set
	 */
	public void setBairro(String bairro) {
		this.bairro = bairro;
	}



	/**
	 * @return the cidade
	 */
	public String getCidade() {
		return cidade;
	}



	/**
	 * @param cidade the cidade to set
	 */
	public void setCidade(String cidade) {
		this.cidade = cidade;
	}



	/**
	 * @return the cep
	 */
	public String getCep() {
		return cep;
	}



	/**
	 * @param cep the cep to set
	 */
	public void setCep(String cep) {
		this.cep = cep;
	}



	/**
	 * @return the uf
	 */
	public String getUf() {
		return uf;
	}



	/**
	 * @param uf the uf to set
	 */
	public void setUf(String uf) {
		this.uf = uf;
	}



	/**
	 * @return the numero
	 */
	public Integer getNumero() {
		return numero;
	}



	/**
	 * @param numero the numero to set
	 */
	public void setNumero(Integer numero) {
		this.numero = numero;
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
	 * @return the pedidos
	 */
	public List<ProdutoPedidoReportVO> getProdutos() {
		if(this.produtos==null)
			this.produtos = new ArrayList<ProdutoPedidoReportVO>();
		
		return produtos;
	}

	/**
	 * @param pedidos the pedidos to set
	 */
	public void setProdutos(List<ProdutoPedidoReportVO> produtos) {
		this.produtos = produtos;
	}

	@Override
	public int compareTo(PedidoProdutoEntregaReportVO o) {
		if(o.id==null)
			return 0;
		else
			return this.id.compareTo(o.id);
	}
	
	public static List<PedidoProdutoEntregaReportVO> fillListReport(List<Pedido> pedidos) {
		List<PedidoProdutoEntregaReportVO> result = new ArrayList<PedidoProdutoEntregaReportVO>();
		PedidoProdutoEntregaReportVO entity = null;
		
		if(pedidos!=null && !pedidos.isEmpty()) {
			for(Pedido pedido : pedidos) {
				entity = new PedidoProdutoEntregaReportVO();
				
				entity.setId(pedido.id);
				entity.setDataPedido(pedido.getDataPedido());
				entity.setNome(pedido.getCliente().getNome());
				entity.setLogradouro(pedido.getCliente().getEnderecos().get(0).getLogradouro());
				entity.setNumero(pedido.getCliente().getEnderecos().get(0).getNumero());
				entity.setComplemento(pedido.getCliente().getEnderecos().get(0).getComplemento());
				entity.setBairro(pedido.getCliente().getEnderecos().get(0).getBairro());
				entity.setCidade(pedido.getCliente().getEnderecos().get(0).getCidade());
				entity.setUf(pedido.getCliente().getEnderecos().get(0).getUf());
				entity.setCep(pedido.getCliente().getEnderecos().get(0).getCep());
				entity.setPrefixoTelefone(pedido.getCliente().getTelefones().get(0).getPrefixo());
				entity.setTelefone(pedido.getCliente().getTelefones().get(0).getNumero());
				entity.setObservacao(pedido.getObservacao());
				entity.setProdutos( ProdutoPedidoReportVO.fillProdutos(Relatorios.findProdutosAguardandoEntrega(pedido.id), 
																	pedido.getValorPedido(), 
																	pedido.getDesconto().getValorDesconto(),
																	pedido.getObservacao()));

				result.add(entity);
			}
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof PedidoProdutoEntregaReportVO))
			return false;
		final PedidoProdutoEntregaReportVO other = (PedidoProdutoEntregaReportVO) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	/**
	 * @return the prefixoTelefone
	 */
	public String getPrefixoTelefone() {
		return prefixoTelefone;
	}

	/**
	 * @param prefixoTelefone the prefixoTelefone to set
	 */
	public void setPrefixoTelefone(String prefixoTelefone) {
		this.prefixoTelefone = prefixoTelefone;
	}

	/**
	 * @return the telefone
	 */
	public String getTelefone() {
		return telefone;
	}

	/**
	 * @param telefone the telefone to set
	 */
	public void setTelefone(String telefone) {
		this.telefone = telefone;
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

}