package nl.knaw.huygens.timbuctoo.experimental.womenwriters;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import nl.knaw.huygens.timbuctoo.crud.InvalidCollectionException;
import nl.knaw.huygens.timbuctoo.crud.NotFoundException;
import nl.knaw.huygens.timbuctoo.crud.UrlGenerator;
import nl.knaw.huygens.timbuctoo.database.DataAccess;
import nl.knaw.huygens.timbuctoo.database.EntityToJsonMapper;
import nl.knaw.huygens.timbuctoo.database.dto.WwReadEntity;
import nl.knaw.huygens.timbuctoo.database.dto.WwRelationRef;
import nl.knaw.huygens.timbuctoo.model.vre.Collection;
import nl.knaw.huygens.timbuctoo.model.vre.Vres;
import nl.knaw.huygens.timbuctoo.security.UserStore;

import java.util.UUID;

import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsn;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsnA;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsnO;

public class WomenWritersJsonCrudService {

  private final Vres mappings;
  private final DataAccess dataAccess;
  private final EntityToJsonMapper entityToJsonMapper;

  public WomenWritersJsonCrudService(Vres mappings,
                                     UserStore userStore,
                                     UrlGenerator relationUrlFor,
                                     DataAccess dataAccess) {
    this.mappings = mappings;
    this.dataAccess = dataAccess;
    entityToJsonMapper = new EntityToJsonMapper(userStore, relationUrlFor);
  }


  public JsonNode get(String collectionName, UUID id) throws InvalidCollectionException, NotFoundException {
    return get(collectionName, id, null);
  }

  public JsonNode get(String collectionName, UUID id, Integer rev)
    throws InvalidCollectionException, NotFoundException {

    final Collection collection = mappings.getCollection(collectionName)
                                          .orElseThrow(() -> new InvalidCollectionException(collectionName));

    if (collection.isRelationCollection()) {
      return jsnO("message", jsn("Getting a wwrelation is not yet supported"));
    } else {
      return getEntity(id, rev, collection);
    }
  }

  private JsonNode getEntity(UUID id, Integer rev, Collection collection) throws NotFoundException {
    ObjectNode result;
    try (DataAccess.DataAccessMethods db = dataAccess.start()) {
      try {
        WwReadEntity entity = db.getWwEntity(id, rev, collection);
        result = entityToJsonMapper.mapEntity(collection, entity, true,
          (readEntity, resultJson) -> {
            if (readEntity instanceof WwReadEntity) {
              resultJson.set("@authorLanguages",
                jsnA(((WwReadEntity) readEntity).getLanguages().stream().map(lang -> jsn(lang))));
            }
          },
          (relationRef, resultJson) -> {
            if (relationRef instanceof WwRelationRef) {
              resultJson.set("gender", jsn(((WwRelationRef) relationRef).getGender()));
              resultJson.set("authors", jsnA(((WwRelationRef) relationRef)
                .getAuthors().stream()
                .map(author -> jsnO("displayName", jsn(author.getLeft()), "gender", jsn(author.getRight())))
              ));
            }
          });
        db.success();
      } catch (NotFoundException e) {
        db.rollback();
        throw e;
      }
    }

    return result;
  }
}
