/**
 * 
 */
package relatorios.parse;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import jxl.Workbook;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

import org.apache.commons.io.FileUtils;

import play.Logger;
import play.i18n.Messages;
import util.CompactadorZip;
import vo.ProdutoPedidoReportVO;

/**
 * @author Felipe Guerra
 * @version 1.0
 * @since 13/11/2013
 */
public class ProdutoFornecedorParse implements Serializable {

	private static final long serialVersionUID = 89786754632456871L;
	
	private List<ProdutoPedidoReportVO> produtosPorPedido = null;
	
	private StringBuffer tempfilePath = new StringBuffer(Messages.get("application.path.upload.archives", ""));
	private StringBuffer caminhoCompletoRelatorio = null;
	private WritableSheet sheet = null;
	private WritableWorkbook workbook = null;
	private File relatorioExcel = null;
	private File arquivosCompactados = null; 
	
	/**
	 * @param dados
	 * @param tempfilePath arquivo onde será gravado os arquivos
	 */
	public ProdutoFornecedorParse(List<ProdutoPedidoReportVO> dados, String tempfilePath) {
		this.produtosPorPedido = dados;
		this.tempfilePath = new StringBuffer(tempfilePath);
	}
	
	public File createReport() {
		String fornecedor = null;
		int coluna = 0;
		int linha = 1;
		
		try {
			caminhoCompletoRelatorio = new StringBuffer();
			this.tempfilePath.append("temp");
			
			arquivosCompactados = new File(this.tempfilePath.toString());
			
			if(arquivosCompactados.exists())
				FileUtils.deleteDirectory(arquivosCompactados);

			arquivosCompactados.mkdir();
			
			initInstances(this.produtosPorPedido.get(0));
			
			fornecedor = this.produtosPorPedido.get(0).getFornecedor();
			
			for(ProdutoPedidoReportVO vo : this.produtosPorPedido) {
				if(!fornecedor.equalsIgnoreCase(vo.getFornecedor())) {
					closeWorkbook();
					
					initInstances(vo);
					
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
			closeWorkbook();
			
			new CompactadorZip().criarZip(arquivosCompactados, arquivosCompactados.listFiles());
			
		}catch(Exception e) {
			Logger.error(e, "Ocorreu um erro na tentativa de gerar o Relatório de Produtos por Fornecedor no formato Excel.");
			throw new RuntimeException(e);
		}
		return new File(arquivosCompactados.getAbsolutePath()+".zip");
	}
	
	private void addLabelHeader() throws IOException {
		try {
			Label label = new Label(0, 0, "Código");
			this.sheet.addCell(label);
			
			Label label2 = new Label(1, 0, "Descrição");
			this.sheet.addCell(label2);
			
			Label label3 = new Label(2, 0, "Quantidade");
			this.sheet.addCell(label3);
			
		}catch(Exception e) {
			Logger.error(e, "Ocorreu um erro na tentativa de escrever o cabeçalho do Relatório de Produtos por Fornecedor no formato Excel.");
			throw new IOException(e);
		}
	}
	
	private void initInstances( 
								ProdutoPedidoReportVO vo
								) throws Exception {
		this.caminhoCompletoRelatorio = new StringBuffer();
		this.caminhoCompletoRelatorio.append(this.tempfilePath);
		this.caminhoCompletoRelatorio.append(File.separatorChar);
		this.caminhoCompletoRelatorio.append(vo.getFornecedor());
		this.caminhoCompletoRelatorio.append(".xls");
		
		this.relatorioExcel = new File(caminhoCompletoRelatorio.toString());
		
		this.relatorioExcel.createNewFile();
		
		this.workbook = Workbook.createWorkbook(this.relatorioExcel);
		
		this.sheet = workbook.createSheet(vo.getFornecedor(), 0);
		
		addLabelHeader();
	}
	
	private void closeWorkbook() throws Exception {
		workbook.write();
		workbook.close();
	}
	
}
