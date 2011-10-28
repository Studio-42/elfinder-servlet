package org.elfinder.servlets.commands;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.log4j.Logger;
import org.elfinder.servlets.ConnectorException;
import org.elfinder.servlets.FileActionEnum;

/**
 * @author Antoine Walter (www.anw.fr)
 * @date 29 aug. 2011
 * @version $Id$
 * @license BSD
 */
public class OpenCommand extends AbstractCommandOverride {
	private static Logger logger = Logger.getLogger(OpenCommand.class);

	@Override
	public void execute() throws ConnectorException {
		File fileCurrent = getExistingDir(getParam("current"), FileActionEnum.READ);
		if (fileCurrent != null) {
			File fileTarget = getExistingFile(getParam("target"), fileCurrent, FileActionEnum.READ);
			serveFile(fileTarget);
		} else {
			contentCommand();
		}
	}

	protected void serveFile(File fileTarget) throws ConnectorException {
		//		if (filetype($file) == 'link') {
		//				$file = $this->_readlink($file);
		//				if (!$file || is_dir($file)) {
		//					header('HTTP/1.x 404 Not Found'); 
		//					exit('File not found');
		//				}
		//				if (!$this->_isAllowed(dirname($file), 'read') || !$this->_isAllowed($file, 'read')) {
		//					header('HTTP/1.x 403 Access Denied'); 
		//					exit('Access denied');
		//				}
		//			}

		String mime = getMime(fileTarget);
		String disp = getMimeDisposition(mime);

		getResponse().setContentType(mime);
		String fileUrl = getFileUrl(fileTarget);
		String fileUrlRelative = getFileUrl(fileTarget);
		getResponse().setHeader("Content-Disposition", disp + "; filename=" + fileUrl);
		getResponse().setHeader("Content-Location", fileUrlRelative);
		getResponse().setHeader("Content-Transfer-Encoding", "binary");
		getResponse().setHeader("Connection", "close");

		InputStream is = null;
		try {
			// serve file
			is = new FileInputStream(fileTarget);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			byte[] b = new byte[500];
			int nb;
			while ((nb = is.read(b)) > 0) {
				getResponseWriter().write(nb);
				baos.write(b, 0, nb);
			}
			b = baos.toByteArray();
			getResponse().setContentLength(b.length);

			closeWriter(getResponseWriter());
			setResponseOutputDone(true);
		} catch (Exception e) {
			logger.error("", e);
			throw new ConnectorException("Unknown error");
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {

				}
			}
		}
	}
}
