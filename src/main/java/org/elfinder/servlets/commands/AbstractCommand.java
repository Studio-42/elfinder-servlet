package org.elfinder.servlets.commands;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.log4j.Logger;
import org.elfinder.servlets.ConnectorException;
import org.elfinder.servlets.FileActionEnum;
import org.elfinder.servlets.config.AbstractConnectorConfig;
import org.elfinder.servlets.fs.IFsImpl;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * This is superclass for each command implemented by the servlet.<br>
 * This is a quick & dirty code translation from PHP connector, probably needs
 * some cleanup...
 * 
 * @author Antoine Walter (www.anw.fr)
 * @date 29 aug. 2011
 * @version $Id$
 * @license BSD
 */
public abstract class AbstractCommand {
	private static Logger logger = Logger.getLogger(AbstractCommand.class);

	private HttpServletRequest request;
	private HttpServletResponse response;
	private JSONObject json;
	private Map<String, Object> requestParameters;
	private List<FileItemStream> listFiles;
	private List<ByteArrayOutputStream> listFileStreams;
	private AbstractConnectorConfig config;
	private boolean isResponseTextHtml = false;
	private PrintWriter out;
	private boolean responseOutputDone = false;
	private boolean forceRunInit = false;

	public AbstractCommand() {
	}

	public void init() {

	}

	public abstract void execute() throws ConnectorException;

	protected HttpServletRequest getRequest() {
		return request;
	}

	protected HttpServletResponse getResponse() {
		return response;
	}

	public void setRequest(HttpServletRequest request) {
		this.request = request;
	}

	public void setResponse(HttpServletResponse response) {
		this.response = response;
	}

	protected String getParam(String paramName) {
		String param = (String) requestParameters.get(paramName);
		if (param != null) {
			param = param.trim();
		}
		return param;
	}

	protected Object getParamObject(String paramName) {
		return requestParameters.get(paramName);
	}

	public void setJson(JSONObject json) {
		this.json = json;
	}

	protected void putResponse(String param, Object value) {
		try {
			json.put(param, value);
		} catch (JSONException e) {
			logger.error("", e);
		}
	}

	protected Object getResponse(String param) {
		try {
			return json.get(param);
		} catch (JSONException e) {
			return null;
		}
	}

	public boolean isResponseOutputDone() {
		return responseOutputDone;
	}

	public void setResponseOutputDone(boolean responseOutputDone) {
		this.responseOutputDone = responseOutputDone;
	}

	public void setRequestParameters(Map<String, Object> requestParameters) {
		this.requestParameters = requestParameters;
	}

	public void setListFiles(List<FileItemStream> listFiles) {
		this.listFiles = listFiles;
	}

	public void setListFileStreams(List<ByteArrayOutputStream> listFileStreams) {
		this.listFileStreams = listFileStreams;
	}

	public void setConfig(AbstractConnectorConfig config) {
		this.config = config;
	}

	protected List<FileItemStream> getListFiles() {
		return listFiles;
	}

	protected List<ByteArrayOutputStream> getListFileStreams() {
		return listFileStreams;
	}

	protected AbstractConnectorConfig getConfig() {
		return config;
	}

	protected IFsImpl getFs() {
		return config.getFs();
	}

	protected void setResponseTextHtml() {
		isResponseTextHtml = true;
	}

	public boolean isResponseTextHtml() {
		return isResponseTextHtml;
	}

	public PrintWriter getResponseWriter() {
		if (out == null) {
			try {
				out = response.getWriter();
			} catch (Exception e) {
				logger.error("", e);
			}
		}
		return out;
	}

	public static void closeWriter(PrintWriter responseWriter) {
		responseWriter.flush();
		responseWriter.close();
	}

	public boolean mustRunInit() {
		return (forceRunInit || request.getParameter("init") != null);
	}

	protected void setForceRunInit(boolean forceRunInit) {
		this.forceRunInit = forceRunInit;
	}

	// ---------

	public void contentCommand() throws ConnectorException {
		File path = getExistingDir(getParam("target"), FileActionEnum.READ);
		if (path == null) {
			path = getRootFile();
		}
		boolean isTree = (getParam("tree") != null);
		_content(path, isTree);
	}

	public void initCommand() throws ConnectorException {
		putResponse("disabled", getConfig().getListDisabled());

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("dotFiles", getConfig().getDotFilesAllowed());
		params.put("uplMaxSize", getConfig().getUploadMaxSize() + "M");
		params.put("archives", getConfig().getListArchives());
		params.put("extract", getConfig().getListExtract());
		params.put("url", getConfig().getRootUrl());
		putResponse("params", params);
	}

	/************************************************************/
	/** config methods **/
	/************************************************************/

	protected String dateFormat(long time) {
		return config.dateFormat(time);
	}

	protected String getMime(File file) {
		return config.getMime(file);
	}

	protected String getMimeDisposition(String mime) {
		String[] parts = mime.split("/");
		String disp = ("image".equals(parts[0]) || "text".equals(parts[0]) ? "inline" : "attachments");
		return disp;
	}

	protected String getFileUrl(File file) {
		return config.getFileUrl(file);
	}

	protected String basename(File path) {
		return config.basename(path);
	}

	protected String getRoot() {
		return config.getRoot();
	}

	protected File getRootFile() {
		return config.getRootFile();
	}

	public boolean _checkName(String n) {
		return config._checkName(n);
	}

	public boolean _isUploadAllow(String fileName) {
		return config._isUploadAllow(fileName);
	}

	public boolean _isAccepted(File file) {
		return config._isAccepted(file);
	}

	public boolean isSpecialDir(File file) {
		return config.isSpecialDir(file);
	}

	/**
	 * For some reasons, some setups have to reencode filename before output to
	 * JSON.<br/>
	 * Let them override it if needed.
	 * 
	 * @param fileName
	 * @return fileName for output
	 */
	protected String encodeFileNameForOutput(String fileName) {
		return config.encodeFileNameForOutput(fileName);
	}

	/************************************************************/
	/** fs methods **/
	/************************************************************/

	/**
	 * Find folder by hash in required folder and subfolders
	 * 
	 * @param string
	 *            $hash folder hash
	 * @param string
	 *            $path folder path to search in
	 * @return string
	 **/
	protected File _findDir(String hash, File path) {
		if (path == null) {
			path = config.getRootFile();
			if (_hash(path).equals(hash)) {
				return path;
			}
		}
		
		File[] children = path.listFiles();
		if (children != null) {
			for (File child : children) {
				if (config._isAccepted(child) && child.isDirectory()) {
					if (_hash(child).equals(hash)) {
						return child;
					}
					File foundDir = _findDir(hash, child);
					if (foundDir != null) {
						return foundDir;
					}
				}
			}
		}
		return null;
	}

	/**
	 * Find file/folder by hash in required folder
	 * 
	 * @param string
	 *            $hash file/folder hash
	 * @param string
	 *            $path folder path to search in
	 **/
	protected File _find(String hash, File path) {
		if (path == null) {
			return null;
		}

		File[] children = path.listFiles();
		if (children != null) {
			for (File child : children) {
				if (config._isAccepted(child)) {
					if (_hash(child).equals(hash)) {
						return child;
					}
				}
			}
		}
		return null;
	}

	protected File getExistingDir(String dirHash, FileActionEnum actionCheck) throws ConnectorException {
		File dir = null;
		if (dirHash != null && !"".equals(dirHash)) {
			dir = _findDir(dirHash, null);

			if (dir == null) {
				throw new ConnectorException("Invalid parameters");
			}

			if (!getConfig()._isAllowedExistingDir(dir, actionCheck)) {
				throw new ConnectorException("Access denied");
			}
		}
		return dir;
	}

	protected File getExistingFile(String fileHash, File existingDir, FileActionEnum actionCheck) throws ConnectorException {
		File existingFile = _find(fileHash, existingDir);
		if (existingFile == null) {
			throw new ConnectorException("File not found");
		}

		if (!getConfig()._isAllowedExistingFile(existingFile, actionCheck)) {
			throw new ConnectorException("Access denied");
		}
		return existingFile;
	}

	protected File getNewFile(String fileName, File existingDir, FileActionEnum actionCheck) throws ConnectorException {
		if (!_checkName(fileName)) {
			throw new ConnectorException("Invalid name");
		}

		File newFile = new File(existingDir, fileName);
		if (newFile.exists()) {
			throw new ConnectorException("File or folder with the same name already exists");
		}

		if (!config._isAllowedNewFile(newFile, actionCheck)) {
			throw new ConnectorException("Access denied");
		}
		return newFile;
	}

	protected File getNewDirectory(String dirName, File existingDir, FileActionEnum actionCheck) throws ConnectorException {
		if (!_checkName(dirName)) {
			throw new ConnectorException("Invalid name");
		}

		File newFile = new File(existingDir, dirName);
		if (newFile.exists()) {
			throw new ConnectorException("File or folder with the same name already exists");
		}

		if (!config._isAllowedNewDir(newFile, actionCheck)) {
			throw new ConnectorException("Access denied");
		}
		return newFile;
	}

	/************************************************************/
	/** image manipulation **/
	/************************************************************/

	/**
	 * Remove image thumbnail
	 * 
	 * @param string
	 *            $img image file
	 * @return void
	 **/
	protected void _rmTmb(File img) {
		// TODO
		// if ($this->_options['tmbDir'] && false != ($tmb =
		// $this->_tmbPath($img)) && file_exists($tmb)) {
		// @unlink($tmb);
		// }
	}

	/************************************************************/
	/** "content" methods **/
	/************************************************************/

	public String getFileBasenameOrRootAlias(File f) {
		String name;
		if (f.getPath().equals(getRootFile().getPath())) {
			name = config.rootAliasOrBaseName();
		} else {
			name = encodeFileNameForOutput(basename(f));
		}
		return name;
	}

	/**
	 * Set current dir info, content and [dirs tree]
	 */
	protected void _content(File path, boolean isTree) {
		_cwd(path);
		_cdc(path);
		if (isTree) {
			File f = new File(config.getRoot());
			putResponse("tree", _tree(f));
		}
	}

	/**
	 * Set current dir info
	 */
	protected void _cwd(File f) {

		String name = getFileBasenameOrRootAlias(f);
		String rel = name;

		Map<String, Object> infos = new HashMap<String, Object>();
		infos.put("hash", _hash(f));
		infos.put("name", name);
		infos.put("mime", "directory");
		infos.put("rel", rel);
		infos.put("size", "0");
		infos.put("date", config.dateFormat(f.lastModified()));
		infos.put("read", true);

		// return true if we have permission for at least one the following
		// actions
		boolean canWriteCurrentDirectoryOrCreateNewFilesOrDirectory = config._isAllowedExistingDir(f, FileActionEnum.WRITE)
				|| config._isAllowedNewDir(f, FileActionEnum.WRITE) || config._isAllowedNewFile(f, FileActionEnum.WRITE);
		infos.put("write", canWriteCurrentDirectoryOrCreateNewFilesOrDirectory);
		infos.put("rm", config._isAllowedExistingDir(f, FileActionEnum.DELETE));
		putResponse("cwd", infos);

	}

	/**
	 * Set current dir content
	 * 
	 * @param string
	 *            path current dir path
	 * @return void
	 **/
	protected void _cdc(File dir) {
		List<Map<String, Object>> infos = new ArrayList<Map<String, Object>>();

		File[] children = dir.listFiles();
		if (children != null) {
			for (File child : children) {
				if (config._isAccepted(child)) {
					Map<String, Object> info = _info(child);
					infos.add(info);
				}
			}
		}
		putResponse("cdc", infos);
	}

	/**
	 * Return file/folder info
	 * 
	 * @param string
	 *            path file path
	 * @return array
	 **/
	protected Map<String, Object> _info(File path) {
		// type = filetype(path);
		// $stat = $type == 'link' ? lstat($path) : stat($path);
		// if ($stat['mtime'] > $this->_today) {
		// $d = 'Today '.date('H:i', $stat['mtime']);
		// } elseif ($stat['mtime'] > $this->_yesterday) {
		// $d = 'Yesterday '.date('H:i', $stat['mtime']);
		// } else {
		// $d = date($this->_options['dateFormat'], $stat['mtime']);
		// }

		boolean isDir = path.isDirectory();
		Map<String, Object> info = new HashMap<String, Object>();
		info.put("name", encodeFileNameForOutput(basename(path)));
		info.put("hash", _hash(path));
		info.put("mime", isDir ? "directory" : config.getMime(path));
		info.put("date", config.dateFormat(path.lastModified()));
		info.put("size", isDir ? getFs().getDirSize(path) : getFs().getFileSize(path));
		info.put("read", isDir ? config._isAllowedExistingDir(path, FileActionEnum.READ) : config._isAllowedExistingFile(path, FileActionEnum.READ));
		info.put("write",isDir ? config._isAllowedExistingDir(path, FileActionEnum.WRITE) : config._isAllowedExistingFile(path, FileActionEnum.WRITE));
		info.put("rm", isDir ? config._isAllowedExistingDir(path, FileActionEnum.DELETE) : config._isAllowedExistingFile(path, FileActionEnum.WRITE));

		if (!isDir) {
			if (config.isFileUrlEnabled() && true == (Boolean) info.get("read")) {
				info.put("url", encodeFileNameForOutput(config.getFileUrl(path)));
			}
			
			if(config.hasThumbnail(path)) {
				info.put("tmb", encodeFileNameForOutput(config.getThumbnailUrl(path)));
			}

			// if ($this->_options['fileURL'] && $info['read']) {
			// $info['url'] = $this->_path2url($lpath ? $lpath : $path);
			// }

			// if (0 === ($p = strpos($info['mime'], 'image'))) {
			// if (false != ($s = getimagesize($path))) {
			// $info['dim'] = $s[0].'x'.$s[1];
			// }
			// if ($info['read']) {
			// $info['resize'] = isset($info['dim']) &&
			// $this->_canCreateTmb($info['mime']);
			// $tmb = $this->_tmbPath($path);
			//
			// if (file_exists($tmb)) {
			// $info['tmb'] = $this->_path2url($tmb);
			// } elseif ($info['resize']) {
			// $this->_result['tmb'] = true;
			// }
			//
			// }
			// }
		}
		return info;
	}

	/**
	 * Return directory tree (multidimensional array)
	 * 
	 * @param string
	 *            path directory path
	 * @return array
	 **/
	protected Map<String, Object> _tree(File path) {
		Map<String, Object> info = new HashMap<String, Object>();
		info.put("hash", _hash(path));
		info.put("name", getFileBasenameOrRootAlias(path));
		info.put("read", config._isAllowedExistingDir(path, FileActionEnum.READ));

		// we return true if we have permission for at least one the following
		// actions
		boolean canWriteCurrentDirectoryOrCreateNewFilesOrDirectory = config._isAllowedExistingDir(path, FileActionEnum.WRITE)
				|| config._isAllowedNewDir(path, FileActionEnum.WRITE) || config._isAllowedNewFile(path, FileActionEnum.WRITE);
		info.put("write", canWriteCurrentDirectoryOrCreateNewFilesOrDirectory);

		List<Object> dirs = new ArrayList<Object>();
		if (true == (Boolean) info.get("read")) {
			File[] children = path.listFiles();
			if (children != null) {
				for (File child : children) {
					if (child.isDirectory()) {
						if (config._isAllowedExistingDir(child, FileActionEnum.READ)) {
							dirs.add(_tree(child));
						}
					}
				}
			}
		}
		info.put("dirs", dirs);
		return info;
	}

	/************************************************************/
	/** utilites **/
	/************************************************************/

	/**
	 * Return file path hash
	 * 
	 * @param string
	 *            path
	 * @return string
	 **/
	protected String _hash(File path) {
		String hash = DigestUtils.md5Hex(path.getAbsolutePath());
		return hash;
	}

}
