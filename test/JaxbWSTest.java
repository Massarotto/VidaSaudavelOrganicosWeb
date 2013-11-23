import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.transform.stream.StreamSource;

import oauth.signpost.http.HttpRequest;

import org.junit.Test;
import org.w3c.dom.Document;

import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream;

import antlr.CharBuffer;

import play.Logger;
import play.i18n.Messages;
import play.libs.WS;
import play.libs.WS.HttpResponse;
import play.libs.WS.WSRequest;
import play.test.UnitTest;
import types.pagseguro.CheckoutPagSeguro;
import types.pagseguro.ItemPagSeguro;
import types.pagseguro.SenderPagSeguro;
import types.pagseguro.SenderPagSeguro.Phone;
import types.pagseguro.ShippingPagSeguro;

/**
 * 
 */

/**
 * @author hpadmin
 *
 */
public class JaxbWSTest extends UnitTest {
	
	
	public CheckoutPagSeguro testMarshall() {
		try {
			CheckoutPagSeguro pagSeguro = new CheckoutPagSeguro();
			pagSeguro.setCurrency("BRL");
			pagSeguro.setReference("Teste");
			
			ItemPagSeguro itemPagSeguro = new ItemPagSeguro();
			itemPagSeguro.setAmount(BigDecimal.ZERO);
			itemPagSeguro.setDescription("Description");
			itemPagSeguro.setId(2L);
			
			List<ItemPagSeguro> items = new ArrayList<ItemPagSeguro>();
			items.add(itemPagSeguro);
			pagSeguro.setItems(items);
			
			SenderPagSeguro sender = new SenderPagSeguro();
			sender.setEmail("teste@teste.com");
			sender.setName("Teste");
			Phone phone = new Phone();
			phone.setAreaCode("11");
			phone.setNumber("99999-0000");
			sender.setPhone(phone);
			
			ShippingPagSeguro shippingPagSeguro = new ShippingPagSeguro();
			shippingPagSeguro.setType(3);
			
			pagSeguro.setShipping(shippingPagSeguro);
			pagSeguro.setSender(sender);
			pagSeguro.setRedirectURL("http://www.vidasaudavelorganicos.com.br/pedido/pagseguro/finalizar");
			
			return pagSeguro;
			
		}catch(Exception e) {
			e.printStackTrace();
			fail("Erro no teste: " + e.getMessage());
			return null;
		}
	}

	@Test
	public void testPayPalService() {
		try {
			
			OutputStream output = new ByteArrayOutputStream();
			System.setProperty("http.proxyHost", "proxy.houston.hp.com");
	        System.setProperty("http.proxyPort", "8080");
	        System.setProperty("https.proxyHost", "proxy.houston.hp.com");
	        System.setProperty("https.proxyPort", "8080");
			
			
			JAXBContext jaxbContext = JAXBContext.newInstance(CheckoutPagSeguro.class);
			Marshaller marshaller = jaxbContext.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_ENCODING, "ISO-8859-1");	
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			marshaller.marshal(this.testMarshall(), output);
			
			System.out.println(output);
			
			/*
			WSRequest request = WS.url("https://ws.pagseguro.uol.com.br/v2/checkout?email=felipe@vidasaudavelorganicos.com.br&token=8D96365337A04FC99809187EB0137863")
								.setHeader("Content-Type", "application/xml");
			request.body(output);
			HttpResponse resp = request.post();
			
			System.out.println(resp.getStatus() + " - "+ resp.getString() );
			*/
			
		}catch(Exception e) {
			fail(e.getMessage());
		}
	}
	
}
