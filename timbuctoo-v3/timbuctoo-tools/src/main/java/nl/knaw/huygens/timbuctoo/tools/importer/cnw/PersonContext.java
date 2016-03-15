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
import nl.knaw.huygens.timbuctoo.model.cnw.AltName;
import nl.knaw.huygens.timbuctoo.model.cnw.CNWLink;
import nl.knaw.huygens.timbuctoo.model.cnw.CNWPerson;
import nl.knaw.huygens.timbuctoo.model.util.PersonName;

import com.google.common.collect.Maps;

public class PersonContext extends XmlContext {
	Map<String, String> locationIdMap = Maps.newHashMap();
	public CNWPerson person;

	public PersonName personName;
	String placeString;
	String country;
	public String birthPlaceId;
	public String deathPlaceId;
	public String year;
	public CNWLink link;
	public String nametype;
	String pid;
	public AltName currentAltName;
	public String currentRelationType;
	public String currentRelativeName;

	public PersonContext(String pid) {
		this.pid = pid;
	}

}
