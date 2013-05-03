/**
 * 
 */
package util;

import java.io.Serializable;
import java.util.Comparator;

import models.CestaAssinaturaProduto;

/**
 * @author guerrafe
 *
 */
public class CestaAssinaturaProdutoComparator implements Comparator<CestaAssinaturaProduto>, Serializable {
	
	private Boolean opcao = Boolean.FALSE;
	
	public CestaAssinaturaProdutoComparator(Boolean opcao) {
		this.opcao = opcao;
	}

	private static final long serialVersionUID = 3183244156533407397L;

	@Override
	public int compare(CestaAssinaturaProduto arg0, CestaAssinaturaProduto arg1) {
		if(arg0==null || arg1==null)
			return 0;
		
		if(!opcao)
			return arg0.getProduto().getSecao().getDescricao().compareTo(arg1.getProduto().getSecao().getDescricao());
		else
			return arg0.getOpcional().compareTo(arg1.getOpcional());
	}

}
