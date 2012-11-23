import java.util.List;

import javax.persistence.EntityManager;

import models.Produto;

import org.apache.lucene.search.Query;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.Search;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import business.produto.ProdutoControl;

import play.db.jpa.JPA;
import play.test.UnitTest;

/**
 * 
 */

/**
 * @author hpadmin
 *
 */
public class LuceneTest extends UnitTest {
	
	@Test
	public void testLucene() {
		EntityManager em = null;
		FullTextEntityManager fullTextEntityManager = null;
		
		try {
			em = JPA.em();
			fullTextEntityManager = Search.getFullTextEntityManager(em);
			
			
			
		}catch(Exception e) {
			fail(e.getLocalizedMessage());
		}
	}
	
	public void testQueryParser() {
		try {
			//QueryParser parser = new QueryParser()
			
		}catch(Exception e) {
			fail(e.getMessage());
		}
	}
	
	
	@After
	public void shutdown() {
		clearJPASession();
	}
}
