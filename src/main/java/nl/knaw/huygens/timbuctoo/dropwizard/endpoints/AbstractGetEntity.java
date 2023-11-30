package nl.knaw.huygens.timbuctoo.dropwizard.endpoints;

import nl.knaw.huygens.timbuctoo.dataset.DataSetRepository;
import nl.knaw.huygens.timbuctoo.security.dto.User;
import nl.knaw.huygens.timbuctoo.dataset.dto.DataSet;
import nl.knaw.huygens.timbuctoo.datastores.quadstore.dto.CursorQuad;
import nl.knaw.huygens.timbuctoo.security.UserValidator;
import nl.knaw.huygens.timbuctoo.security.exceptions.UserValidationException;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.rio.RDFWriterRegistry;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.WriterConfig;
import org.eclipse.rdf4j.rio.helpers.BasicWriterSettings;
import org.eclipse.rdf4j.rio.helpers.JSONLDMode;
import org.eclipse.rdf4j.rio.helpers.JSONLDSettings;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.OutputStream;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import static nl.knaw.huygens.timbuctoo.util.BNodeHelper.isSkolomIri;

public abstract class AbstractGetEntity {
  private static final int MAX_DEPTH_BNODES = 5;
  private static final WriterConfig WRITER_CONFIG = new WriterConfig()
      .set(BasicWriterSettings.INLINE_BLANK_NODES, true)
      .set(JSONLDSettings.JSONLD_MODE, JSONLDMode.COMPACT)
      .set(JSONLDSettings.OPTIMIZE, true)
      .set(JSONLDSettings.USE_NATIVE_TYPES, true);

  private final RDFWriterRegistry rdfWriterRegistry;
  private final DataSetRepository dataSetRepository;
  private final UserValidator userValidator;

  public AbstractGetEntity(DataSetRepository dataSetRepository, UserValidator userValidator) {
    this.rdfWriterRegistry = RDFWriterRegistry.getInstance();
    this.dataSetRepository = dataSetRepository;
    this.userValidator = userValidator;
  }

  protected static String escapeCharacters(String value) {
    return value
        .replace("\\", "\\\\")
        .replace("\n", "\\n")
        .replace("\r", "\\r")
        .replace("\"", "\\\"");
  }

  protected Response handleRequest(String ownerId, String dataSetId, HttpHeaders headers, String id,
                                   Function<DataSet, Function<String, Stream<CursorQuad>>> createStreamWithDataset) {
    Optional<User> user;
    try {
      user = userValidator.getUserFromAccessToken(headers.getHeaderString("authorization"));
    } catch (UserValidationException e) {
      return Response.status(404).build();
    }

    Optional<DataSet> dataSet;

    if (user.isPresent()) {
      dataSet = dataSetRepository.getDataSet(user.get(), ownerId, dataSetId);
    } else {
      dataSet = dataSetRepository.getDataSet(null, ownerId, dataSetId);
    }

    if (dataSet.isEmpty()) {
      return Response.status(Response.Status.FORBIDDEN).build();
    }

    RDFFormat format = headers.getAcceptableMediaTypes().stream()
        .map(type -> type.isCompatible(MediaType.TEXT_HTML_TYPE) ?
            Optional.of(RDFFormat.JSONLD) :
            rdfWriterRegistry.getFileFormatForMIMEType(type.toString()))
        .flatMap(Optional::stream)
        .findFirst()
        .orElse(RDFFormat.JSONLD);

    return Response
        .ok()
        .type(format.getDefaultMIMEType())
        .entity((StreamingOutput) out ->
            createEntity(dataSet.get(), id, createStreamWithDataset.apply(dataSet.get()), format, out))
        .build();
  }

  private static void createEntity(DataSet dataSet, String id, Function<String, Stream<CursorQuad>> createStream,
                                   RDFFormat format, OutputStream out) {
    Model model = dataSet.initModel();
    addToModel(model, id, createStream);
    Rio.write(model, out, format, WRITER_CONFIG);
  }

  private static void addToModel(Model model, String uri, Function<String, Stream<CursorQuad>> createStream) {
    addToModel(model, uri, createStream, 1);
  }

  private static void addToModel(Model model, String uri,
                                 Function<String, Stream<CursorQuad>> createStream, int level) {
    if (level > MAX_DEPTH_BNODES) {
      return;
    }

    try (Stream<CursorQuad> quads = createStream.apply(uri)) {
      for (CursorQuad quad : (Iterable<CursorQuad>) quads::iterator) {
        model.add(quad.getStatement());

        if (isSkolomIri(quad.getObject())) {
          addToModel(model, quad.getObject(), createStream, level + 1);
        }
      }
    }
  }
}
