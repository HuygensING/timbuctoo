package nl.knaw.huygens.timbuctoo.v5.datastores.implementations.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import nl.knaw.huygens.timbuctoo.v5.datastores.dto.StoreStatus;
import nl.knaw.huygens.timbuctoo.v5.datastores.dto.StoreStatusImpl;
import nl.knaw.huygens.timbuctoo.v5.datastores.implementations.json.dto.Schema;
import nl.knaw.huygens.timbuctoo.v5.datastores.schema.SchemaStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.schema.dto.Predicate;
import nl.knaw.huygens.timbuctoo.v5.datastores.schema.dto.Type;
import nl.knaw.huygens.timbuctoo.v5.datastores.triples.TripleStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.triples.dto.Quad;
import nl.knaw.huygens.timbuctoo.v5.logprocessing.QuadHandler;
import nl.knaw.huygens.timbuctoo.v5.logprocessing.exceptions.LogProcessingFailedException;
import nl.knaw.huygens.timbuctoo.v5.logprocessing.exceptions.ProcessingFailedException;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.LANGSTRING;
import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.RDF_TYPE;
import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.UNKNOWN;
import static org.slf4j.LoggerFactory.getLogger;

public class JsonSchemaStore implements SchemaStore {
  private final Schema jsonData;
  private static final Function<String, Type> TYPE_MAKER = Type::new;
  protected final File schemaFile;
  protected final ObjectMapper mapper;
  private static final Logger LOG = getLogger(JsonSchemaStore.class);

  public JsonSchemaStore(File dataLocation, ObjectMapper objectMapper) throws IOException {
    mapper = objectMapper;
    schemaFile = dataLocation;
    if (schemaFile.exists()) {
      jsonData = mapper.readValue(schemaFile, Schema.class);
      for (Map.Entry<String, Type> typeEntry : jsonData.types.entrySet()) {
        typeEntry.getValue().setName(typeEntry.getKey());
      }
    } else {
      jsonData = initialData();
    }
  }

  private Schema initialData() {
    Schema result = new Schema();
    result.types = new HashMap<>();
    result.storeStatus = new StoreStatusImpl(0);
    return result;
  }

  @Override
  public Map<String, Type> getTypes() {
    return jsonData.types;
  }

  @Override
  public void process(TripleStore tripleStore, long version) throws ProcessingFailedException {
    Importer importer = new Importer(tripleStore, jsonData.storeStatus);
    try {
      tripleStore.getQuads(importer);
      jsonData.storeStatus.finishUpdate(version);
      jsonData.types = importer.nextTypes;
      saveData(jsonData);
    } catch (LogProcessingFailedException e) {
      jsonData.storeStatus.abortUpdate(e.getMessage());
      saveData(jsonData);
      throw new ProcessingFailedException(e.getCause());
    }
  }

  private void saveData(Schema data) throws ProcessingFailedException {
    try {
      mapper.writeValue(schemaFile, data);
    } catch (IOException e) {
      throw new ProcessingFailedException(e);
    }
  }

  @Override
  public void close() throws Exception {
  }

  @Override
  public StoreStatus getStatus() {
    return jsonData.storeStatus;
  }

  private class Importer implements QuadHandler {
    private final TripleStore tripleStore;
    private final StoreStatusImpl storeStatus;
    Map<String, Type> nextTypes = new HashMap<>();
    String curSubject = "";
    String prevPredicate = "";
    boolean predicateUsedTwice;
    Map<String, Type> curTypes = new HashMap<>();


    private Importer(TripleStore tripleStore, StoreStatusImpl storeStatus) {
      this.tripleStore = tripleStore;
      this.storeStatus = storeStatus;
    }

    @Override
    public void start(long lineCount) throws LogProcessingFailedException {
      storeStatus.startUpdate(lineCount);
    }

    private void onTriple(String subject) {
      if (!curSubject.equals(subject)) {
        curSubject = subject;
        prevPredicate = "";
        curTypes.clear();
      }
    }

    private void handleStatement(String predicateStr, String object, String valueType)
        throws LogProcessingFailedException {
      if (curTypes.isEmpty()) {
        curTypes.put(UNKNOWN, nextTypes.computeIfAbsent(UNKNOWN, TYPE_MAKER));
      }
      if (prevPredicate.equals(predicateStr)) {
        predicateUsedTwice = true;
      } else {
        predicateUsedTwice = false;
      }
      prevPredicate = predicateStr;
      final ArrayList<Predicate> predicates = new ArrayList<>();
      for (Type type : curTypes.values()) {
        predicates.add(type.getOrCreatePredicate(predicateStr));
      }
      for (Predicate predicate : predicates) {
        if (predicateUsedTwice) {
          predicate.setList(true);
        } else {
          predicate.incUsage(); //this predicate is used at an instance of this type
        }
      }
      if (valueType != null) {
        for (Predicate predicate : predicates) {
          predicate.addValueType(valueType);
        }
      } else {
        try (Stream<Quad> quads = tripleStore.getQuads(object, RDF_TYPE)) {
          Iterator<Quad> iterator = quads.iterator();
          boolean hadType = false;
          while (iterator.hasNext()) {
            Quad quad = iterator.next();
            for (Predicate predicate : predicates) {
              hadType = true;
              predicate.addReferenceType(quad.getObject());
            }
          }
          if (!hadType) {
            for (Predicate predicate : predicates) {
              predicate.addReferenceType(UNKNOWN);
            }
          }
        }
      }
    }

    @Override
    public void onPrefix(long line, String prefix, String iri) throws LogProcessingFailedException {
    }

    @Override
    public void onRelation(long line, String subject, String predicate, String object, String graph)
        throws LogProcessingFailedException {
      if (storeStatus.getPosition() > line) {
        return;
      }
      storeStatus.setPosition(line);
      onTriple(subject);
      if (RDF_TYPE.equals(predicate)) {
        curTypes.put(object, nextTypes.computeIfAbsent(object, TYPE_MAKER));
      } else {
        handleStatement(predicate, object, null);
      }
    }

    @Override
    public void onLiteral(long line, String subject, String predicate, String object, String valueType, String graph)
        throws LogProcessingFailedException {
      if (storeStatus.getPosition() > line) {
        return;
      }
      storeStatus.setPosition(line);
      onTriple(subject);
      handleStatement(predicate, object, valueType);
    }

    @Override
    public void onLanguageTaggedString(long line, String subject, String predicate, String value, String language,
                                       String graph)
        throws LogProcessingFailedException {
      if (storeStatus.getPosition() > line) {
        return;
      }
      storeStatus.setPosition(line);
      onTriple(subject);
      handleStatement(predicate, value, LANGSTRING);
    }

    @Override
    public void cancel() throws LogProcessingFailedException {
    }

    @Override
    public void finish() throws LogProcessingFailedException {
    }
  }
}
