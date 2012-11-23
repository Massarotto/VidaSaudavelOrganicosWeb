/**
 * 
 */
package controllers;

import java.util.List;

import javax.persistence.Query;

import org.apache.commons.lang.StringUtils;

import business.cliente.service.EnderecoService;
import business.produto.ProdutoControl;

import models.Produto;
import play.Logger;
import play.i18n.Messages;
import play.libs.WS;
import play.modules.paginate.ValuePaginator;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Http.Header;

/**
 * @author guerrafe
 *
 */
public class Home extends BaseController {
	
	public static void index(String message) {
		Logger.debug("###### Início - Home Vida Saudável...%s #######", "");
		Boolean home = Boolean.TRUE;

		List<Produto> produtos = Produto.find("ativo = ? AND ehPromocao = ?", Boolean.TRUE, Boolean.TRUE).fetch(12);
		
		Logger.debug("###### Fim - Home Vida Saudável...%s #######", message);
		render(produtos, message, SecaoProdutos.loadAll(), home);
	}
	
	public static void search() {
		String param = params.get("search", String.class);
		
		Logger.debug("###### Início - Pesquisa de produto...%s #######", param);
		
		if(!StringUtils.isEmpty(param.trim())) {
			List<Produto> prods = new ProdutoControl().findProdutosByNomeOuDetalhe(param);
			
			Logger.debug("Foram encontrados %s produtos para o parâmetro: %s", prods.size(), param);
			
			Logger.debug("###### Fim - Pesquisa de produto...%s #######", param);
			
			ValuePaginator<Produto> produtos = new ValuePaginator<Produto>(prods);
			produtos.setPageSize(50);
			
			render(produtos);
			
		}else {
			index(Messages.get("validation.required", "texto"));
		}
	}
	
}
