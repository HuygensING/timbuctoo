package nl.knaw.huygens.timbuctoo.rest.providers;

/*
 * #%L
 * Timbuctoo REST api
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

import com.google.inject.Inject;
import com.google.inject.Singleton;
import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.model.RelationDTO;
import nl.knaw.huygens.timbuctoo.model.RelationSearchable;
import nl.knaw.huygens.timbuctoo.model.RelationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

/**
 * Generates a CSV representation for a reception search.
 * The current implementation is very specific for the NEWW project,
 * in particular, it uses the WWPerson and WWDocument classes.
 * By changing the requirements of the getClientRepresentation method
 * we can probably eliminate this program.
 */
@Provider
@Produces(CSVProvider.TEXT_CSV)
@Singleton
public class CSVProvider implements MessageBodyWriter<RelationSearchable> {

  public static final String TEXT_CSV = "text/csv";
  public static final MediaType TEXT_CSV_TYPE = new MediaType("text", "csv");

  public static final char SEPARATOR = '\t';

  /**
   * Length cannot be determined in advance.
   */
  private static final long UNKNOWN_LENGTH = -1;

  private static final Logger LOG = LoggerFactory.getLogger(CSVProvider.class);

  private final Repository repository;

  @Inject
  public CSVProvider(Repository repository) {
    this.repository = repository;
  }

  @Override
  public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return TEXT_CSV_TYPE.equals(mediaType) && RelationSearchable.class.isAssignableFrom(type);
  }

  @Override
  public long getSize(RelationSearchable doc, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return UNKNOWN_LENGTH;
  }

  @Override
  public void writeTo(RelationSearchable dto, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream out)
    throws IOException {

    List<RelationDTO> refs = dto.getRefs();
    if (refs != null && refs.size() > 0) {
      RelationDTO first = refs.get(0);
      String name = first.getRelationName();
      RelationType relationType = repository.getRelationTypeByName(name, false);
      if (relationType == null) {
        LOG.error("No relation type with name {}", name);
        return;
      }
      String sourceTypeName = relationType.getSourceTypeName();
      if (!"person".equals(sourceTypeName) && !"document".equals(sourceTypeName)) {
        LOG.error("Illegal source type of relation: {}", sourceTypeName);
        LOG.error("Allowed values are: 'person' and 'document'");
        return;
      }

      // header
      StringBuilder builder = new StringBuilder();
      appendKeysTo(builder, first.getSourceData(), "src-");
      appendTo(builder, "relationType");
      appendKeysTo(builder, first.getTargetData(), "dst-");
      writeLine(out, builder);

      // content
      for (RelationDTO ref : refs) {
        builder = new StringBuilder();
        appendValuesTo(builder, ref.getSourceData());
        appendTo(builder, ref.getRelationName());
        appendValuesTo(builder, ref.getTargetData());
        writeLine(out, builder);
      }
    }
  }

  private void appendKeysTo(StringBuilder builder, Map<String, ? extends Object> data, String prefix) {
    for (Map.Entry<String, ? extends Object> entry : data.entrySet()) {
      appendTo(builder, prefix + entry.getKey());
    }
  }

  private void appendValuesTo(StringBuilder builder, Map<String, ? extends Object> data) {
    for (Map.Entry<String, ? extends Object> entry : data.entrySet()) {
      appendTo(builder, entry.getValue() == null ? "" : "" + entry.getValue());
    }
  }

  private void appendTo(StringBuilder builder, String text) {
    if (builder.length() > 0) {
      builder.append(SEPARATOR);
    }
    builder.append(text);
  }

  private void writeLine(OutputStream out, StringBuilder builder) throws IOException {
    builder.append('\n');
    out.write(builder.toString().getBytes("UTF-8"));
  }

}
