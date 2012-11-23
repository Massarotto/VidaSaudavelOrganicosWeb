import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import models.Produto;

import org.junit.Test;

import ebay.api.paypalapi.SetExpressCheckoutResponseType;
import ebay.apis.eblbasecomponents.ErrorType;

import business.pagamento.service.PayPalService;

import play.mvc.Controller;
import play.mvc.Http.Response;
import play.mvc.results.Redirect;
import play.test.UnitTest;
import types.ListaProduto;

/**
 * 
 */

/**
 * @author hpadmin
 *
 */
public class JaxbWSTest extends UnitTest {
	
	
	public void testMarshall() {
		try {
			List<Produto> prods = Produto.findAll();
			ListaProduto produtos = new ListaProduto( prods );
			
			JAXBContext jaxbContext = JAXBContext.newInstance(ListaProduto.class);
			Marshaller marshaller = jaxbContext.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_ENCODING, "ISO-8859-1");
			marshaller.marshal(produtos, System.out);
			
		}catch(Exception e) {
			e.printStackTrace();
			fail("Erro no teste: " + e.getMessage());
		}
		
	}

	@Test
	public void testPayPalService() {
		try {
			PayPalService service = PayPalService.newInstance("https://api-3t.sandbox.paypal.com/2.0/", 
															"admin_1349354368_biz_api1.vidasaudavelorganicos.com.br", 
															"1349354393", "AFcWxV21C7fd0v3bYYYRCpSSRl31Abkbb2lU0cYLOHGFsrtTe7ddMY.R");
			
//			SetExpressCheckoutResponseType test = service.solicitarPagamento("Joselito", 100.0d, 9999999L);
//			
//			List<ErrorType> errors = test.getErrors();
//			
//			if(errors.isEmpty()) {
//				System.out.println(test.getToken() + " ok: " + test.getCorrelationID() + ", " + test.getBuild());
//				StringBuffer queryString = new StringBuffer();
//				queryString.append(service.getUrlConfirmacaoPagamento());
//				queryString.append(test.getToken());
//				
//				Controller.redirect(queryString.toString(), "");
//				
//			}else {
//				for(ErrorType error : errors)
//					System.out.println(error.getErrorCode() + "->" + error.getLongMessage());
//			
//			}
			
		}catch(Exception e) {
			fail(e.getMessage());
		}
	}
	
}
