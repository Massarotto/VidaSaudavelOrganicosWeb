/**
 * 
 */
package controllers;

import play.Logger;
import play.cache.Cache;
import play.libs.Codec;
import play.libs.Images;
import play.mvc.Controller;

/**
 * @author guerrafe
 *
 */
public class Application extends BaseController {
	
	public static void captcha(String id) {
		Images.Captcha captcha = Images.captcha();
		Logger.debug("######## Vai adicionar o captcha ao cache %s ########", id);
		
		String code = captcha.getText();
		
		Cache.set(id, code, "5mn");
		
		renderBinary(captcha);
	}

}
