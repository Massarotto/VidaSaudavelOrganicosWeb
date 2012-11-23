package relatorios;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.List;
import java.util.Map;

import models.Pedido;
import net.sf.jasperreports.engine.JRExporter;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.JExcelApiExporter;
import net.sf.jasperreports.view.JasperViewer;
import play.Logger;
import vo.PedidoProdutoEntregaReportVO;

public class BaseJasperReport {
	
	public static InputStream generateExcelReport(String pathJasperReport, String reportDefFile, Map<?, ?> reportParams, List<PedidoProdutoEntregaReportVO> pedidos) {
		OutputStream stream = new ByteArrayOutputStream();
		
		try {
			JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(pedidos);
			
			JasperPrint print = JasperFillManager.fillReport(pathJasperReport, reportParams, dataSource);
			
			JRExporter exporter = new JExcelApiExporter();
			
			exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, stream);
			exporter.setParameter(JRExporterParameter.JASPER_PRINT, print);
			
			exporter.exportReport();

		}catch(Exception e) {
			Logger.error(e, "Erro ao tentar gerar o relatório.");
			throw new RuntimeException(e);
		}
		return new ByteArrayInputStream(((ByteArrayOutputStream) stream).toByteArray());
	}

	public static InputStream generatePdfReport(String pathJasperReport, String reportDefFile, Map<?, ?> reportParams, List<PedidoProdutoEntregaReportVO> pedidos) {
		OutputStream stream = new ByteArrayOutputStream();
		
		try {
			JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(pedidos);
			
			JasperPrint print = JasperFillManager.fillReport(pathJasperReport, reportParams, dataSource);
			
			byte[] bytes = JasperExportManager.exportReportToPdf(print);
			
			stream.write(bytes, 0, bytes.length);

		}catch(Exception e) {
			Logger.error(e, "Erro ao tentar gerar o relatório.");
			throw new RuntimeException(e);
		}
		return new ByteArrayInputStream(((ByteArrayOutputStream) stream).toByteArray());
	}
	
}
