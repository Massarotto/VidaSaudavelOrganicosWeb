package controllers;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import models.Fornecedor;
import models.Pedido;
import models.PedidoItem;
import models.Produto;
import play.Logger;
import play.data.binding.As;
import play.db.jpa.JPA;
import play.i18n.Messages;
import play.modules.paginate.ValuePaginator;
import play.mvc.Before;
import relatorios.BaseJasperReport;
import relatorios.parse.ProdutoPedidoReportParse;
import relatorios.parse.ProdutoReportParse;
import util.PedidoFornecedorComparator;
import vo.PedidoProdutoEntregaReportVO;
import vo.ProdutoPedidoReportVO;

public class Relatorios extends BaseController {
	
	private static final String RELATORIO_PEDIDOS_ABERTO_VENDAS		=	"PedidoClienteEntrega.jasper";
	private static final String REPORT_TITLE						=	"RELATÓRIO DE PEDIDO";
	private static final String SUBREPORT_DIR						= 	Messages.get("application.path.report", "");
	public static final String RELATORIO_PRODUTO_FORNECEDOR			=	"RELATORIO_PRODUTOS_FORNECEDOR";
	public static final String RELATORIO_PRODUTO_ESTOQUE			=	"RELATORIO_PRODUTOS_ESTOQUE_ENTREGA";
	public static final String RELATORIO_PRODUTOS					=	"RELATORIO_PRODUTOS_ATIVOS";
	
	@Before
	static void estaAutorizado() {
		Logger.debug("####### Verificar se o usuário autenticado é admin... ########");
		
		if(!session.contains("isAdmin") || Boolean.valueOf(session.get("isAdmin"))==Boolean.FALSE) {
			Logger.debug("####### Usuário não autorizado a acessar essa funcionalidade...%s ########", session.get("usuarioAutenticado"));
			
			Home.index("Usuário não autorizado a acessar essa funcionalidade.");
		}
	}
	
	public static void index() {
		render();
	}
	
	public static void relatorioProdutosCadastrados() {
		List<Produto> produtos = Produto.find("order by fornecedor.nome, ativo ASC", null).fetch();
		ProdutoReportParse parser = new ProdutoReportParse(produtos);
		
		renderBinary(parser.parse(), RELATORIO_PRODUTOS+".csv");
	}
	
	public static void relatorioProdutoPedidoCliente() {
		Logger.debug("##### Início - Gerar Relatório de Pedidos Abertos não entregues...######");
		
		List<Pedido> result = findPedidosAguardandoEntrega(null, null);
		
		ValuePaginator<Pedido> vPedidos = new ValuePaginator<Pedido>(result);
		vPedidos.setPageSize(30);
		
		BigDecimal valorTotalPedidos = Pedido.calcularValorTotalPedidos(result);
		
		Logger.debug("##### Fim - Gerar Relatório de Pedidos Abertos não entregues...######");
		render(vPedidos, valorTotalPedidos);
	}
	
	public static void relatorioProdutoFornecedor() {
		Logger.debug("##### Início - Gerar Relatório de Produtos por Fornecedor...######");
		
		List<ProdutoPedidoReportVO> result = findProdutosPorFornecedor(null, null);
		
		Collections.sort(result, new PedidoFornecedorComparator());
		
		ValuePaginator<ProdutoPedidoReportVO> vProdutos = new ValuePaginator<ProdutoPedidoReportVO>(result);
		
		vProdutos.setPageSize(20);
		
		Logger.debug("##### Fim - Gerar Relatório de Produtos por Fornecedor...]######");
		render(vProdutos);
	}
	
	public static void exportarRelatorioProdutoFornecedorCSV() {
		renderBinary(generateRelatorioProdutoFornecedorCSV(), RELATORIO_PRODUTO_FORNECEDOR+".csv");
	}
	
	public static InputStream generateRelatorioProdutoFornecedorCSV() {
		List<ProdutoPedidoReportVO> result = null;
		
		List<Produto> produtos = findProdutosAguardandoEntrega(null);
		
		result = ProdutoPedidoReportVO.fillProdutos(produtos);
		
		Collections.sort(result, new PedidoFornecedorComparator());

		ProdutoPedidoReportParse parse = new ProdutoPedidoReportParse(result);
		
		return parse.generateProdutoPedidoReport();
	}
	
	public static InputStream generateRelatorioProdutoEstoqueFornecedorCSV() {
		List<ProdutoPedidoReportVO> result = getPedidosEstoqueAguardandoEntrega();
		
		Collections.sort(result, new PedidoFornecedorComparator());

		ProdutoPedidoReportParse parse = new ProdutoPedidoReportParse(result);
		
		return parse.generateProdutoPedidoReport();
	}
	
	public static void exportarRelatorioView() {
		try {
			renderBinary( exportarRelatorioPdf(), "PedidoProdutosReport.pdf");
			
		}catch(Exception e) {
			Logger.error(e, "Erro ao tentar exportar o relatório.");
			throw new RuntimeException(e);
		}
	}
	
	public static InputStream exportarRelatorioPdf() {
		Map parametros = new HashMap();
		try {
			StringBuilder pathStaticContent = new StringBuilder(Messages.get("application.path.report", ""));
			pathStaticContent.append(RELATORIO_PEDIDOS_ABERTO_VENDAS);
		
			parametros.put("REPORT_TITLE", REPORT_TITLE);
			parametros.put("SUBREPORT_DIR", SUBREPORT_DIR);
			
			List<PedidoProdutoEntregaReportVO> dados = PedidoProdutoEntregaReportVO.fillListReport(findPedidosAguardandoEntrega(null, null));
			
			return BaseJasperReport.generatePdfReport(pathStaticContent.toString(), "PedidoClienteEntrega", parametros, dados);
			
		}catch(Exception e) {
			Logger.error(e, "Erro ao tentar exportar o relatório.");
			throw new RuntimeException(e);
		}
	}
	
	public static void exportarRelatorioMSExcel() {
		Map parametros = new HashMap();
		
		try {
			StringBuilder pathStaticContent = new StringBuilder(Messages.get("application.path.report", ""));
			pathStaticContent.append(RELATORIO_PEDIDOS_ABERTO_VENDAS);
		
			parametros.put("REPORT_TITLE", REPORT_TITLE);
			parametros.put("SUBREPORT_DIR", SUBREPORT_DIR);
			
			List<PedidoProdutoEntregaReportVO> dados = PedidoProdutoEntregaReportVO.fillListReport(findPedidosAguardandoEntrega(null, null));
			
			renderBinary( BaseJasperReport.generateExcelReport(pathStaticContent.toString(), "PedidoClienteEntrega", parametros, dados), "PedidoProdutosReport.xls" );
			
		}catch(Exception e) {
			Logger.error(e, "Erro ao tentar exportar o relatório em modo MS Excel.");
			throw new RuntimeException(e);
		}
	}
	
	private static List<ProdutoPedidoReportVO> findProdutosPorFornecedor(Date inicio, Date fim) {
		List<ProdutoPedidoReportVO> result = new ArrayList<ProdutoPedidoReportVO>();
		
		List<Pedido> pedidos = findPedidosAguardandoEntrega(inicio, fim);
		
		if(pedidos!=null && !pedidos.isEmpty()) {
			for(Pedido pedido : pedidos) {
				for(PedidoItem item : pedido.getItens()) {				
					ProdutoPedidoReportVO _tempVO = ProdutoPedidoReportVO.findProdutoPedidoReportByIdProduto(result, item.getProdutos());
					
					if(_tempVO==null)
						result.addAll(ProdutoPedidoReportVO.fillProdutos(item, null));
					else
						_tempVO.addQuantidade(item.getQuantidade());
				}
			}
		}
		return result;
	}
	
	/**
	 * <p>JPA 2.0</p>
	 * @param ehSantosOrganicos
	 * @return produtos
	 */
	public static List<Produto> findProdutosAguardandoEntrega(Long idPedido) {
		Predicate pedido = null;
		
		try {
			CriteriaBuilder builder = JPA.em().getCriteriaBuilder();
			
			CriteriaQuery<Produto> query = builder.createQuery(Produto.class);
			
			Root<Produto> root = query.from(Produto.class);
			
			Join<Produto, PedidoItem> joinItems = root.join("listPedidoItem");
			
			joinItems.alias("pedidoProduto");
			
			Join<PedidoItem, Pedido> joinPedidos = joinItems.join("pedido");
			
			Join<Produto, Fornecedor> joinFornec = root.join("fornecedor");
			
			Predicate status = builder.equal(joinPedidos.get("codigoEstadoPedido"), Pedido.PedidoEstado.AGUARDANDO_ENTREGA);
			
			query.where(status).orderBy(builder.asc(joinFornec.get("id")));
			
			if(idPedido!=null) {
				pedido = builder.equal(joinPedidos.get("id"), idPedido);
				query.where(pedido);
			}
			
			TypedQuery<Produto> queryProdutos = JPA.em().createQuery(query);
			
			List<Produto> produtos = queryProdutos.getResultList();
			
			return produtos;
			
		}catch(Exception e) {
			Logger.error(e, "Erro ao tentar consultar os produtos para o relatório.", "");
			throw new RuntimeException(e);
		}
	}
	
	public static List<ProdutoPedidoReportVO> getPedidosEstoqueAguardandoEntrega() {
		Logger.info("#### Início - Produtos em Estoque para Entrega ####", "");
		List<ProdutoPedidoReportVO> result = new ArrayList<ProdutoPedidoReportVO>();
		
		List<Pedido> pedidos = findPedidosAguardandoEntrega();
		
		if(pedidos!=null && !pedidos.isEmpty()) {
			for(Pedido pedido : pedidos) {
				for(PedidoItem item : pedido.getItens()) {		
					ProdutoPedidoReportVO _tempVO = ProdutoPedidoReportVO.findProdutoPedidoReportByIdProduto(result, item.getProdutos());
					
					if(_tempVO==null)
						result.addAll(ProdutoPedidoReportVO.fillProdutos(item, null, Boolean.TRUE));
					else
						_tempVO.addQuantidade(item.getQuantidade());
				}
			}
		}
		Logger.info("#### Fim - Produtos em Estoque para Entrega ####", "");
		return result;
	}

	private static List<Pedido> findPedidosAguardandoEntrega() {
		Query query = JPA.em().createQuery("select ped from Pedido ped JOIN ped.itens it JOIN it.produtos prods JOIN prods.produtoEstoque estoque " +
											"where ped.codigoEstadoPedido =:codigoEstadoPedido AND estoque.ativo =:estoqueAtivo");
		
		query.setParameter("codigoEstadoPedido", Pedido.PedidoEstado.AGUARDANDO_ENTREGA);
		query.setParameter("estoqueAtivo", Boolean.TRUE);
		
		List<Pedido> pedidosAbertos = query.getResultList();
		
		return pedidosAbertos;
	}
	
	private static List<Pedido> findPedidosAguardandoEntrega(Date inicio, Date fim) {
		StringBuilder query = new StringBuilder("codigoEstadoPedido = ? AND dataEntrega IS NULL");
		
		List<Object> params = new ArrayList<Object>();
		
		params.add(Pedido.PedidoEstado.AGUARDANDO_ENTREGA);
		
		if(inicio!=null) {
			query.append(" ").append("AND dataPedido >= ?");
			params.add(inicio);
		}
		
		if(fim!=null) {
			query.append(" ").append("AND dataPedido <= ?");
			params.add(fim);
		}
		
		Object[] paramsSize = new Object[params.size()];
		
		List<Pedido> pedidosAbertos = Pedido.find(query.toString(), params.toArray(paramsSize)).fetch();
		
		return pedidosAbertos;
	}
	
}
