package nl.knaw.huygens.timbuctoo.storage;

/*
 * #%L
 * Timbuctoo core
 * =======
 * Copyright (C) 2012 - 2014 Huygens ING
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

import java.io.IOException;
import java.util.List;

import nl.knaw.huygens.timbuctoo.model.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

public class UserFileCollectionDeserializer extends JsonDeserializer<UserFileCollection> {
	private static Logger LOG = LoggerFactory.getLogger(UserFileCollectionDeserializer.class);

	@Override
	public UserFileCollection deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
		try {
			List<User> users = jp.readValueAs(new TypeReference<List<User>>() {});
			return new UserFileCollection(users);
		} catch (Exception e) {
			LOG.error("UserFileCollection could not be deserialized", e);
		}
		return new UserFileCollection();
	}
}
