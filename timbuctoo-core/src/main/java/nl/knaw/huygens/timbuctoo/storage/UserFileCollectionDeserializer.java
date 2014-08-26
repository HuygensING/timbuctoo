package nl.knaw.huygens.timbuctoo.storage;

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
