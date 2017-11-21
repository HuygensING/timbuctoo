package nl.knaw.huygens.timbuctoo.v5.dropwizard.contenttypes;

import nl.knaw.huygens.timbuctoo.v5.serializable.serializations.SqlSerialization;

import javax.ws.rs.Produces;

@Produces(SqlWriter.MIME_TYPE)
public class SqlWriter extends SerializerWriter {

  public static final String MIME_TYPE = "text/sql";

  public SqlWriter() {
    super(SqlSerialization::new);
  }

  @Override
  public String getMimeType() {
    return MIME_TYPE;
  }
}
