/**
 * 
 */
package relatorios.parse;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.util.List;

import play.Logger;

import models.Produto;

/**
 * @author guerrafe
 *
 */
public class ProdutoReportParse implements Serializable {
	
	private List<Produto> produtos;
	
	public ProdutoReportParse(List<Produto> produtos) {
		this.produtos = produtos;
	}

	public InputStream parse() {
		InputStream result = null;
		StringBuilder line = new StringBuilder();
		
		try {
			if(produtos!=null && !produtos.isEmpty()) {
				for(Produto produto : produtos) {
					line.append(produto.id).append(";");
					line.append(produto.getNome()).append(";");
					line.append(produto.getDescricao()).append(";");
					line.append(produto.getFornecedor().getNome()).append(";");
					line.append(produto.getSecao()==null ? "" : produto.getSecao().getDescricao()).append(";");
					line.append(produto.getCodigoProduto()).append(";");
					line.append(produto.getAtivo() ? "ATIVO" : "INATIVO").append(";");
					line.append(produto.getValorPago()).append(";");
					line.append(produto.getValorVenda()).append(";");
					line.append(produto.getDataCadastro()).append(";");
					line.append((produto.getProdutoEstoque()!=null && Boolean.TRUE.equals(produto.getProdutoEstoque().getAtivo())) 
								? "Quantidade em estoque: " + String.valueOf(produto.getProdutoEstoque().getQuantidade()) 
								: " - ").append(";");
					line.append("\r\n");
					
				}
				result = new ByteArrayInputStream(line.toString().getBytes("ISO-8859-1"));
			}
			
		}catch(Exception e) {
			Logger.error(e, "Ocorreu um erro na tentativa de gerar o Relatório de Produtos no formato CSV.");
			throw new RuntimeException(e);
		}
		return result;
	}
	
}
