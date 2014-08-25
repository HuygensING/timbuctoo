package nl.knaw.huygens.timbuctoo.storage;

import java.io.IOException;
import java.util.List;

import nl.knaw.huygens.timbuctoo.model.VREAuthorization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

public class VREAuthorizationFileCollectionDeserializer extends JsonDeserializer<VREAuthorizationFileCollection> {
	private static Logger LOG = LoggerFactory.getLogger(VREAuthorizationFileCollectionDeserializer.class);

	@Override
	public VREAuthorizationFileCollection deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
		try {
			List<VREAuthorization> authorizations = jp.readValueAs(new TypeReference<List<VREAuthorization>>() {});

			return new VREAuthorizationFileCollection(authorizations);
		} catch (Exception e) {
			LOG.error("VREAuthorizationFileCollection could not be deserialized", e);
		}

		return new VREAuthorizationFileCollection();
	}
}
