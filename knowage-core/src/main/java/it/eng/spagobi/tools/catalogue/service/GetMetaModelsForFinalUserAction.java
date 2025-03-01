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
package it.eng.spagobi.tools.catalogue.service;

import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import it.eng.spagobi.commons.bo.Domain;
import it.eng.spagobi.commons.bo.Role;
import it.eng.spagobi.commons.bo.RoleMetaModelCategory;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.ICategoryDAO;
import it.eng.spagobi.commons.dao.IDomainDAO;
import it.eng.spagobi.commons.dao.IRoleDAO;
import it.eng.spagobi.commons.serializer.SerializationException;
import it.eng.spagobi.commons.serializer.SerializerFactory;
import it.eng.spagobi.commons.utilities.UserUtilities;
import it.eng.spagobi.federateddataset.dao.ISbiFederationDefinitionDAO;
import it.eng.spagobi.tools.catalogue.bo.MetaModel;
import it.eng.spagobi.tools.catalogue.dao.IMetaModelsDAO;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;
import it.eng.spagobi.utilities.service.JSONSuccess;

public class GetMetaModelsForFinalUserAction extends GetMetaModelsAction {

	@Override
	public void doService() {

		logger.debug("IN");

		try {
			IMetaModelsDAO dao = DAOFactory.getMetaModelsDAO();
			dao.setUserProfile(this.getUserProfile());
			List<MetaModel> allModels = null;

			List<Integer> categories = getCategories();

			// the administrator can see ALL models
			if (UserUtilities.isAdministrator(this.getUserProfile())) {
				allModels = dao.loadAllMetaModels();
			} else {
				if (categories == null) {// no category defined in the db
					if (requestContainsAttribute(FILTERS)) {
						String filterString = getAttributeAsString(FILTERS);
						JSONObject jsonObject = new JSONObject(filterString);
						allModels = getFilteredModels(jsonObject, dao);
					} else {
						allModels = dao.loadAllMetaModels();
					}
				} else {
					if (categories.size() > 0) {
						if (requestContainsAttribute(FILTERS)) {
							String filterString = getAttributeAsString(FILTERS);
							JSONObject jsonObject = new JSONObject(filterString);
							allModels = getFilteredModels(jsonObject, dao, categories);
						} else {
							allModels = dao.loadMetaModelByCategories(categories);
						}
					}
				}
			}

			if (allModels == null) {
				allModels = new ArrayList<MetaModel>();
			}

			logger.debug("Read " + allModels.size() + " existing models");

			logger.debug("Read federated dataset");
			ISbiFederationDefinitionDAO federDsDao = DAOFactory.getFedetatedDatasetDAO();
			dao.setUserProfile(this.getUserProfile());

			Integer start = this.getStart();
			logger.debug("Start : " + start);
			// Integer limit = this.getLimit();
			// logger.debug("Limit : " + limit);

			int startIndex = Math.min(start, allModels.size());
			int stopIndex = allModels.size();
			// (limit > 0) ? Math.min(start + limit, allModels.size()) : allModels.size();

			List<MetaModel> toReturnSublist = allModels.subList(startIndex, stopIndex);

			try {
				JSONArray modelsJSON = (JSONArray) SerializerFactory.getSerializer("application/json").serialize(toReturnSublist, null);
				JSONObject rolesResponseJSON = createJSONResponse(modelsJSON, toReturnSublist.size());
				writeBackToClient(new JSONSuccess(rolesResponseJSON));
			} catch (IOException e) {
				throw new SpagoBIServiceException(SERVICE_NAME, "Impossible to write back the responce to the client", e);
			} catch (JSONException e) {
				throw new SpagoBIServiceException(SERVICE_NAME, "Cannot serialize objects into a JSON object", e);
			} catch (SerializationException e) {
				throw new SpagoBIServiceException(SERVICE_NAME, "Cannot serialize objects into a JSON object", e);
			}
		} catch (JSONException e) {
			logger.error("Cannot serialize objects into a JSON object", e);
			throw new SpagoBIServiceException(SERVICE_NAME, "Cannot serialize objects into a JSON object", e);
		} finally {
			logger.debug("OUT");
		}

	}

	protected List<Integer> getCategories() {

		List<Integer> categories = new ArrayList<Integer>();
		try {
			// NO CATEGORY IN THE DOMAINS
			IDomainDAO domaindao = DAOFactory.getDomainDAO();
			ICategoryDAO categoryDao = DAOFactory.getCategoryDAO();

			// TODO : Makes sense?
			List<Domain> dialects = categoryDao.getCategoriesForBusinessModel()
				.stream()
				.map(Domain::fromCategory)
				.collect(toList());
			if (dialects == null || dialects.size() == 0) {
				return null;
			}

			Collection userRoles = this.getUserProfile().getRoles();
			Iterator userRolesIter = userRoles.iterator();
			IRoleDAO roledao = DAOFactory.getRoleDAO();
			while (userRolesIter.hasNext()) {
				String roleName = (String) userRolesIter.next();
				Role role = roledao.loadByName(roleName);
				List<RoleMetaModelCategory> aRoleCategories = roledao.getMetaModelCategoriesForRole(role.getId());
				if (aRoleCategories != null) {
					for (Iterator iterator = aRoleCategories.iterator(); iterator.hasNext();) {
						RoleMetaModelCategory roleMetaModelCategory = (RoleMetaModelCategory) iterator.next();
						categories.add(roleMetaModelCategory.getCategoryId());
					}
				}
			}
		} catch (Exception e) {
			logger.error("Error loading the categories visible from the roles of the user");
			throw new SpagoBIRuntimeException("Error loading the categories visible from the roles of the user");
		}
		return categories;
	}

	protected List<MetaModel> getFilteredModels(JSONObject jsonObject, IMetaModelsDAO dao, List<Integer> categories) throws JSONException {
		List<MetaModel> metaModels = new ArrayList<MetaModel>();
		String columnFilter = jsonObject.getString("columnFilter");
		String valueFilter = jsonObject.getString("valueFilter");
		String typeFilter = jsonObject.getString("typeFilter");

		if (columnFilter.equals("category")) {
			if (typeFilter.equals("=")) {
				Integer categoryId = getCategoryIdbyName(valueFilter);
				if (categories == null || categories.size() == 0 || categories.contains(categoryId)) {
					if (categoryId != null) {
						List<Integer> categories2 = new ArrayList<Integer>();
						categories2.add(categoryId);
						metaModels.addAll(dao.loadMetaModelByCategories(categories2));
					}
				}

			} else if (typeFilter.equals("like")) {
				List<Integer> categoryIds = getCategoryIdbyContainsName(valueFilter);
				if (!categoryIds.isEmpty()) {
					for (Integer categoryId : categoryIds) {
						if (categories == null || categories.size() == 0 || categories.contains(categoryId)) {
							List<Integer> categories2 = new ArrayList<Integer>();
							categories.add(categoryId);
							metaModels.addAll(dao.loadMetaModelByCategories(categories2));
						}

					}
				}
			}

		} else if (columnFilter.equals("name")) {
			String filter = getFilterString(columnFilter, typeFilter, valueFilter);
			metaModels.addAll(dao.loadMetaModelByFilter(filter, categories));

		}
		return metaModels;
	}

}
