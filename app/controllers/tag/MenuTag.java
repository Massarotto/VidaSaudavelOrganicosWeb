/**
 * 
 */
package controllers.tag;

import groovy.lang.Closure;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import models.Secao;

import controllers.SecaoProdutos;

import play.Logger;
import play.cache.Cache;
import play.templates.FastTags;
import play.templates.GroovyTemplate.ExecutableTemplate;

/**
 * @author guerrafe
 *
 */
@FastTags.Namespace("vidasaudavel.tag")
public class MenuTag extends FastTags {

	public MenuTag() {
		super();
	}

	public static void _menu(Map<?, ?> args, Closure body, PrintWriter out,
							ExecutableTemplate template, int fromLine) {
		
		List<Secao> secoes = Cache.get("menu", List.class);
		 
		if(secoes==null || secoes.isEmpty())
			secoes = SecaoProdutos.loadAll();
		 
		List<NoSecao> nos = load(secoes);

		Logger.debug("#### Fim renderizar menu... ####","");
		
		Cache.add("menu", secoes, "480mn");
		
		for(NoSecao _no : nos)
			out.print(_no.buildMenu());
	}
	
	private static List<NoSecao> load(List<Secao> secoes) {
		List<NoSecao> nos = new ArrayList<NoSecao>(0);
		
		if(secoes!=null && !secoes.isEmpty()) {
			Collections.sort(secoes);
			nos = new LinkedList<NoSecao>();
			
			NoSecao no = null;
			
			for(Secao secao : secoes) {
				if(secao.getSecaoPai()==null) {
					no = new NoSecao(secao.getSecaoPai(), secao);
				
					order(no, secoes);
					
					nos.add(no);
				}
			}
		}
		return nos;
	}
	
	private static void order(NoSecao no, List<Secao> _secoes) {
		for(Secao secao : _secoes) {
			if(no.getSecao().equals(secao.getSecaoPai())) {
				NoSecao _tmpNo = new NoSecao(null, secao);
				
				no.addFilho(_tmpNo);
				
				order(_tmpNo, _secoes);
			}
		}
	}
	
}
