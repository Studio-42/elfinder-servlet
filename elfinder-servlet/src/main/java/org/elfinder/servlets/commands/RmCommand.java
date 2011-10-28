package org.elfinder.servlets.commands;

import java.io.File;
import java.util.List;

import org.elfinder.servlets.ConnectorException;
import org.elfinder.servlets.FileActionEnum;
import org.elfinder.servlets.FsException;

/**
 * @author Antoine Walter (www.anw.fr)
 * @date 29 aug. 2011
 * @version $Id$
 * @license BSD
 */
public class RmCommand extends AbstractCommandOverride {

	@SuppressWarnings("unchecked")
	@Override
	public void execute() throws ConnectorException {
		File dirCurrent = getExistingDir(getParam("current"), FileActionEnum.WRITE);
		if (dirCurrent != null) {
			List<String> targets = (List<String>) getParamObject("targets[]");
			if (targets == null || targets.isEmpty()) {
				throw new ConnectorException("Invalid parameters");
			}

			for (String targetHash : targets) {
				File fileTarget = getExistingFile(targetHash, dirCurrent, FileActionEnum.DELETE);
				if (fileTarget == null) {
					throw new ConnectorException("File not found");
				}
				_remove(fileTarget, false);
			}

			_content(dirCurrent, true);
		}
	}

	/**
	 * Remove file or folder (recursively)
	 **/
	protected void _remove(File path, boolean fromRecursive) throws ConnectorException {
		if (path == null || !path.exists()) {
			throw new ConnectorException("Invalid parameters");
		}
		if (!path.isDirectory()) {
			if (!getConfig()._isAllowedExistingDir(path, FileActionEnum.DELETE)) {
				throw new ConnectorException("Access denied");
			}
			try {
				getFs().removeFile(path);
			} catch (FsException e) {
				throw new ConnectorException("Unable to remove file", e);
			}
		} else {
			if (!getConfig()._isAllowedExistingFile(path, FileActionEnum.DELETE)) {
				throw new ConnectorException("Access denied");
			}
			removeDirectory(path, fromRecursive);
		}
	}

	protected void removeDirectory(File path, boolean fromRecursive) throws ConnectorException {
		File[] children = path.listFiles();
		if (children != null) {
			for (File child : children) {
				if (!getConfig().isSpecialDir(child)) {
					_remove(child, true);
				}
			}
		}

		try {
			getFs().removeEmptyDirectory(path);
		} catch (FsException e) {
			throw new ConnectorException("Unable to remove file", e);
		}
	}
}
