package org.elfinder.servlets.commands;

import org.elfinder.servlets.ConnectorException;

/**
 * @author Antoine Walter (www.anw.fr)
 * @date 29 aug. 2011
 * @version $Id$
 * @license BSD
 */
public class ContentCommand extends AbstractCommandOverride {

	@Override
	public void execute() throws ConnectorException {
		contentCommand();
	}
}
