/**
 * 
 */
package controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import org.apache.commons.lang.StringUtils;

import models.Produto;
import models.ProdutoEstoque;
import play.Logger;
import play.data.binding.As;
import play.db.jpa.Transactional;
import play.modules.paginate.ValuePaginator;
import play.mvc.Before;
import types.EstoqueService;
import business.estoque.EstoqueControl;

/**
 * @author hpadmin
 *
 */
public class Estoque extends BaseController {
	
	@Before(unless={"reservarProduto", "reporProdutoEstoque"})
	static void estaAutorizado() {
		Logger.debug("####### Verificar se o usuário autenticado é admin... ########");
		
		if( (StringUtils.isEmpty(session.get("isAdmin")) || Boolean.FALSE.equals(Boolean.valueOf(session.get("isAdmin")))) 
				&& (StringUtils.isEmpty(session.get("isEmployee")) && Boolean.FALSE.equals(Boolean.valueOf(session.get("isEmployee")))) ) 
			{
			Logger.debug("####### Usuário não autorizado a acessar essa funcionalidade...%s ########", session.get("usuarioAutenticado"));
			
			Home.index("Usuário não autorizado a acessar essa funcionalidade.");
		}
	}
	
	public static void index() {
		ProdutoEstoque estoque = new ProdutoEstoque();
		List<Produto> produtos = Produto.find("ativo = ? order by descricao asc", Boolean.TRUE).fetch();
		
		render(estoque, produtos);
	}
	
	public static void show() {
		List<ProdutoEstoque> listaEstoque = ProdutoEstoque.findAll();
		
		ValuePaginator<ProdutoEstoque> posicaoEstoque = new ValuePaginator<ProdutoEstoque>(listaEstoque);
		posicaoEstoque.setPageSize(30);
		
		render(posicaoEstoque);
	}
	
	@Transactional(readOnly=false)
	public static void cadastrar(ProdutoEstoque estoque, @As("dd/MM/yyyy") Date dataValidade) {
		Logger.debug("#### Início - Cadastrar Estoque para o produto id: %s ####", estoque.getProduto().id);
		
		if(ProdutoEstoque.find("produto.id = ?", estoque.getProduto().id).first()!=null)
			validation.addError("estoque.produto", "message.error.estoque.produto", "");
		
		if(validation.hasErrors()) {
			validation.keep();
			
			index();
			
		}else {
			Produto produto = Produto.findById(estoque.getProduto().id);
			produto.setProdutoEstoque(estoque);
			produto.setDataValidade(dataValidade);
			estoque.setProduto(produto);
			estoque.setDataCadastro(new Date());
			estoque.setUsuarioAlteracao(session.get("usuarioAutenticado"));
			
			estoque.save();
		}
		Logger.debug("#### Fim - Cadastrar Estoque para o produto id: %s ####", estoque.getProduto().id);
		
		show();
	}
	
	@Transactional(readOnly=false)
	public static void changeStatus(Long id) {
		ProdutoEstoque _estoque = EstoqueControl.loadEstoque(id, null);
		
		_estoque.setAtivo(!_estoque.getAtivo());
		
		_estoque.save();
		
		show();
	}
	
	public static void edit(Long id) {
		ProdutoEstoque estoque = ProdutoEstoque.findById(id);
		
		render(estoque);
	}
	
	@Transactional(readOnly=false)
	public static void atualizar(ProdutoEstoque estoque, @As("dd/MM/yyyy") Date dataValidade) {
		Logger.debug("#### Início - Cadastrar Estoque para o produto id: %s ####", estoque.getProduto().id);
		
		if(validation.hasErrors()) {
			validation.keep();
			
			edit(estoque.getProduto().id);
			
		}else {
			Produto produto = Produto.findById(estoque.getProduto().id);
			produto.setProdutoEstoque(estoque);
			
			if(dataValidade!=null)
				produto.setDataValidade(dataValidade);	
			
			estoque.setProduto(produto);
			
			EstoqueControl.atualizarEstoque(estoque, estoque.getQuantidade(), session.get("usuarioAutenticado"));
		}
		Logger.debug("#### Fim - Cadastrar Estoque para o produto id: %s ####", estoque.getProduto().id);
		
		show();
	}

	@SuppressWarnings("all")
	public static void order(String order, Boolean asc) {
		StringBuffer params = new StringBuffer();
		params.append("order by ").append(order).append(" ").append(asc ? "ASC" : "DESC");
		
		List<ProdutoEstoque> prods = ProdutoEstoque.find(params.toString(), null).fetch();
		
		ValuePaginator<ProdutoEstoque> posicaoEstoque = new ValuePaginator<ProdutoEstoque>(prods);
		posicaoEstoque.setPageSize(30);
		
		renderTemplate("Estoque/show.html", posicaoEstoque);
	}

	public static void findByParams(String produtoParametro, String param) {
		Logger.debug("######## Início - Pesquisar estoque pelo parâmetro: %s########", param); 
		List<ProdutoEstoque> estoque = new ArrayList<ProdutoEstoque>();
		StringBuilder query = new StringBuilder();
		Object parametro = null;

		if("produto.dataValidade".equalsIgnoreCase(param)){
			SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
			
			try {
				parametro = dateFormat.parse(produtoParametro);
				query.append(param);
				query.append(" <= ?");
				
			}catch(Exception e) {
				validation.addError("dataPedido", "Digite uma data válida.", "");
				Logger.error(e, "Não foi possível converter a data: %s", param);
			}
			
		}else {
			parametro = "%"+produtoParametro.toUpperCase()+"%";
			query.append("UPPER(");
			query.append(param);
			query.append(") LIKE ? ");
		}
		
		if(!validation.hasErrors())
			estoque = ProdutoEstoque.find(query.toString(), parametro).fetch();
		
		Logger.debug("######## Fim - Pesquisar estoque pelo parâmetro: %s########", param);
		
		ValuePaginator<ProdutoEstoque> posicaoEstoque = new ValuePaginator<ProdutoEstoque>(estoque);
		posicaoEstoque.setPageSize(30);
		
		render("Estoque/show.html", posicaoEstoque);
	}
	
	@Transactional(readOnly=false)
	public static void reservarProduto(Long idProduto, Integer qtd) {
		ProdutoEstoque estoque = null;
		EstoqueService service = null;
		Marshaller marshaller = null;
		File xmlReservaEstoque = null;
		InputStreamReader reader = null;
		
		try{
			Logger.info("Início - Reserva de estoque para o produto: %s", idProduto);
			
			service = new EstoqueService();
			estoque = EstoqueControl.loadEstoque(null, idProduto);
			
			JAXBContext jaxbContext = JAXBContext.newInstance(EstoqueService.class);
			marshaller = jaxbContext.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			
			xmlReservaEstoque = new File(System.getProperty("java.io.tmpdir")+File.separatorChar + "reservaEstoque_" + new Date().getTime() + ".xml");
			xmlReservaEstoque.createNewFile();
			
			reader = new InputStreamReader(new FileInputStream(xmlReservaEstoque));
			
			if(estoque==null) {
				service.setError(1000L);
				service.setMessage( service.getErrorMessage(1000L) );
				
			}else if(!estoque.getAtivo() || estoque.getQuantidade()<=0 || (qtd!=null && estoque.getQuantidade()<qtd)) {
				service.setError(1001L);
				service.setMessage( service.getErrorMessage(1001L) );
				
			}else {
				estoque.setQuantidade(estoque.getQuantidade() - (qtd==null ? 1 : qtd));
				
				EstoqueControl.atualizarEstoque(estoque);
				
				service.setMessage("Sucesso");
			}
			
			marshaller.marshal(service, xmlReservaEstoque);
			
			char[] buffer = new char[new FileInputStream(xmlReservaEstoque).available()];
			
			reader.read(buffer);
			
			renderXml(new String(buffer));
			
		}catch(Exception e) {
			Logger.error(e, "Erro ao tentar reservar o produto. id: " + idProduto);
			service.setError(9999L);
			service.setMessage("Ocorreu um erro ao tenatr reservar o produto: " + e.getMessage());
			renderXml(e);
			
		}finally {
			if(reader!=null)
				try {
					reader.close();
					
				}catch(IOException e) {}
			
			Logger.info("Fim - Reserva de estoque para o produto: %s", idProduto);
		}
	}
	
	@Transactional(readOnly=false)
	public static void reporProdutoEstoque(Long idProduto, Integer qtd) {
		ProdutoEstoque estoque = null;
		EstoqueService service = null;
		Marshaller marshaller = null;
		File xmlReservaEstoque = null;
		InputStreamReader reader = null;
		
		try{
			Logger.info("Início - Repor estoque para o produto: %s", idProduto);
			
			service = new EstoqueService();
			estoque = EstoqueControl.loadEstoque(null, idProduto);
			
			JAXBContext jaxbContext = JAXBContext.newInstance(EstoqueService.class);
			marshaller = jaxbContext.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			
			xmlReservaEstoque = new File(System.getProperty("java.io.tmpdir")+File.separatorChar + "reporEstoque_" + new Date().getTime() + ".xml");
			xmlReservaEstoque.createNewFile();
			
			reader = new InputStreamReader(new FileInputStream(xmlReservaEstoque));
			
			if(estoque==null) {
				service.setError(1000L);
				service.setMessage( service.getErrorMessage(1000L) );
				
			}else if(!estoque.getAtivo()) {
				service.setError(1001L);
				service.setMessage( service.getErrorMessage(1001L) );
				
			}else {
				estoque.setQuantidade(estoque.getQuantidade() + (qtd==null ? 1 : qtd));
				
				EstoqueControl.atualizarEstoque(estoque);
				
				service.setMessage("Sucesso");
			}
			
			marshaller.marshal(service, xmlReservaEstoque);
			
			char[] buffer = new char[new FileInputStream(xmlReservaEstoque).available()];
			
			reader.read(buffer);
			
			renderXml(new String(buffer));
			
		}catch(Exception e) {
			Logger.error(e, "Erro ao tentar repor o produto. id: " + idProduto);
			service.setError(9999L);
			service.setMessage("Ocorreu um erro ao tenatr reservar o produto: " + e.getMessage());
			renderXml(e);
			
		}finally {
			if(reader!=null)
				try {
					reader.close();
					
				}catch(IOException e) {}
			
			Logger.info("Fim - Repor estoque para o produto: %s", idProduto);
		}
	}
}
