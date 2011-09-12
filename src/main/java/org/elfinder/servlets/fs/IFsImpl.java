package org.elfinder.servlets.fs;

import java.io.ByteArrayOutputStream;
import java.io.File;

import org.elfinder.servlets.FsException;
import org.elfinder.servlets.config.AbstractConnectorConfig;


/**
 * @author Antoine Walter (www.anw.fr)
 * @date 29 aug. 2011
 * @version $Id$
 * @license BSD
 */

/**
 * Interface implemented by a filesystem.<br>
 * A filesystem is the place where ElFinder's file and directories are stored. This is not necessary a "physical" filesystem, this could be a database, remote location or anything else.
 */
public interface IFsImpl {

	public void init(AbstractConnectorConfig config);

	/**
	 * Create a new file and write content.
	 * 
	 * @param newFile
	 * @param os may be null
	 * @throws FsException
	 */
	public void createFile(File newFile, ByteArrayOutputStream os) throws FsException;

	/**
	 * Create a new folder.
	 * @param folder
	 * @throws FsException
	 */
	public void createFolder(File folder) throws FsException;

	/**
	 * Rename a file or directory.
	 * @param targetFile
	 * @param futureFile
	 * @throws FsException
	 */
	public void renameFileOrDirectory(File targetFile, File futureFile) throws FsException;

	/**
	 * Copy a file or directory.
	 * @param targetFile
	 * @param futureFile
	 * @throws FsException
	 */
	public void copyFileOrDirectory(File targetFile, File futureFile) throws FsException;

	/**
	 * Move a file.
	 * @param file
	 * @param targetDirectory
	 * @throws FsException
	 */
	public void moveFile(File file, File targetDirectory) throws FsException;

	/**
	 * Delete a file.
	 * @param path
	 * @throws FsException
	 */
	public void removeFile(File path) throws FsException;

	/**
	 * Delete an empty directory.
	 * @param path
	 * @throws FsException
	 */
	public void removeEmptyDirectory(File path) throws FsException;

	/**
	 * Additional permission check performed by filesystem: returns true if filesystem allows access to the file, else false.
	 * @param file
	 * @return
	 */
	public boolean isAllowedFile(File file);

	/**
	 * Dir size in bytes.
	 * 
	 * @param dir
	 * @return dir size in bytes
	 */
	public long getDirSize(File dir);

	/**
	 * File size in bytes.
	 * 
	 * @param file
	 * @return file size in bytes
	 */
	public long getFileSize(File file);
}
