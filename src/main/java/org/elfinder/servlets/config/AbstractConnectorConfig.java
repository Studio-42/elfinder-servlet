package org.elfinder.servlets.config;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.activation.MimetypesFileTypeMap;

import org.elfinder.servlets.FileActionEnum;
import org.elfinder.servlets.fs.IFsImpl;

import eu.medsea.mimeutil.MimeType;
import eu.medsea.mimeutil.MimeUtil;

/**
 * @author Antoine Walter (www.anw.fr)
 * @date 29 aug. 2011
 * @version $Id$
 * @license BSD
 */

/**
 * Parent class for connector configuration.<br>
 * This class must be extended and maybe overriden for your needs.<br>
 * Ideally it should be an interface, I didn't find time for refactoring...<br>
 */
public abstract class AbstractConnectorConfig {
	private static final String[] allowedCommands = new String[] { "content", "init", "mkdir", "mkfile", "open", "rename", "rm", "upload", "paste" };

	static {
		// MIME types initialization
		MimeUtil.registerMimeDetector("eu.medsea.mimeutil.detector.MagicMimeMimeDetector");
	}

	/**
	 * Constructor.
	 */
	public AbstractConnectorConfig() {
	}

	/**
	 * Returns the filesystem to use.
	 * @return
	 */
	public abstract IFsImpl getFs();

	/**
	 * Get root directory, where ElFinder files and directories will be stored.
	 * @return
	 */
	public abstract String getRoot();

	/**
	 * Get URL of root directory.
	 * @return
	 */
	public abstract String getRootUrl();

	/**
	 * Getting URL of a file.
	 * @param file
	 * @return
	 */
	public abstract String getFileUrl(File file);
	
	/**
	 * Get URL of file thumnail.
	 * @return
	 */
	public abstract String getThumbnailUrl(File path);
	
	/**
	 * Returns true if there is a thumbnail to display for the file.
	 * @return
	 */
	public abstract boolean hasThumbnail(File path);

	/**
	 * Returns root alias, or null to use actual root folder name.
	 * 
	 * @return root alias, or null to use actual root folder name
	 */
	private String getRootAlias() {
		return null;
	}

	/**
	 * This can be used for restricting commands access.
	 * @param commandStr
	 * @return
	 */
	public boolean isCommandAllowed(String commandStr) {
		//return ArrayUtils.contains(allowedCommands, commandStr);
		return true;
	}

	/**
	 * Just a common implementation of basic permission check, allowing to do everything on all files except deleting the root directory.
	 * @param dir
	 * @param action
	 * @return
	 */
	protected boolean isAllowedCommon(File dir, FileActionEnum action) {
		// make sure this is not the root directory
		if (dir.getPath().equals(getRootFile().getPath())) {
			if (action.equals(FileActionEnum.DELETE)) {
				return false;
			}
		}

		// additional checks from file system
		getFs().isAllowedFile(dir);
		return true;
	}

	/**
	 * Restricts actions performed on an existing directory.
	 * @param dir
	 * @param action
	 * @return
	 */
	public boolean _isAllowedExistingDir(File dir, FileActionEnum action) {
		return isAllowedCommon(dir, action);
	}

	/**
	 * Restricts actions performed on an existing file.
	 * @param dir
	 * @param action
	 * @return
	 */
	public boolean _isAllowedExistingFile(File dir, FileActionEnum action) {
		return isAllowedCommon(dir, action);
	}

	/**
	 * Restricts actions performed on a new directory.
	 * @param dir
	 * @param action
	 * @return
	 */
	public boolean _isAllowedNewDir(File dir, FileActionEnum action) {
		return isAllowedCommon(dir, action);
	}

	/**
	 * Restricts actions performed on a new file.
	 * @param dir
	 * @param action
	 * @return
	 */
	public boolean _isAllowedNewFile(File dir, FileActionEnum action) {
		return isAllowedCommon(dir, action);
	}

	/**
	 * Format used to display dates.
	 * @return
	 */
	protected String getDateFormatPattern() {
		return "MM/dd/yyyy HH:mm:ss";
	}

	/**
	 * Formatting a date.
	 * @param time
	 * @return
	 */
	public String dateFormat(long time) {
		SimpleDateFormat format = new SimpleDateFormat(getDateFormatPattern());
		return format.format(time);
	}

	/**
	 * Getting MIME type of a file.
	 * @param file
	 * @return
	 */
	public String getMime(File file) {
		Collection<MimeType> mimes = MimeUtil.getMimeTypes(file);
		if (!mimes.isEmpty()) {
			return mimes.iterator().next().toString();
		}
		// not found try the second method...
		return new MimetypesFileTypeMap().getContentType(file);
	}

	/**
	 * Getting relative path of a file.
	 * @param file
	 * @return
	 */
	public String getRelativePath(File path) {
		String url = null;
		File currentPath = path;
		while (!currentPath.getPath().equals(getRootFile().getPath())) {
			if (url == null) {
				url = basename(currentPath);
			} else {
				url = basename(currentPath) + "/" + url;
			}

			currentPath = currentPath.getParentFile();
		}
		return url;
	}

	/**
	 * Old stuff coming from original PHP connector.
	 * @param path
	 * @return
	 */
	public String basename(File path) {
		// $path == $this->_options['root'] && $this->_options['rootAlias'] ? $this->_options['rootAlias'] : basename($path),
		return path.getName();
	}

	public String rootAliasOrBaseName() {
		String rootAlias = getRootAlias();
		if (rootAlias != null) {
			return rootAlias;
		}
		return basename(getRootFile());
	}

	public File getRootFile() {
		return new File(getRoot());
	}

	public boolean isFileUrlEnabled() {
		return true;
	}

	/************************************************************/
	/** init methods **/
	/************************************************************/

	public List<String> getListDisabled() {
		return new ArrayList<String>();
	}

	public boolean getDotFilesAllowed() {
		return true;
	}

	/**
	 * Max upload size in MB per file.
	 * 
	 * @return Max upload size in MB per file
	 */
	public long getUploadMaxSize() {
		return 50;
	}

	/**
	 * Max upload size in MB for all files.
	 * 
	 * @return Max upload size in MB for all files
	 */
	public long getUploadMaxSizeTotal() {
		return 1000;
	}

	public List<String> getListArchives() {
		return new ArrayList<String>();
	}

	public List<String> getListExtract() {
		return new ArrayList<String>();
	}

	/************************************************************/
	/** fs methods **/
	/************************************************************/

	/**
	 * Check new file name for invalid simbols. Return name if valid
	 * 
	 * @return string $n file name
	 * @return string
	 **/
	public boolean _checkName(String n) {
		if (n == null) {
			return false;
		}
		n = n.trim();
		if ("".equals(n)) {
			return false;
		}

		//n = strip_tags(trim(n.trim()));
		//		if (!$this->_options['dotFiles'] && '.' == substr($n, 0, 1)) {
		//			return false;
		//		}
		return n.matches("|^[^\\\\/\\<\\>:]+$|");
	}

	/************************************************************/
	/** access control **/
	/************************************************************/

	/**
	 * Return true if file's mimetype is allowed for upload
	 * 
	 * @param string $name file name
	 * @param string $tmpName uploaded file tmp name
	 * @return bool
	 **/
	public boolean _isUploadAllow(String fileName) {
		//		$mime  = $this->_mimetype($this->_options['mimeDetect'] != 'internal' ? $tmpName : $name);
		//		$allow = false;
		//		$deny  = false;
		//
		//		if (in_array('all', $this->_options['uploadAllow'])) {
		//			$allow = true;
		//		} else {
		//			foreach ($this->_options['uploadAllow'] as $type) {
		//				if (0 === strpos($mime, $type)) {
		//					$allow = true;
		//					break;
		//				}
		//			}
		//		}
		//		
		//		if (in_array('all', $this->_options['uploadDeny'])) {
		//			$deny = true;
		//		} else {
		//			foreach ($this->_options['uploadDeny'] as $type) {
		//				if (0 === strpos($mime, $type)) {
		//					$deny = true;
		//					break;
		//				}
		//			}
		//		}
		//		return 0 === strpos($this->_options['uploadOrder'], 'allow') ? $allow && !$deny : $allow || !$deny;

		return true;
	}

	/**
	 * Return true if file name is not . or .. If file name begins with . return value according to $this->_options['dotFiles']
	 * 
	 * @param string $file file name
	 * @return bool
	 **/
	public boolean _isAccepted(File file) {
		if (isSpecialDir(file)) {
			return false;
		}
		//		if (!$this->_options['dotFiles'] && '.' == substr($file, 0, 1)) {
		//			return false;
		//		}
		return true;
	}

	/**
	 * Return true if file name is not . or .. If file name begins with . return value according to $this->_options['dotFiles']
	 * 
	 * @param string $file file name
	 * @return bool
	 **/
	public boolean isSpecialDir(File file) {
		String fileName = file.getName();
		return (".".equals(fileName) || "..".equals(fileName));
	}

	/**
	 * For some reasons, some setups have to reencode filename before output to JSON.<br/>
	 * Let them override it if needed.
	 * 
	 * @param fileName
	 * @return fileName for output
	 */
	public String encodeFileNameForOutput(String fileName) {
		return fileName;
	}

}
