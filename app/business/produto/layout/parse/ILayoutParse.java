/**
 * 
 */
package business.produto.layout.parse;

import java.io.File;
import java.io.Serializable;
import java.util.List;

import business.produto.layout.LayoutArquivo;

/**
 * @author hpadmin
 *
 */
public interface ILayoutParse<T> extends Serializable {

	List<T> parse(Long idFornecedor, File archive);
}
