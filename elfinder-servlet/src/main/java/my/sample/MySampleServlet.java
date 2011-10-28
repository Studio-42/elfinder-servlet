package my.sample;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.elfinder.servlets.AbstractConnectorServlet;
import org.elfinder.servlets.config.AbstractConnectorConfig;

/**
 * @author Antoine Walter (www.anw.fr)
 * @date 29 aug. 2011
 * @version $Id$
 * @license BSD
 */

/**
 * Sample of custom servlet implementation.
 */
@SuppressWarnings("serial")
public class MySampleServlet extends AbstractConnectorServlet {

	private static Logger S_LOG = Logger.getLogger(MySampleServlet.class);

	public static final String SERVLET_URL = "/docs/";

	@Override
	protected AbstractConnectorConfig prepareConfig(HttpServletRequest request) {
		// here we could use various configs based on request URL/cookies...
		return new MySampleConfig();
	}

}
