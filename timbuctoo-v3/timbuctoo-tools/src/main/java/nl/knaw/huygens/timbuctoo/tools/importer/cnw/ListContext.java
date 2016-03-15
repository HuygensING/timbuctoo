package nl.knaw.huygens.timbuctoo.tools.importer.cnw;

/*
 * #%L
 * Timbuctoo tools
 * =======
 * Copyright (C) 2012 - 2015 Huygens ING
 * =======
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import java.util.Map;

import nl.knaw.huygens.tei.XmlContext;

import com.google.common.collect.Maps;

public class ListContext extends XmlContext {

	private Map<String, String> map = Maps.newHashMap();
	private String name = "";
	private String listKey = "";

	public String getListKey() {
		return listKey;
	}

	protected Map<String, String> getMap() {
		return map;
	}

	protected String getName() {
		return name;
	}

	protected void setName(String name) {
		this.name = name;
	}

	public void setListKey(String listKey) {
		this.listKey = listKey;

	}
}
