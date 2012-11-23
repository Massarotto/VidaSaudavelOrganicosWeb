/**
 * 
 */
package business.estoque;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import exception.ProdutoEstoqueException;

import play.Logger;

import models.CarrinhoItem;
import models.PedidoItem;
import models.Produto;
import models.ProdutoEstoque;

/**
 * @author Felipe G. de Oliveira
 * @version 1.0
 * @since 28/05/2012
 */
public class EstoqueControl implements Serializable {
	
	public EstoqueControl() {
		super();
	}
	
	public static synchronized void atualizarEstoque(ProdutoEstoque produtoEstoque, Integer quantidade, String usuario) {
		if(produtoEstoque!=null) {
			Logger.info("######### Início - Atualizar Estoque [estoque id: %s; quantidade: %s] #########", produtoEstoque.id, quantidade);
			
			produtoEstoque.setDataAlteracao(new Date());
			produtoEstoque.setUsuarioAlteracao(usuario);
			produtoEstoque.save();

			Logger.info("######### Fim - Atualizar Estoque [estoque id: %s;] #########", produtoEstoque.id);
		}
	}

	public static synchronized void atualizarEstoque(ProdutoEstoque produtoEstoque) {
		if(produtoEstoque!=null) {
			Logger.info("######### Início - Atualizar Estoque [estoque id: %s;] #########", produtoEstoque.id);
			
			produtoEstoque.setDataAlteracao(new Date());
			
			produtoEstoque.save();

			Logger.info("######### Fim - Atualizar Estoque [estoque id: %s;] #########", produtoEstoque.id);
		}
	}
	
	public static synchronized ProdutoEstoque loadEstoque(Long id, Long idProduto) {
		if(id==null)
			return findByIdProduto(idProduto);
		else 
			if(idProduto==null)
				return load(id);
			else 
				return null;
	}
	
	private static synchronized ProdutoEstoque load(Long id) {
		return ProdutoEstoque.findById(id);
	}

	private static synchronized ProdutoEstoque findByIdProduto(Long id) {
		ProdutoEstoque result = ProdutoEstoque.find("produto.id = ?", id).first();
		
		return result;
	}
	
	/**
	 * @param idCarrinho
	 * @param itens
	 * @throws ProdutoEstoqueException
	 */
	public static synchronized void atualizarEstoque(List<CarrinhoItem> itens) throws ProdutoEstoqueException {
		Integer quantidade = 0;
		Produto produto = null;
		ProdutoEstoque estoque = null;
		
		if(itens!=null && !itens.isEmpty()) {
			Logger.debug("#### Atualizar estoque para produtos do carrinho. ####", "");
			for(CarrinhoItem item : itens) {
				quantidade = item.getQuantidade();
				produto = item.getProdutos().get(0);
				
				Logger.debug("#### Produto [%s] em estoque? [%s] ####", produto.getNome(), produto.getProdutoEstoque()!=null);
				
				if(produto.getProdutoEstoque()!=null) {
					estoque = loadEstoque(produto.getProdutoEstoque().id, null);
					
					if(estoque.getQuantidade()<quantidade) {
						StringBuilder exception = new StringBuilder();
						exception.append("O estoque do produto '").append(estoque.getProduto().getNome());
						exception.append("' - Quantidade em estoque: ").append(estoque.getQuantidade());
						exception.append(", não é suficiente para atender ao pedido");
						
						throw new ProdutoEstoqueException(exception.toString());
					}
					
					estoque.setQuantidade(estoque.getQuantidade()-quantidade);
					atualizarEstoque(estoque);
				}
			}
			Logger.debug("#### Fim atualizar estoque para produtos do carrinho. ####", "");
		}
	}
	
	/**
	 * Através de uma lista de ítem de pedidos, atualiza a posição do estoque adicionando a quantidade de cada pedido ao estoque já existente.
	 * @param itens
	 * @param usuarioAlteracao
	 */
	public static synchronized void reporEstoque(List<PedidoItem> itens, String usuarioAlteracao) {
		Logger.debug("#### Atualizar o estoque...usuário: %s####", usuarioAlteracao);
		Produto produto = null;
		ProdutoEstoque estoque = null;
		Integer quantidade = null;
		
		if(itens!=null && !itens.isEmpty()) {
			for(PedidoItem item : itens) {
				quantidade = 0;
				
				produto = item.getProdutos().get(0);
				estoque = loadEstoque(null, produto.id);
				
				if(estoque!=null) {
					quantidade = estoque.getQuantidade() + item.getQuantidade();
					
					estoque.setQuantidade(quantidade);
					estoque.setDataAlteracao(new Date());
					estoque.setUsuarioAlteracao(usuarioAlteracao);
					
					estoque.save();
					Logger.info("#### Estoque para o produto %s atualizado. Qtde atualizada: %s ####", produto.getNome(), quantidade);
				}
			}
		}
		Logger.debug("#### Fim atualizar o estoque...usuário: %s####", usuarioAlteracao);
	}
	
	/**
	 * Consulta todos os produtos que se encontram com uma quantidade menor ou igual a especificada. 
	 * @param qtdMinimaEstoque
	 * @return lista dos produtos em estoque
	 */
	public static synchronized List<ProdutoEstoque> findProdutoEstoque(Integer qtdMinimaEstoque) {
		Logger.debug("### Início - Consultar estoque com quantidade mínima de %s. ###", qtdMinimaEstoque);
		List<ProdutoEstoque> result = null;
		
		result = ProdutoEstoque.find("quantidade <= ? AND enviarRelatorio = ? order by produto.nome ASC", qtdMinimaEstoque, Boolean.TRUE).fetch();
		
		Logger.debug("### Fim - Consultar estoque com quantidade mínima de %s. Resultado: %s ###", qtdMinimaEstoque, result.size());
		
		return result;
	}
	
}
