package org.elfinder.servlets;

/**
 * @author Antoine Walter (www.anw.fr)
 * @date 29 aug. 2011
 * @version $Id$
 * @license BSD
 */

/**
 * Exception thrown during command execution.
 */
public class ConnectorException extends Exception {

	public ConnectorException(String message) {
		super(message);
	}

	public ConnectorException(String message, Throwable e) {
		super(message, e);
	}

}
