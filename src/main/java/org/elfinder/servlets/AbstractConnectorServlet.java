/*
 * FCKeditor - The text editor for internet Copyright (C) 2003-2005 Frederico Caldeira Knabben Licensed under the terms of the GNU Lesser General Public
 * License: http://www.opensource.org/licenses/lgpl-license.php For further information visit: http://www.fckeditor.net/ File Name: ConnectorServlet.java Java
 * Connector for Resource Manager class. Version: 2.3 Modified: 2005-08-11 16:29:00 File Authors: Simone Chiaretta (simo@users.sourceforge.net)
 */

package org.elfinder.servlets;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.elfinder.servlets.commands.AbstractCommand;
import org.elfinder.servlets.commands.ContentCommand;
import org.elfinder.servlets.commands.OpenCommand;
import org.elfinder.servlets.config.AbstractConnectorConfig;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Antoine Walter (www.anw.fr)
 * @date 29 aug. 2011
 * @version $Id$
 * @license BSD
 */

/**
 * This abstract servlet acts as controler:<br>
 * - receives requests from ElFinder client<br>
 * - retrieves appropriate configuration<br>
 * - instanciates command implementation and executes it<br>
 * - returns json or HTML response<br>
 * <br>
 * <br>
 * This class is *abstract*, you have to implement prepareConfig() so that the servlet will fit to your needs.<br>
 * This way, a single servlet can use multiple configurations and be used as backend of multiple ElFinder clients.<br>
 * <br>
 * Commands are implemented into the same package, in example: org.elfinder.servlets.commands.MkdirCommand<br>
 * Each command can be extended and overriden for specific needs. All you have to do is to create an new class with the following naming convention:<br>
 * => org.elfinder.servlets.commands.MkdirCommandOverride overrides org.elfinder.servlets.commands.MkdirCommand<br>
 */
public abstract class AbstractConnectorServlet extends HttpServlet {

	private static Logger logger = Logger.getLogger(AbstractConnectorServlet.class);

	/**
	 * JSON response.
	 */
	private JSONObject json;
	
	/**
	 * Parameters read from request.
	 */
	private Map<String, Object> requestParams;
	
	/**
	 * Files read from request.
	 */
	private List<FileItemStream> listFiles;
	
	/**
	 * Files data read from request.
	 */
	private List<ByteArrayOutputStream> listFileStreams;

	/**
	 * This function must be implemented to return appropriate configuration.<br>
	 * A single servlet can manage multiple configurations and be used as backend of multiple ElFinder clients.
	 * @param request
	 * @return
	 * @throws Exception
	 */
	protected abstract AbstractConnectorConfig prepareConfig(HttpServletRequest request) throws Exception;

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		processRequest(request, response);
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		processRequest(request, response);
	}

	/**
	 * Processing a new request from ElFinder client.
	 * @param request
	 * @param response
	 */
	protected void processRequest(HttpServletRequest request, HttpServletResponse response) {
		if (logger.isDebugEnabled()) {
			logger.debug("processing request: " + request.getRequestURI() + request.getQueryString());
		}

		// important: set encoding BEFORE writing anything in the response...
		response.setCharacterEncoding("UTF-8");
		//HttpUtils.noCache(response);

		// parse request parameters and files...
		parseRequest(request, response);

		json = new JSONObject();

		try {
			// set configuration
			AbstractConnectorConfig config = prepareConfig(request);
			if (config == null) {
				throw new Exception("Configuration problem");
			}

			// prepare command and run
			AbstractCommand command = prepareCommand((String) requestParams.get("cmd"), request, response, config);
			try {
				command.execute();
			} catch (ConnectorException e) {
				logger.warn("command returned an error", e);
				putResponse("error", e.getMessage());
			}

			// append init info if needed
			if (command.mustRunInit()) {
				try {
					command.initCommand();
				} catch (ConnectorException e) {
					logger.warn("command returned an error", e);
					putResponse("error", e.getMessage());
				}
			}

			// output if command didn't do it
			if (!command.isResponseOutputDone()) {
				output(response, command.isResponseTextHtml(), json, command.getResponseWriter());
				command.setResponseOutputDone(true);
			}
		} catch (Exception e) {
			logger.error("Unknown error", e);
			putResponse("error", "Unknown error");

			// output the error
			try {
				output(response, false, json, response.getWriter());
			} catch (Exception ee) {
				logger.error("", ee);
			}
		}

		// close streams
		if (listFileStreams != null) {
			for (ByteArrayOutputStream os : listFileStreams) {
				try {
					os.close();
				} catch (Exception e) {}
			}
		}
	}

	protected static void output(HttpServletResponse response, boolean isResponseTextHtml, JSONObject json, PrintWriter responseWriter) {
		// encoding was already set by servlet
		if (isResponseTextHtml) {
			response.setContentType("text/html; charset=UTF-8");
		} else {
			response.setContentType("application/json; charset=UTF-8");
		}

		try {
			json.write(responseWriter);
		} catch (Exception e) {
			logger.error("", e);
		}

		AbstractCommand.closeWriter(responseWriter);
	}

	/**
	 * Parse request parameters and files.
	 * @param request
	 * @param response
	 */
	protected void parseRequest(HttpServletRequest request, HttpServletResponse response) {
		requestParams = new HashMap<String, Object>();
		listFiles = new ArrayList<FileItemStream>();
		listFileStreams = new ArrayList<ByteArrayOutputStream>();

		// Parse the request
		if (ServletFileUpload.isMultipartContent(request)) {
			// multipart request
			try {
				ServletFileUpload upload = new ServletFileUpload();
				FileItemIterator iter = upload.getItemIterator(request);
				while (iter.hasNext()) {
					FileItemStream item = iter.next();
					String name = item.getFieldName();
					InputStream stream = item.openStream();
					if (item.isFormField()) {
						requestParams.put(name, Streams.asString(stream));
					} else {
						String fileName = item.getName();
						if (fileName != null && !"".equals(fileName.trim())) {
							listFiles.add(item);

							ByteArrayOutputStream os = new ByteArrayOutputStream();
							IOUtils.copy(stream, os);
							listFileStreams.add(os);
						}
					}
				}
			} catch (Exception e) {
				logger.error("Unexpected error parsing multipart content", e);
			}
		} else {
			// not a multipart
			for (Object mapKey : request.getParameterMap().keySet()) {
				String mapKeyString = (String) mapKey;

				if (mapKeyString.endsWith("[]")) {
					// multiple values
					String values[] = request.getParameterValues(mapKeyString);
					List<String> listeValues = new ArrayList<String>();
					for (String value : values) {
						listeValues.add(value);
					}
					requestParams.put(mapKeyString, listeValues);
				} else {
					// single value
					String value = request.getParameter(mapKeyString);
					requestParams.put(mapKeyString, value);
				}
			}
		}
	}

	/**
	 * Instanciate command implementation and prepare it before execution.
	 * @param commandStr
	 * @param request
	 * @param response
	 * @param config
	 * @return
	 */
	protected AbstractCommand prepareCommand(String commandStr, HttpServletRequest request, HttpServletResponse response, AbstractConnectorConfig config) {
		if (commandStr != null) {
			commandStr = commandStr.trim();
		}

		if (commandStr == null && "POST".equals(request.getMethod())) {
			putResponse("error", "Data exceeds the maximum allowed size");
		}

		if (!config.isCommandAllowed(commandStr)) {
			putResponse("error", "Permission denied");
		}

		AbstractCommand command = null;
		if (commandStr != null) {
			command = instanciateCommand(commandStr);
			if (command == null) {
				putResponse("error", "Unknown command");
			}
		} else {
			String current = (String) request.getParameterMap().get("current");
			if (current != null) {
				command = new OpenCommand();
			} else {
				command = new ContentCommand();
			}
		}

		command.setRequest(request);
		command.setResponse(response);
		command.setJson(json);
		command.setRequestParameters(requestParams);
		command.setListFiles(listFiles);
		command.setListFileStreams(listFileStreams);
		command.setConfig(config);

		command.init();

		return command;
	}

	/**
	 * Instanciate a command from its name.
	 * @param commandName
	 * @return
	 */
	protected AbstractCommand instanciateCommand(String commandName) {
		AbstractCommand instance = null;
		try {
			Class<AbstractCommand> clazz = getCommandClass(commandName);
			if (clazz != null) {
				instance = clazz.newInstance();
				if (instance == null) {
					throw new Exception("Command not found : " + commandName);
				}
			}
		} catch (Exception e) {
			// instance will be null
			logger.error("Could not instanciate connector configuration", e);
		}
		return instance;
	}

	/**
	 * Get command class for a command name.
	 * @param commandName
	 * @return
	 */
	protected Class<AbstractCommand> getCommandClass(String commandName) {
		// do we have override for command?
		Class<AbstractCommand> clazz = getCommandClassOverride(commandName);
		if (clazz == null) {
			// no override, use the default command
			clazz = getCommandClassDefault(commandName);
		}
		return clazz;
	}

	/**
	 * Get default implementation class for a command.
	 * @param commandName
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected Class<AbstractCommand> getCommandClassDefault(String commandName) {
		String className = AbstractConnectorServlet.class.getPackage().getName() + ".commands." + StringUtils.capitalize(commandName) + "Command";
		Class<AbstractCommand> clazz = null;
		try {
			clazz = (Class<AbstractCommand>) Class.forName(className);
		} catch (ClassNotFoundException e) {
			// not found
		}
		return clazz;
	}

	/**
	 * Get override implementation class for a command.
	 * @param commandName
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected Class<AbstractCommand> getCommandClassOverride(String commandName) {
		String className = AbstractConnectorServlet.class.getPackage().getName() + ".commands." + StringUtils.capitalize(commandName) + "CommandOverride";
		Class<AbstractCommand> clazz = null;
		try {
			clazz = (Class<AbstractCommand>) Class.forName(className);
		} catch (ClassNotFoundException e) {
			// not found
		}
		return clazz;
	}

	/**
	 * Append data to JSON response.
	 * @param param
	 * @param value
	 */
	protected void putResponse(String param, Object value) {
		try {
			json.put(param, value);
		} catch (JSONException e) {
			logger.error("json write error", e);
		}
	}
}
