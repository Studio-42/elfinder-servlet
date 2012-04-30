package my.elconnector;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.elfinder.servlets.config.AbstractConnectorConfig;
import org.elfinder.servlets.fs.DiskFsImpl;
import org.elfinder.servlets.fs.IFsImpl;

/**
 * @author Antoine Walter (www.anw.fr)
 * @date 29 aug. 2011
 * @version $Id$
 * @license BSD
 */

/**
 * Sample of custom connector configuration.
 */
public class MySampleConfig extends AbstractConnectorConfig {

	private static final Logger S_LOG = Logger.getLogger(MySampleConfig.class);

	/**
	 * Filesystem.
	 */
	private DiskFsImpl fsImpl;

	public MySampleConfig() {
		fsImpl = new DiskFsImpl();
	}

	@Override
	public IFsImpl getFs() {
		return fsImpl;
	}

	@Override
	public String getRoot() {
		return ElfinderConnectorServlet.HOME_SHARED_DOCS;
	}

	@Override
	public String getRootUrl() {
		return ElfinderConnectorServlet.THUMBNAIL;
	}

	@Override
	public String rootAliasOrBaseName() {
		return ElfinderConnectorServlet.SHARED_DOCS;
	}
}
