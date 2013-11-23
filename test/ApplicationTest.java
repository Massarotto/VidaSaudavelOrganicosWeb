import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.junit.Test;

import play.test.FunctionalTest;

public class ApplicationTest extends FunctionalTest {

    @Test
    public void testJNDIJetty() {
    	try {
    		Properties properties = new Properties();
    		properties.put(javax.naming.Context.PROVIDER_URL, "http://localhost:8008/");
    		properties.put(javax.naming.Context.INITIAL_CONTEXT_FACTORY, "org.eclipse.jetty.jndi.InitialContextFactory");
    		
    		Context context = new InitialContext(properties);
    		Object envs = context.lookup("java:/comp/env/jdbc/vsorganicos");
    		
    		envs.hashCode();
    		
    	}catch(Exception e) {
    		e.printStackTrace();
    		fail(e.getMessage());
    	}
    }
    
}