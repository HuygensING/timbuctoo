package nl.knaw.huygens.timbuctoo.experimental.womenwriters;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import nl.knaw.huygens.timbuctoo.crud.InvalidCollectionException;
import nl.knaw.huygens.timbuctoo.crud.NotFoundException;
import nl.knaw.huygens.timbuctoo.crud.UrlGenerator;
import nl.knaw.huygens.timbuctoo.database.DataAccess;
import nl.knaw.huygens.timbuctoo.database.DataAccess.CustomEntityProperties;
import nl.knaw.huygens.timbuctoo.database.DataAccess.CustomRelationProperties;
import nl.knaw.huygens.timbuctoo.database.EntityToJsonMapper;
import nl.knaw.huygens.timbuctoo.database.EntityToJsonMapper.ExtraEntityMappingOptions;
import nl.knaw.huygens.timbuctoo.database.EntityToJsonMapper.ExtraRelationMappingOptions;
import nl.knaw.huygens.timbuctoo.database.dto.ReadEntity;
import nl.knaw.huygens.timbuctoo.database.dto.RelationRef;
import nl.knaw.huygens.timbuctoo.database.dto.dataset.Collection;
import nl.knaw.huygens.timbuctoo.model.vre.Vre;
import nl.knaw.huygens.timbuctoo.model.vre.Vres;
import nl.knaw.huygens.timbuctoo.security.UserStore;
import nl.knaw.huygens.timbuctoo.util.Tuple;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static nl.knaw.huygens.timbuctoo.database.dto.DisplayNameHelper.getDisplayname;
import static nl.knaw.huygens.timbuctoo.model.GraphReadUtils.getProp;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsn;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsnA;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsnO;
import static nl.knaw.huygens.timbuctoo.util.Tuple.tuple;

public class WomenWritersJsonCrudService {

  public static final Logger LOG = LoggerFactory.getLogger(WomenWritersJsonCrudService.class);
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
        CustomEntityMapping customEntityMapping = new CustomEntityMapping();
        CustomRelationMapping customRelationMapping = new CustomRelationMapping();

        ReadEntity entity = db.getEntity(id, rev, collection, customEntityMapping, customRelationMapping);

        result = entityToJsonMapper.mapEntity(collection, entity, true, customEntityMapping, customRelationMapping);
        db.success();
      } catch (NotFoundException e) {
        db.rollback();
        throw e;
      }
    }

    return result;
  }

  private static class CustomEntityMapping implements CustomEntityProperties, ExtraEntityMappingOptions {

    @Override
    public void execute(ReadEntity readEntity, ObjectNode resultJson) {
      resultJson.set("@authorLanguages",
        jsnA(((Set<String>) readEntity.getExtraProperty("languages").orElse(Sets.newHashSet())).stream().map(
          lang -> jsn(lang))));
    }


    @Override
    public void execute(ReadEntity entity, Vertex entityVertex) {
      Set<String> languages = new HashSet<>();
      final Iterator<Edge> isCreatorOf = entityVertex.edges(Direction.IN, "isCreatedBy");

      while (isCreatorOf.hasNext()) {
        final Edge next = isCreatorOf.next();
        final Boolean creatorOfIsAccepted = next.property("wwrelation_accepted").isPresent() ?
          (Boolean) next.property("wwrelation_accepted").value() : false;
        final Boolean creatorOfIsLatest = next.property("isLatest").isPresent() ?
          (Boolean) next.property("isLatest").value() : false;

        if (creatorOfIsAccepted && creatorOfIsLatest) {
          final Vertex publication = next.outVertex();
          final Iterator<Edge> hasWorkLanguage = publication.edges(Direction.OUT, "hasWorkLanguage");
          while (hasWorkLanguage.hasNext()) {
            final Edge nextLanguage = hasWorkLanguage.next();
            final Boolean languageIsAccepted = nextLanguage.property("wwrelation_accepted").isPresent() ?
              (Boolean) nextLanguage.property("wwrelation_accepted").value() : false;
            final Boolean languageIsLatest = nextLanguage.property("isLatest").isPresent() ?
              (Boolean) nextLanguage.property("isLatest").value() : false;

            if (languageIsAccepted && languageIsLatest) {
              final Vertex languageVertex = nextLanguage.inVertex();
              final String language = getProp(languageVertex, "wwlanguage_name", String.class).orElse(null);
              if (language != null) {
                languages.add(language);
              }
            }
          }
        }
      }
      entity.addExtraPoperty("languages", languages);
    }


  }

  public static class CustomRelationMapping implements CustomRelationProperties, ExtraRelationMappingOptions {
    @Override
    public void execute(RelationRef relationRef, ObjectNode resultJson) {
      resultJson.set("gender", jsn((String) relationRef.getExtraProperty("gender").orElse("")));
      resultJson.set("authors", jsnA(((List<Tuple<String, String>>) relationRef.getExtraProperty("authors")
                                                                               .orElse(Lists.newArrayList()))
        .stream()
        .map(author -> jsnO("displayName", jsn(author.getLeft()), "gender", jsn(author.getRight())))
      ));
    }

    @Override
    public void execute(GraphTraversalSource traversalSource, Vre vre, Vertex target,
                        RelationRef relationRef1) {
      String gender =
        getProp(target, "wwperson_gender", String.class)
          .orElse(null);
      if (gender != null) {
        gender = gender.replaceAll("\"", "");
      }
      List<Tuple<String, String>> authors = getAuthorsForPublication(traversalSource, vre, target);
      relationRef1.addExtraProperty("gender", gender);
      relationRef1.addExtraProperty("authors", authors);
    }

    private List<Tuple<String, String>> getAuthorsForPublication(GraphTraversalSource traversalSource,
                                                                 Vre vre,
                                                                 Vertex vertex) {
      List<Tuple<String, String>> authors = new ArrayList<>();

      final Iterator<Edge> isCreatedBy = vertex.edges(Direction.OUT, "isCreatedBy");

      while (isCreatedBy.hasNext()) {
        final Edge next = isCreatedBy.next();
        final Boolean isAccepted = (Boolean) next.property("wwrelation_accepted").isPresent() ?
          (Boolean) next.property("wwrelation_accepted").value() : false;
        final Object isLatest = next.property("isLatest").isPresent() ?
          (Boolean) next.property("isLatest").value() : false;

        if (isAccepted && (Boolean) isLatest) {
          final Vertex author1 = next.inVertex();
          final Collection personCollection = vre.getCollectionForTypeName("wwperson");
          final String authorName = getDisplayname(traversalSource, author1, personCollection).orElse(null);
          String authorGender = getProp(author1, "wwperson_gender", String.class).orElse(null);
          if (authorGender != null) {
            authorGender = authorGender.replaceAll("\"", "");
          }
          if (authorName != null) {
            authors.add(tuple(authorName, authorGender));
          }
        }
      }
      return authors;
    }

  }

}
