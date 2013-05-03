/**
 * 
 */
package util;

import java.lang.reflect.Type;

import play.i18n.Messages;

import models.CestaProduto;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * @author guerrafe
 *
 */
public class CestaProdutoSerializer implements JsonSerializer<CestaProduto> {

	@Override
	public JsonElement serialize(CestaProduto arg0, Type arg1, JsonSerializationContext arg2) {
		JsonObject root = new JsonObject();
		StringBuffer imagem = new StringBuffer();
		String opcao = null;
		
		imagem.append("<img ");
		imagem.append("alt='").append(arg0.getProduto().getImagemProduto()).append("' ");
		imagem.append("src='"); 
		imagem.append(Messages.get("application.static.content", ""));
		imagem.append(Messages.get("application.path.public.images", ""));
		imagem.append(arg0.getProduto().getImagemProduto());
		imagem.append("' class='imgBoxMain' />"); 
		
		root.addProperty("secao", arg0.getProduto().getSecao().getDescricao());
		root.addProperty("imagem", imagem.toString());
		
		if(arg0.getOpcional())
			opcao = Messages.get("form.cesta.second", "");
		else
			opcao = Messages.get("form.cesta.principal", "");
		
		root.addProperty("opcional", opcao);
		root.addProperty("produto", arg0.getProduto().getNome());
		root.addProperty("valor", arg0.getProduto().getValorVenda());
		root.addProperty("id", arg0.id);
		root.addProperty("codigo", arg0.getProduto().id);
		root.addProperty("informacoes", arg0.getProduto().getDetalhe());
		root.addProperty("quantidade", Integer.valueOf(1));
		
		return root;
	}

}
