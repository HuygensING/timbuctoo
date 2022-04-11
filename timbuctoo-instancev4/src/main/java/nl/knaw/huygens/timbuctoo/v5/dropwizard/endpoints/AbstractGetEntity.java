package nl.knaw.huygens.timbuctoo.v5.dropwizard.endpoints;

import nl.knaw.huygens.timbuctoo.v5.dataset.DataSetRepository;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.DataSet;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.CursorQuad;
import nl.knaw.huygens.timbuctoo.v5.security.UserValidator;
import nl.knaw.huygens.timbuctoo.v5.security.dto.User;
import nl.knaw.huygens.timbuctoo.v5.security.exceptions.UserValidationException;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.util.Iterator;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

public abstract class AbstractGetEntity {
  private final DataSetRepository dataSetRepository;
  private final UserValidator userValidator;

  public AbstractGetEntity(DataSetRepository dataSetRepository, UserValidator userValidator) {
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

  protected Response handleRequest(String authHeader, String ownerId, String dataSetId,
                                   Function<DataSet, Stream<CursorQuad>> createStream) {
    Optional<User> user;
    try {
      user = userValidator.getUserFromAccessToken(authHeader);
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

    return streamToStreamingResponse(createStream.apply(dataSet.get()));
  }

  private Response streamToStreamingResponse(final Stream<CursorQuad> dataStream) {
    StreamingOutput streamingData = output -> {
      BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(output));
      try (Stream<CursorQuad> data = dataStream) {

        for (Iterator<CursorQuad> dataIt = data.iterator(); dataIt.hasNext(); ) {
          writer.write(dataIt.next().toString() + "\n");
        }
      }
      writer.flush();
    };

    return Response.ok(streamingData).build();
  }
}
