package org.elfinder.servlets;

/**
 * @author Antoine Walter (www.anw.fr)
 * @date 29 aug. 2011
 * @version $Id$
 * @license BSD
 */

/**
 * Actions performable by the filesystem.
 */
public enum FileActionEnum {
	READ,
	WRITE,
	DELETE,
	// relative to current directory:
	CREATE_FILE,
	CREATE_DIR;

	private FileActionEnum() {

	}
}
