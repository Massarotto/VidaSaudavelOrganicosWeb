/**
 * 
 */
package relatorios.parse;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.collections.map.HashedMap;

import jxl.Workbook;
import jxl.format.BoldStyle;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.format.Colour;
import jxl.write.DateFormat;
import jxl.write.DateTime;
import jxl.write.Label;
import jxl.write.WritableCell;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import models.Endereco;
import models.FormaPagamento;
import models.Pedido;
import play.Logger;

/**
 * <p>
 * 	Classe que representa a transformação do Modelo para o formato Excel do módulo de Relatórios
 * </p>
 * @author Felipe Guerra
 * @version 1.0
 * @since 21/02/2014
 */
public class PedidoReportParse implements Serializable {
	
	private static final int HEADER_WORKSHEET 		= 1;
	
	private static final int CONTENT_WORKSHEET 		= 2;

	private static final long serialVersionUID = -79500988005551L;

	private List<Pedido> pedidos = null;
	private WritableSheet sheet = null;
	private WritableWorkbook workbook = null;
	private BigDecimal totalPedidos = BigDecimal.ZERO;
	private int linha = 0;
	private NumberFormat numberFormat = null;
	@SuppressWarnings("unchecked")
	private Map<Integer, BigDecimal> formaPagamentoValor = new HashedMap();
	
	public PedidoReportParse(List<Pedido> pedidos) {
		if(pedidos==null)
			throw new IllegalStateException("A lista de Pedidos é um argumento obrigatório");
		
		this.pedidos = pedidos;
	}

	public List<Pedido> getPedidos() {
		return pedidos;
	}
	
	public File createReport(String reportName) {
		File report = null;
		int coluna = 0;
		StringBuffer endereco = null;
		
		try {
			report = new File(new StringBuffer().append(System.getProperty("java.io.tmpdir")).append(File.separatorChar).append(reportName).toString());
			
			this.workbook = Workbook.createWorkbook(report);
			
			this.sheet = workbook.createSheet(reportName, 0);

			numberFormat = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
			
			setPedidosFechamentoHeader();
			addLabelHeader();
			for(Pedido pedido : this.pedidos) {
				linha++;
				Label id = new Label(coluna, linha, String.valueOf(pedido.id), getWritableCellFormat(CONTENT_WORKSHEET));
				sheet.addCell(id);
				coluna++;
				
				Label cliente = new Label(coluna, linha, pedido.getCliente().getNome(), getWritableCellFormat(CONTENT_WORKSHEET));
				sheet.addCell(cliente);
				coluna++;
				
				Endereco ender = pedido.getCliente().getEnderecos().get(0);
				endereco = new StringBuffer();
				Label enderecoCliente = new Label(coluna, linha, endereco.append(ender.getLogradouro()).append(", ")
																		.append(ender.getNumero()).append(" - ")
																		.append(ender.getComplemento()).append(" - ")
																		.append("CEP: ").append(ender.getCepFormatado()).toString()
																		, getWritableCellFormat(CONTENT_WORKSHEET));
				sheet.addCell(enderecoCliente);
				coluna++;
				
				Label bairro = new Label(coluna, linha, ender.getBairro(), getWritableCellFormat(CONTENT_WORKSHEET));
				sheet.addCell(bairro);
				coluna++;
				
				Label cidade = new Label(coluna, linha, ender.getCidade(), getWritableCellFormat(CONTENT_WORKSHEET));
				sheet.addCell(cidade);
				coluna++;
				
				Label valorTotal = new Label(coluna, linha, numberFormat.format(pedido.getValorTotal()), getWritableCellFormat(CONTENT_WORKSHEET));
				sheet.addCell(valorTotal);
				totalPedidos = totalPedidos.add(pedido.getValorTotal());
				setValorPorFormaPagamento(pedido.getPagamento().getFormaPagamento().getCodigo(), pedido.getValorTotal());
				coluna++;
				
				Label valorReal = new Label(coluna, linha, "", getWritableCellFormat(CONTENT_WORKSHEET));
				sheet.addCell(valorReal);
				coluna++;
				
				Label valorPendente = new Label(coluna, linha, "", getWritableCellFormat(CONTENT_WORKSHEET));
				sheet.addCell(valorPendente);
				coluna++;
				
				Label valorPago = new Label(coluna, linha, pedido.getValorPago()==null ? "" : numberFormat.format(pedido.getValorPago()), getWritableCellFormat(CONTENT_WORKSHEET));
				sheet.addCell(valorPago);
				coluna++;
				
				Label foiPago = new Label(coluna, linha, "", getWritableCellFormat(CONTENT_WORKSHEET));
				sheet.addCell(foiPago);
				coluna++;
				
				Label formaPagamento = new Label(coluna, linha, pedido.getPagamento().getFormaPagamento().getDescricao(), getWritableCellFormat(CONTENT_WORKSHEET));
				sheet.addCell(formaPagamento);
				coluna++;
				
				Label saldo = new Label(coluna, linha, "", getWritableCellFormat(CONTENT_WORKSHEET));
				sheet.addCell(saldo);
				coluna++;
				
				coluna = 0;
			}
			setValorTotalPorFormaPagamento();
			setValorTotalPedidos();
			
			closeWorkbook();
			
		}catch(Exception e) {
			Logger.error(e, "Ocorreu um erro no Relatório de Fechamento de Pedidos.");
			throw new RuntimeException(e);
		}
		return report;
	}

	private void addLabelHeader() throws IOException {
		int cont = 0;
		this.linha++;
		
		try {
			Label label = new Label(cont++, this.linha, "Pedido", getWritableCellFormat(HEADER_WORKSHEET));
			
			this.sheet.addCell(label);
			
			Label label1 = new Label(cont++, this.linha, "Cliente", getWritableCellFormat(HEADER_WORKSHEET));
			
			this.sheet.addCell(label1);
			
			Label label8 = new Label(cont++, this.linha, "Endereço", getWritableCellFormat(HEADER_WORKSHEET));
			this.sheet.addCell(label8);
			
			Label bairro = new Label(cont++, this.linha, "Bairro", getWritableCellFormat(HEADER_WORKSHEET));
			this.sheet.addCell(bairro);
			
			Label cidade = new Label(cont++, this.linha, "Cidade", getWritableCellFormat(HEADER_WORKSHEET));
			this.sheet.addCell(cidade);
			
			Label label2 = new Label(cont++, this.linha, "Valor Total", getWritableCellFormat(HEADER_WORKSHEET));
			
			this.sheet.addCell(label2);
			
			Label label3 = new Label(cont++, this.linha, "Valor Real", getWritableCellFormat(HEADER_WORKSHEET));
			
			this.sheet.addCell(label3);
			
			Label label4 = new Label(cont++, this.linha, "Valor Pendente", getWritableCellFormat(HEADER_WORKSHEET));
			
			this.sheet.addCell(label4);
			
			Label label5 = new Label(cont++, this.linha, "Total Pago", getWritableCellFormat(HEADER_WORKSHEET));
			
			this.sheet.addCell(label5);
			
			Label label9 = new Label(cont++, this.linha, "Pago", getWritableCellFormat(HEADER_WORKSHEET));
			
			this.sheet.addCell(label9);
			
			Label label6 = new Label(cont++, this.linha, "Forma Pagamento", getWritableCellFormat(HEADER_WORKSHEET));
			
			this.sheet.addCell(label6);
			
			Label label7 = new Label(cont++, this.linha, "Saldo", getWritableCellFormat(HEADER_WORKSHEET));
			
			this.sheet.addCell(label7);
			
		}catch(Exception e) {
			Logger.error(e, "Ocorreu um erro na tentativa de escrever o cabeçalho do Relatório.");
			throw new IOException(e);
		}
	}
	
	private void closeWorkbook() throws Exception {
		workbook.write();
		workbook.close();
	}
	
	private WritableCellFormat getWritableCellFormat(int worksheet) throws WriteException {
		WritableCellFormat wcf = null;
		WritableFont font = null;
		
		switch(worksheet) {
			case CONTENT_WORKSHEET:
				font = new WritableFont(WritableFont.TIMES, 9);
				font.setColour(Colour.GRAY_80);
				wcf = new WritableCellFormat(font);
				wcf.setBorder(Border.ALL, BorderLineStyle.THIN, Colour.BLACK);
				break;
			
			case HEADER_WORKSHEET:
				font = new WritableFont(WritableFont.COURIER, 10);
				font.setColour(Colour.BROWN);
				font.setBoldStyle(WritableFont.BOLD);
				wcf = new WritableCellFormat(font);
				wcf.setBorder(Border.ALL, BorderLineStyle.MEDIUM, Colour.BLACK);
				wcf.setBackground(Colour.YELLOW);
				break;
				
			default:
				break;
		}
			
		return wcf;
	}
	
	private void setPedidosFechamentoHeader() throws Exception {
		int cont = 0;
		
		Label label = new Label(cont++, this.linha, "Data", getWritableCellFormat(HEADER_WORKSHEET));
		this.sheet.addCell(label);
		WritableCell cell = new DateTime(cont++, this.linha, new Date());
		WritableCellFormat cellFormat = new WritableCellFormat(new DateFormat("dd/MM/yyyy"));
		cell.setCellFormat(cellFormat);
		
		this.sheet.addCell(cell);
		
		cont = 0;
		this.linha++;
		Label caixa = new Label(cont++, this.linha, "Caixa Inicial", getWritableCellFormat(HEADER_WORKSHEET));
		this.sheet.addCell(caixa);
		
		this.linha++;
	}
	
	private void setValorTotalPedidos() throws Exception {
		int cont = 0;
		this.linha++;
		
		Label label = new Label(cont++, this.linha, "Valor Total", getWritableCellFormat(CONTENT_WORKSHEET));
		this.sheet.addCell(label);
		
		Label valor = new Label(cont++,this.linha, numberFormat.format(this.totalPedidos), getWritableCellFormat(CONTENT_WORKSHEET));
		this.sheet.addCell(valor);
	}
	
	private void setValorTotalPorFormaPagamento() throws Exception {
		Iterator<Integer> formasPagamento = this.formaPagamentoValor.keySet().iterator();
		this.linha++;
		
		while(formasPagamento.hasNext()) {
			Integer idFormaPagamento = formasPagamento.next();
			
			int cont = 0;
			this.linha++;
			
			Label label = new Label(cont++, this.linha, FormaPagamento.getFormaPagamento(idFormaPagamento).getDescricao(), getWritableCellFormat(CONTENT_WORKSHEET));
			this.sheet.addCell(label);
			
			Label valor = new Label(cont++,this.linha, numberFormat.format(this.formaPagamentoValor.get(idFormaPagamento)), getWritableCellFormat(CONTENT_WORKSHEET));
			this.sheet.addCell(valor);
		}
	}
	
	private void setValorPorFormaPagamento(Integer idFormaPagamento, BigDecimal valor) {
		if(!this.formaPagamentoValor.containsKey(idFormaPagamento)) {
			this.formaPagamentoValor.put(idFormaPagamento, valor);
			
		}else {
			BigDecimal valorAtualAux = this.formaPagamentoValor.get(idFormaPagamento);
			
			if(valorAtualAux==null)
				valorAtualAux = BigDecimal.ZERO;
			
			valorAtualAux =  valorAtualAux.add(valor);
			
			this.formaPagamentoValor.put(idFormaPagamento, valorAtualAux);
		}
	}	
}