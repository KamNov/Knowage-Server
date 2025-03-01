/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
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
package it.eng.spagobi.analiticalmodel.execution.service;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.CharsetDecoder;
import java.util.Date;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.serializer.MetadataJSONSerializer;
import it.eng.spagobi.commons.services.AbstractSpagoBIAction;
import it.eng.spagobi.tools.objmetadata.bo.ObjMetacontent;
import it.eng.spagobi.tools.objmetadata.dao.IObjMetacontentDAO;
import it.eng.spagobi.utilities.engines.SpagoBIEngineServiceException;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;
import it.eng.spagobi.utilities.service.JSONAcknowledge;

/**
 *
 * @author Zerbetto Davide
 *
 */
public class SaveMetadataAction extends AbstractSpagoBIAction {

	public static final String SERVICE_NAME = "SAVE_METADATA_ACTION";

	// REQUEST PARAMETERS
	public static final String OBJECT_ID = "OBJECT_ID";
	public static final String SUBOBJECT_ID = "SUBOBJECT_ID";
	public static final String METADATA = "METADATA";

	// logger component
	private static Logger logger = Logger.getLogger(SaveMetadataAction.class);

	@Override
	public void doService() {
		logger.debug("IN");
		try {
			IObjMetacontentDAO dao=DAOFactory.getObjMetacontentDAO();
			dao.setUserProfile(getUserProfile());
			Integer biobjectId = this.getAttributeAsInteger(OBJECT_ID);
			logger.debug("Object id = " + biobjectId);
			Integer subobjectId = null;
			try {
				subobjectId = this.getAttributeAsInteger(SUBOBJECT_ID);
			} catch (NumberFormatException e) {}
			logger.debug("Subobject id = " + subobjectId);
			String jsonEncodedMetadata = getAttributeAsString( METADATA );
			try {
				CharsetDecoder decoder=UTF_8.newDecoder();
				jsonEncodedMetadata=decoder.decode(ByteBuffer.wrap(jsonEncodedMetadata.getBytes())).toString();
			} catch(Throwable t) {
				// firefox
			}


			logger.debug(METADATA + " = [" + jsonEncodedMetadata + "]");
			JSONArray metadata = new JSONArray(jsonEncodedMetadata);
			for (int i = 0; i < metadata.length(); i++) {
				JSONObject aMetadata = metadata.getJSONObject(i);
				Integer metadataId = aMetadata.getInt(MetadataJSONSerializer.METADATA_ID);
				String text = aMetadata.getString(MetadataJSONSerializer.TEXT);
				ObjMetacontent aObjMetacontent = dao.loadObjMetacontent(metadataId, biobjectId, subobjectId);
				if (aObjMetacontent == null) {
					logger.debug("ObjMetacontent for metadata id = " + metadataId + ", biobject id = " + biobjectId +
							", subobject id = " + subobjectId + " was not found, creating a new one...");
					aObjMetacontent = new ObjMetacontent();
					aObjMetacontent.setObjmetaId(metadataId);
					aObjMetacontent.setBiobjId(biobjectId);
					aObjMetacontent.setSubobjId(subobjectId);
					aObjMetacontent.setContent(text.getBytes(UTF_8));
					aObjMetacontent.setCreationDate(new Date());
					aObjMetacontent.setLastChangeDate(new Date());
					dao.insertObjMetacontent(aObjMetacontent);
				} else {
					logger.debug("ObjMetacontent for metadata id = " + metadataId + ", biobject id = " + biobjectId +
							", subobject id = " + subobjectId + " was found, it will be modified...");
					aObjMetacontent.setContent(text.getBytes(UTF_8));
					aObjMetacontent.setLastChangeDate(new Date());
					dao.modifyObjMetacontent(aObjMetacontent);
				}

			}

			try {
				writeBackToClient( new JSONAcknowledge() );
			} catch (IOException e) {
				String message = "Impossible to write back the responce to the client";
				throw new SpagoBIEngineServiceException(getActionName(), message, e);
			}

		} catch (Exception e) {
			throw new SpagoBIServiceException(SERVICE_NAME, "Exception occurred while saving metadata", e);
		} finally {
			logger.debug("OUT");
		}
	}

}
