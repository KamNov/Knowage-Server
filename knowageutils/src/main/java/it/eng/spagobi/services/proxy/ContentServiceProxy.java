/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2021 Engineering Ingegneria Informatica S.p.A.
 *
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.eng.spagobi.services.proxy;

import java.net.URL;
import java.util.HashMap;

import javax.servlet.http.HttpSession;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import org.apache.log4j.Logger;

import it.eng.spagobi.services.common.ParametersWrapper;
import it.eng.spagobi.services.content.ContentService;
import it.eng.spagobi.services.content.bo.Content;
import it.eng.spagobi.services.security.exceptions.SecurityException;

/**
 *
 * Proxy of Content Service
 *
 */
public final class ContentServiceProxy extends AbstractServiceProxy {

	private static final String SERVICE_NAME = "Content Service";

	private static final QName SERVICE_QNAME = new QName("http://content.services.spagobi.eng.it/", "ContentService");

	private static Logger logger = Logger.getLogger(ContentServiceProxy.class);

	/**
	 * use this i engine context only.
	 *
	 * @param user    user ID
	 * @param session http session
	 */
	public ContentServiceProxy(String user, HttpSession session) {
		super(user, session);
		if (user == null)
			logger.error("User ID IS NULL....");
		if (session == null)
			logger.error("HttpSession IS NULL....");
	}

	private ContentServiceProxy() {
		super();
	}

	private ContentService lookUp() throws SecurityException {
		try {
			ContentService service = null;

			if (serviceUrl != null) {
				URL serviceUrlWithWsdl = new URL(serviceUrl.toString() + "?wsdl");

				service = Service.create(serviceUrlWithWsdl, SERVICE_QNAME).getPort(ContentService.class);
			} else {
				service = Service.create(SERVICE_QNAME).getPort(ContentService.class);
			}

			setCommonHeader(service);

			return service;
		} catch (Exception e) {
			logger.error("Impossible to locate [" + SERVICE_NAME + "] at [" + serviceUrl + "]");
			throw new SecurityException("Impossible to locate [" + SERVICE_NAME + "] at [" + serviceUrl + "]", e);
		}
	}

	/**
	 * Read template.
	 *
	 * @param document String
	 *
	 * @return Content
	 */
	public Content readMap(String mapName) {
		logger.debug("IN.mapName=" + mapName);
		if (mapName == null || mapName.length() == 0) {
			logger.error("mapName is NULL");
			return null;
		}
		try {
			return lookUp().readMap(readTicket(), userId, mapName);
		} catch (Exception e) {
			logger.error("Error during service execution", e);

		} finally {
			logger.debug("OUT");
		}
		return null;
	}

	/**
	 * Read template.
	 *
	 * @param document String
	 *
	 * @return Content
	 */
	public Content readTemplate(String document, HashMap attributes) {
		logger.debug("IN.document=" + document);
		if (document == null || document.length() == 0) {
//	    logger.error("Documenti ID is NULL");
			return null;
		}
		try {
			ParametersWrapper _attributes = new ParametersWrapper();
			_attributes.getMap().putAll(attributes);
			return lookUp().readTemplate(readTicket(), userId, document, _attributes);
		} catch (Exception e) {
			logger.error("Error during service execution", e);

		} finally {
			logger.debug("OUT");
		}
		return null;
	}

	/**
	 * Read template by label.
	 *
	 * @param document String
	 *
	 * @return Content
	 */
	public Content readTemplateByLabel(String label, HashMap attributes) {
		logger.debug("IN.document=" + label);
		if (label == null || label.length() == 0) {
			logger.error("Documenti Label is NULL");
			return null;
		}
		try {
			ParametersWrapper _attributes = new ParametersWrapper();
			_attributes.getMap().putAll(attributes);
			return lookUp().readTemplateByLabel(readTicket(), userId, label, _attributes);
		} catch (Exception e) {
			logger.error("Error during service execution", e);

		} finally {
			logger.debug("OUT");
		}
		return null;
	}

	/**
	 * Publish template.
	 *
	 * @param attributes HashMap
	 *
	 * @return String
	 */
	public String publishTemplate(HashMap attributes) {
		logger.debug("IN");
		if (attributes == null) {
			logger.error("attributes is NULL");
			return null;
		}
		try {
			ParametersWrapper _attributes = new ParametersWrapper();
			_attributes.getMap().putAll(attributes);
			return lookUp().publishTemplate(readTicket(), userId, _attributes);
		} catch (Exception e) {
			logger.error("Error during service execution", e);

		} finally {
			logger.debug("OUT");
		}
		return null;
	}

	/**
	 * Map catalogue.
	 *
	 * @param operation   String
	 * @param path        String
	 * @param featureName String
	 * @param mapName     String
	 *
	 * @return String
	 */
	public String mapCatalogue(String operation, String path, String featureName, String mapName) {
		logger.debug("IN");
		if (operation == null || operation.length() == 0) {
			logger.error("operation is NULL");
			return null;
		}
		try {
			return lookUp().mapCatalogue(readTicket(), userId, operation, path, featureName, mapName);
		} catch (Exception e) {
			logger.error("Error during service execution", e);

		} finally {
			logger.debug("OUT");
		}
		return null;
	}

	/**
	 * Read sub object content.
	 *
	 * @param nameSubObject String
	 *
	 * @return Content
	 */
	public Content readSubObjectContent(String nameSubObject) {
		logger.debug("IN.nameSubObject=" + nameSubObject);
		if (nameSubObject == null || nameSubObject.length() == 0) {
			logger.error("SubObject is NULL");
			return null;
		}
		try {
			return lookUp().readSubObjectContent(readTicket(), userId, nameSubObject);
		} catch (Exception e) {
			logger.error("Error during service execution", e);

		} finally {
			logger.debug("OUT");
		}
		return null;
	}

	/**
	 * Read sub object content.
	 *
	 * @param nameSubObject
	 * @param objId
	 * @return
	 * @throws java.rmi.RemoteException
	 */
	public Content readSubObjectContent(String nameSubObject, Integer objId) throws java.rmi.RemoteException {
		logger.debug("IN.nameSubObject=" + nameSubObject);
		if (nameSubObject == null || nameSubObject.length() == 0) {
			logger.error("SubObject is NULL");
			return null;
		}
		try {
			return lookUp().readSubObjectContentByObjId(readTicket(), userId, nameSubObject, objId);
		} catch (Exception e) {
			logger.error("Error during service execution", e);

		} finally {
			logger.debug("OUT");
		}
		return null;
	}

	/**
	 * Save sub object.
	 *
	 * @param documentiId         String
	 * @param analysisName        String
	 * @param analysisDescription String
	 * @param visibilityBoolean   String
	 * @param content             String
	 *
	 * @return String
	 */
	public String saveSubObject(String documentiId, String analysisName, String analysisDescription, String visibilityBoolean, String content) {
		logger.debug("IN.documentiId=" + documentiId);
		if (documentiId == null || documentiId.length() == 0) {
			logger.error("documentiId is NULL");
			return null;
		}
		try {
			return lookUp().saveSubObject(readTicket(), userId, documentiId, analysisName, analysisDescription, visibilityBoolean, content);
		} catch (Exception e) {
			logger.error("Error during service execution", e);

		} finally {
			logger.debug("OUT");
		}
		return null;
	}

	/**
	 * Save object template.
	 *
	 * @param documentiId  String
	 * @param templateName String
	 * @param content      String
	 *
	 * @return String
	 */
	public String saveObjectTemplate(String documentiId, String templateName, String content) {
		logger.debug("IN.documentiId=" + documentiId);
		if (documentiId == null || documentiId.length() == 0) {
			logger.error("documentiId is NULL");
			return null;
		}
		if (templateName == null || templateName.length() == 0) {
			logger.error("templateName is NULL");
			return null;
		}
		if (content == null || content.length() == 0) {
			logger.warn("templateName is NULL");
		}
		try {
			return lookUp().saveObjectTemplate(readTicket(), userId, documentiId, templateName, content);
		} catch (Exception e) {
			logger.error("Error during service execution", e);

		} finally {
			logger.debug("OUT");
		}
		return null;
	}

	/**
	 * Download all.
	 *
	 * @param biobjectId String
	 * @param fileName   String
	 *
	 * @return String
	 */
	public Content downloadAll(String biobjectId, String fileName) {
		logger.debug("IN");
		if (biobjectId == null || biobjectId.length() == 0) {
			logger.error("biobjectId is NULL");
			return null;
		}
		if (fileName == null || fileName.length() == 0) {
			logger.error("fileName is NULL");
			return null;
		}
		try {
			return lookUp().downloadAll(readTicket(), userId, biobjectId, fileName);
		} catch (Exception e) {
			logger.error("Error during service execution", e);

		} finally {
			logger.debug("OUT");
		}
		return null;
	}

}
