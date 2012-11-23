import java.io.InputStream;
import java.io.StringWriter;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.*;

import business.pagamento.service.PayPalService;

import controllers.Mail;
import ebay.api.paypalapi.SetExpressCheckoutResponseType;
import ebay.apis.eblbasecomponents.ErrorType;
import exception.SystemException;
import play.Logger;
import play.test.*;
import play.libs.WS;
import play.libs.WS.HttpResponse;
import play.libs.WS.WSRequest;
import play.mvc.*;
import play.mvc.Http.*;
import models.*;

public class ApplicationTest extends FunctionalTest {

    @Test
    public void testThatIndexPageWorks() {
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
//				HttpResponse response = WS.url(queryString.toString()).get();
//				
//				System.out.println( response.getStatus() );
//				
//				InputStream input = response.getStream() ;
//				StringWriter output = new StringWriter();
//				
//				IOUtils.copy(input, output);
//				
//				Logger.info("Resposta PayPal: %s", output);
//				
//				assertTrue(true);
//				
//			}else {
//				for(ErrorType error : errors)
//					System.out.println(error.getErrorCode() + "->" + error.getLongMessage());
//				
//				assertTrue(false);
//			}
			
    	}catch(Exception e) {
    		fail(e.getMessage());
    	}
    }
    
}