import java.util.List;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import models.Desconto;
import models.Fornecedor;
import models.Pedido;
import models.PedidoItem;
import models.Produto;

import org.junit.Test;

import play.db.jpa.JPA;
import play.test.UnitTest;

public class BasicTest extends UnitTest {

    @Test
    public void aVeryImportantThingToTest() {
        assertEquals(2, 1 + 1);
    }
    @Test
    public void testQueryProdutosComplexa() {
    	try {
    		CriteriaBuilder builder = JPA.em().getCriteriaBuilder();
    		
    		CriteriaQuery<Produto> query = builder.createQuery(Produto.class);
    		Root<Produto> root = query.from(Produto.class);
    		Join<Produto, PedidoItem> joinItems = root.join("listPedidoItem");
    		joinItems.alias("pedidoProduto");
    		Join<PedidoItem, Pedido> joinPedidos = joinItems.join("pedido");
    		Predicate isAtivo = builder.equal(root.get("ativo"), Boolean.TRUE);
    		Join<Produto, Fornecedor> joinFornec = root.join("fornecedor");
    		Predicate nome = builder.notEqual(joinFornec.get("nome"), "Santos Orgânicos");
    		Predicate status = builder.equal(joinPedidos.get("codigoEstadoPedido"), Pedido.PedidoEstado.AGUARDANDO_ENTREGA);
    		query.where(isAtivo, nome, status).orderBy(builder.asc(joinFornec.get("nome")));
    		
    		TypedQuery<Produto> queryProdutos = JPA.em().createQuery(query);
    		List<Produto> produtos = queryProdutos.getResultList();
    		
    		assertNotNull(produtos);
 
    	}catch(Exception e) {
    		e.printStackTrace();
    		fail("error");
    	}
    }
    
   
    public void testCupomDesconto() {
    	try {
    		List<Desconto> result = Desconto.find("cupons <> ?", "").fetch();
    		
    	}catch(Exception e) {
    		e.printStackTrace();
    		fail(e.getMessage());
    	}
    	
    }

}
