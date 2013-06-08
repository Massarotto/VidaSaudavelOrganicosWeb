/**
 * 
 */
package relatorios.parse;

import java.io.Serializable;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import models.ProdutoEstoque;
import play.Logger;

/**
 * @author hpadmin
 *
 */
public class EstoqueParse implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1089768574651L;

	public static String buildHtmlLayout(List<ProdutoEstoque> estoque) {
		StringBuffer build = new StringBuffer();
		NumberFormat numberFormat = null;
		
		try {
			if(estoque!=null && !estoque.isEmpty()) {
				numberFormat = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
				
				for(ProdutoEstoque produtoEstoque : estoque) {
					build.append("<tr>");
					build.append("<td align='center'>");
					build.append(produtoEstoque.getProduto().getNome());
					build.append("</td>");
					build.append("<td align='center'>");
					build.append(produtoEstoque.getQuantidade());
					build.append("</td>");
					build.append("<td align='center'>");
					build.append(numberFormat.format(produtoEstoque.getPreco()));
					build.append("</td>");
					build.append("<td align='center'>");
					build.append(produtoEstoque.getProduto().getDataValidade()==null ? " - " : produtoEstoque.getProduto().getDataValidade());
					build.append("</td>");
					build.append("</tr>");
				}
			}
			
		}catch(Exception e) {
			Logger.error(e, "Erro ao tentar fazer o parse dos produtos em Estoque no formato HTML.", "");
			throw new RuntimeException(e);
		}
		return build.toString();
	}

}
