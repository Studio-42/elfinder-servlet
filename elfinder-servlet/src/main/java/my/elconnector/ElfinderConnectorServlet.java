package my.elconnector;

import java.io.File;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.log4j.Logger;
import org.elfinder.servlets.AbstractConnectorServlet;
import org.elfinder.servlets.config.AbstractConnectorConfig;

/**
 * @author Özkan Pakdil
 * @date 29 aug. 2011
 * @version $Id$
 * @license BSD
 */

/**
 * Sample of custom servlet implementation.
 */
@SuppressWarnings("serial")
public class ElfinderConnectorServlet extends AbstractConnectorServlet {

	public static String SHARED_DOCS = "Shared docs";
	public static String THUMBNAIL = "/thumbnailer?p=";
	public static String HOME_SHARED_DOCS = "/home/shared-docs";

	private static Logger S_LOG = Logger.getLogger(ElfinderConnectorServlet.class);

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		if(!StringUtils.isBlank(getServletContext().getInitParameter("HOME_SHARED_DOCS"))){
			HOME_SHARED_DOCS = getServletContext().getInitParameter("HOME_SHARED_DOCS");
			File f=new File(HOME_SHARED_DOCS);
			if(!f.exists()){
				f.mkdirs();
			}
		}
		if(!StringUtils.isBlank(getServletContext().getInitParameter("THUMBNAIL")))
			THUMBNAIL = getServletContext().getInitParameter("THUMBNAIL");
		if(!StringUtils.isBlank(getServletContext().getInitParameter("SHARED_DOCS")))
			SHARED_DOCS = getServletContext().getInitParameter("SHARED_DOCS");
	}

	@Override
	protected AbstractConnectorConfig prepareConfig(HttpServletRequest request) {
		// here we could use various configs based on request URL/cookies...
		return new MySampleConfig();
	}

}
