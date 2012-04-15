package org.elfinder.servlets.fs;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.elfinder.servlets.FsException;
import org.elfinder.servlets.config.AbstractConnectorConfig;


/**
 * @author Antoine Walter (www.anw.fr)
 * @date 29 aug. 2011
 * @version $Id$
 * @license BSD
 */

/**
 * Filesystem based on Java IO.
 */
public class DiskFsImpl implements IFsImpl {

	private AbstractConnectorConfig config;

	public DiskFsImpl() {
	}

	public void init(AbstractConnectorConfig config) {
		this.config = config;
	}

	protected AbstractConnectorConfig getConfig() {
		return config;
	}

	public void createFile(File newFile, ByteArrayOutputStream os) throws FsException {
		if (os == null) {
			os = new ByteArrayOutputStream();
		}
		try {
			if (!newFile.createNewFile()) {
				throw new FsException("unable to create file");
			}
			try {
				FileOutputStream fs = new FileOutputStream(newFile);
				fs.write(os.toByteArray());
				fs.flush();
				fs.close();
			} catch (Exception ee) {
				newFile.delete();
				throw new FsException("unable to write file");
			}
		} catch (Exception e) {
			throw new FsException("unable to create file");
		}
	}

	public void createFolder(File folder) throws FsException {
		boolean ok = folder.mkdir();
		if (!ok) {
			throw new FsException("Unable to create folder");
		}
	}

	public void renameFileOrDirectory(File targetFile, File futureFile) throws FsException {
		boolean ok = targetFile.renameTo(futureFile);
		if (!ok) {
			throw new FsException("Unable to rename file from " + targetFile.getPath() + " to " + futureFile.getPath());
		}
	}

	public void copyFileOrDirectory(File targetFile, File futureFile) throws FsException {
		boolean ok = true;
		try {
			if (targetFile.isDirectory()) {
				FileUtils.copyDirectory(targetFile, futureFile);
			} else {
				FileUtils.copyFile(targetFile, futureFile);
			}
		} catch (IOException e) {
			ok = false;
		}
		if (!ok) {
			throw new FsException("Unable to copy file from " + targetFile.getPath() + " to " + futureFile.getPath());
		}
	}

	public void moveFile(File file, File futureFile) throws FsException {
		// renameTo doesn't always work on windows?
		try {
			FileUtils.copyFile(file, futureFile);
			if (!file.delete()) {
				throw new Exception();
			}
		} catch (Exception e) {
			throw new FsException("Unable to move file");
		}
	}

	public void removeFile(File path) throws FsException {
		if (!path.delete()) {
			throw new FsException("Unable to remove file");
		}
	}

	public void removeEmptyDirectory(File path) throws FsException {
		if (!path.delete()) {
			throw new FsException("Unable to remove directory");
		}
	}

	public boolean isAllowedFile(File file) {
		return true;
	}

	public long getDirSize(File dir) {
		return FileUtils.sizeOfDirectory(dir);
	}

	public long getFileSize(File file) {
		return file.length();
	}

}
