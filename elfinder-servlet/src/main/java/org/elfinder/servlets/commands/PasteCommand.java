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
public class PasteCommand extends AbstractCommandOverride {

	@SuppressWarnings("unchecked")
	@Override
	public void execute() throws ConnectorException {
		File dirCurrent = getExistingDir(getParam("current"), FileActionEnum.READ);
		if (dirCurrent != null) {
			File dirSrc = getExistingDir(getParam("src"), FileActionEnum.READ);
			if (dirSrc != null) {

				File dirDst = getExistingDir(getParam("dst"), FileActionEnum.READ);
				if (dirDst != null) {

					List<String> targets = (List<String>) getParamObject("targets[]");
					if (targets == null || targets.isEmpty()) {
						throw new ConnectorException("Invalid parameters");
					}

					boolean cut = "1".equals(getParam("cut"));

					for (String targetHash : targets) {
						File fileTarget = getExistingFile(targetHash, dirSrc, FileActionEnum.READ);
						if (fileTarget == null) {
							_content(dirCurrent, true);
							throw new ConnectorException("File not found");
						}

						if (dirSrc.getAbsolutePath().equals(dirDst.getAbsolutePath())) {
							_content(dirCurrent, true);
							throw new ConnectorException("Unable to copy into itself");
						}

						File futureFile = getNewFile(basename(fileTarget), dirDst, FileActionEnum.WRITE);

						if (cut) {
							// moving file...
							if (!getConfig()._isAllowedExistingFile(fileTarget, FileActionEnum.DELETE)) {
								throw new ConnectorException("Access denied");
							}

							try {
								getFs().renameFileOrDirectory(fileTarget, futureFile);
								// TODO
								//								if (!is_dir($f)) {
								//									$this->_rmTmb($f);
								//								}
							} catch (FsException e) {
								_content(dirCurrent, true);
								throw new ConnectorException("Unable to move files");
							}
						} else {
							// copying file...
							try {
								getFs().copyFileOrDirectory(fileTarget, futureFile);
							} catch (FsException e) {
								_content(dirCurrent, true);
								throw new ConnectorException("Unable to copy files");
							}
						}
					}
				}
			}
			_content(dirCurrent, true);
		}
	}
}
