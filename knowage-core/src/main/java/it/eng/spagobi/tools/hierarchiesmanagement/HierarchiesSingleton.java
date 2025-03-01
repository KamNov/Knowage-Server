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
package it.eng.spagobi.tools.hierarchiesmanagement;

import org.apache.log4j.Logger;

import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

/**
 * This class is a singleton that contains the Hierarchies object
 *
 * @author Marco Cortella (marco.cortella@eng.it)
 *
 */
public class HierarchiesSingleton {

	private static final Logger LOGGER = Logger.getLogger(HierarchiesSingleton.class);

	private static Hierarchies instance;

	public static synchronized Hierarchies getInstance() {
		LOGGER.debug("IN");

		try {
			instance = new Hierarchies();
		} catch (Exception e) {
			LOGGER.error("Impossible to create the Hierarchies object", e);
			throw new SpagoBIRuntimeException(e.getMessage(), e);
		}
		LOGGER.debug("OUT");

		return instance;
	}

	public static synchronized void refreshHierarchies() {
		LOGGER.debug("IN");

		LOGGER.debug("refresh hierarchies");
		instance = new Hierarchies();
		LOGGER.debug("OUT");

	}

	private HierarchiesSingleton() {

	}

}
