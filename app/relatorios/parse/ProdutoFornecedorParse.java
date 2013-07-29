/**
 * 
 */
package relatorios.parse;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.List;

import jxl.Workbook;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import play.Logger;
import vo.ProdutoPedidoReportVO;

/**
 * @author guerrafe
 *
 */
public class ProdutoFornecedorParse implements Serializable {

	private static final long serialVersionUID = 89786754632456871L;
	
	private List<ProdutoPedidoReportVO> produtosPorPedido = null;
	
	public ProdutoFornecedorParse(List<ProdutoPedidoReportVO> dados) {
		produtosPorPedido = dados;
	}

	private WritableWorkbook createWorkbook(File nomeArquivo) throws IOException {
		WritableWorkbook result = null;
		
		result = Workbook.createWorkbook(nomeArquivo);
		
		return result;
	}
	
	public File createReport(String nomeArquivo) {
		WritableSheet sheet = null;
		String fornecedor = null;
		int coluna = 0;
		int linha = 1;
		File relatorioExcel = null;
		
		try {
			relatorioExcel =  new File(nomeArquivo);
			
			WritableWorkbook workbook = createWorkbook(relatorioExcel);
			
			sheet = createWritableSheet(this.produtosPorPedido.get(0).getFornecedor(), workbook);
			addLabelHeader(sheet);
			fornecedor = this.produtosPorPedido.get(0).getFornecedor();
			
			for(ProdutoPedidoReportVO vo : this.produtosPorPedido) {
				if(!fornecedor.equalsIgnoreCase(vo.getFornecedor())) {
					sheet = createWritableSheet(vo.getFornecedor(), workbook);
					addLabelHeader(sheet);
					linha = 1;
				}
				Label codigo = new Label(coluna, linha, vo.getCodigoProduto());
				sheet.addCell(codigo);
				coluna++;
				
				Label descricao = new Label(coluna, linha, vo.getDescricao());
				sheet.addCell(descricao);
				coluna++;
				
				Number quantidade = new Number(coluna, linha, vo.getQuantidade());
				sheet.addCell(quantidade);
				
				coluna = 0;
				linha++;
				fornecedor = vo.getFornecedor();
			}
			
			workbook.write();
			workbook.close();
			
		}catch(Exception e) {
			Logger.error(e, "Ocorreu um erro na tentativa de gerar o Relatório de Produtos por Fornecedor no formato Excel.");
			throw new RuntimeException(e);
		}
		return relatorioExcel;
	}
	
	private WritableSheet createWritableSheet(String nomeFornecedor, WritableWorkbook workbook) {
		WritableSheet sheet = workbook.createSheet(nomeFornecedor, 0);
		
		return sheet;
	}
	
	private void addLabelHeader(WritableSheet sheet) throws IOException {
		try {
			Label label = new Label(0, 0, "Código");
			sheet.addCell(label);
			
			Label label2 = new Label(1, 0, "Descrição");
			sheet.addCell(label2);
			
			Label label3 = new Label(2, 0, "Quantidade");
			sheet.addCell(label3);
			
		}catch(Exception e) {
			Logger.error(e, "Ocorreu um erro na tentativa de escrever o cabeçalho do Relatório de Produtos por Fornecedor no formato Excel.");
			throw new IOException(e);
		}
	}
	
}
