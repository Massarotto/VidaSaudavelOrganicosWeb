/**
 * 
 */
package util;

import java.io.Serializable;
import java.util.Comparator;

import models.CestaProduto;

/**
 * @author guerrafe
 *
 */
public class CestaProdutoComparator implements Comparator<CestaProduto>, Serializable {

	private static final long serialVersionUID = 3458978833231917729L;
	
	public CestaProdutoComparator() { 
	}

	@Override
	public int compare(CestaProduto o1, CestaProduto o2) {
		if(o1==null || o2==null)
			return 0;
		
		return o1.getOpcional().compareTo(o2.getOpcional());
	}
}
