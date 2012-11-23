/**
 * 
 */
package controllers;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import business.produto.ProdutoControl;
import business.produto.layout.LayoutArquivo;
import business.produto.layout.parse.factory.LayoutFactory;

import models.Fornecedor;
import play.Logger;
import play.data.validation.Required;
import play.data.validation.Valid;
import play.db.jpa.Transactional;
import play.i18n.Messages;
import play.mvc.Before;
import play.mvc.Controller;

/**
 * @author guerrafe
 *
 */
public class Fornecedores extends BaseController {
	
	@Before(unless={"upload", "sendArchive"})
	static void estaAutorizado() {
		Logger.debug("####### Verificar se o usuário autenticado é admin... ########");
		
		if(!session.contains("isAdmin") || new Boolean(session.get("isAdmin"))==false) {
			Logger.debug("####### Usuário não autorizado a acessar essa funcionalidade...%s ########", session.get("usuarioAutenticado"));
			
			Home.index("Usuário não autorizado a acessar essa funcionalidade.");
		}
	}
	
	public static void index(Fornecedor fornecedor, String success) {
		if(fornecedor==null)
			fornecedor = new Fornecedor();
		
		render(fornecedor, success);
	}
	
	@Transactional(readOnly=false)
	public static void cadastrar(@Valid Fornecedor fornecedor) {
		Logger.debug("###### Início - Cadastrar Fornecedor: %s ######", fornecedor.getNome());
		String message = null;
		
		if(validation.hasErrors()) {
			Logger.debug("###### Erro - Cadastrar Fornecedor: %s ######", fornecedor.getNome());
			
			validation.keep();
			params.flash();
			
		}else {
			fornecedor.setAtivo(true);
			
			fornecedor.save();
			
			message = Messages.get("form.insert.data.success", "");
			
			params.put("success", message);
		}
		Logger.debug("###### Fim - Cadastrar Fornecedor: %s ######", fornecedor.getNome());
		show();
	}
	
	public static void show() {
		Logger.debug("###### Início - Consultar todos os Fornecedores... ######", "");
		
		List<Fornecedor> fornecedores = Fornecedor.findAll();
		
		render(fornecedores);
	}
	
	public static void edit(Long id) {
		Logger.debug("###### Início - Consultar o Fornecedor pelo id: %s ######", id);
		Fornecedor fornecedor = Fornecedor.findById(id);
		
		Logger.debug("###### Fim - Consultar o Fornecedor pelo id: %s ######", id);
		render(fornecedor);
	}
	
	@Transactional(readOnly=false)
	public static void atualizar(@Valid Fornecedor fornecedor) {
		Logger.debug("###### Início - Atualizar Fornecedor: %s ######", fornecedor.getNome());
		
		
		if(validation.hasErrors()) {
			Logger.debug("###### Erro - Atualizar Fornecedor: %s ######", fornecedor.getNome());
			
			validation.keep();
			params.flash();
			
			edit(fornecedor.id);
			
		}else {
			fornecedor.save();
			
			Logger.debug("###### Fim - Atualizar Fornecedor: %s ######", fornecedor.getNome());
			show();
		}
	}
	
	public static void upload(String message) {
		List<Fornecedor> fornecedores = Fornecedor.find("ativo = ?", Boolean.TRUE).fetch();
		
		render(fornecedores, message);
	}
	
	public static void sendArchive(@Required(message="Por favor, selecione algum arquivo") File imagem, 
								Long idFornecedor,
								Integer atualizarProdutos) throws IOException {
		Logger.info("##### Início - Enviar arquivo de parceiro [%s] #####", session.get("usuarioAutenticado"));
		File path = null;
		String caminhoArquivos = null;
		String message = null;
		Long maxLengthArchive = null;
		
		if(validation.hasErrors()) {
			validation.keep();
			params.flash();
			
		}else {
			Logger.info("##### Vai mover o arquivo %s - tamanho: %s Kb #####", imagem.getName(), imagem.length()/1024);
			
			maxLengthArchive = Long.parseLong(Messages.get("form.parceiros.upload.archive.maxLength", ""));
			
			if(imagem.length()>maxLengthArchive) {
				validation.addError("imagem", "form.parceiros.error.archive.maxLength", "");
				validation.keep();
				params.flash();
				
			}if(atualizarProdutos.equals(1) && idFornecedor==null) {
				validation.addError("imagem", "message.required.product.fornecedor", "");
				validation.keep();
				params.flash();
				
			}else {
				caminhoArquivos = Messages.get("application.path.upload.archives", "");
				
				if(!StringUtils.isEmpty(caminhoArquivos)) {
					path = new File(caminhoArquivos + (idFornecedor==null ? "" :  String.valueOf(idFornecedor)));
					Logger.info("#### Diretório arquivo %s ####", path.getAbsolutePath());
					
					if(path.exists()) {
						for(File arquivo : path.listFiles())
							if(!arquivo.isDirectory())
								arquivo.delete();
						
						path.delete();
						
						Logger.info("#### Diretório %s apagado? %s ####", path.getAbsolutePath(), !path.exists());
					}
					path.mkdir();
					
					FileUtils.copyFileToDirectory(imagem, path);
				
					if(atualizarProdutos.equals(1)) {
						new ProdutoControl().atualizarProdutos(idFornecedor);
						message = "Arquivo enviado e produtos do site atualizados.";
						
					}else if(atualizarProdutos.equals(2)) {
						ProdutoControl control = new ProdutoControl(LayoutFactory.getLayout(LayoutArquivo.PRODUTO_CSV));
						control.atualizarProdutos();
						
						message = "Arquivo enviado e dados dos produtos atualizados.";
					}
					
				}else {
					validation.addError("imagem", "form.parceiros.path.notConfigured", "");
				}
			}
		}
		Logger.info("##### Fim - Enviar arquivo de parceiro [%s] #####", session.get("usuarioAutenticado"));
		
		upload(message);
	}

}
