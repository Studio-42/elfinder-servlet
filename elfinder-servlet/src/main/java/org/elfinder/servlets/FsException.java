package org.elfinder.servlets;

/**
 * @author Antoine Walter (www.anw.fr)
 * @date 29 aug. 2011
 * @version $Id$
 * @license BSD
 */

/**
 * Exception thrown by the filesystem.
 */
public class FsException extends Exception {

	public FsException(String message) {
		super(message);
	}

	public FsException(String message, Throwable e) {
		super(message, e);
	}

}
