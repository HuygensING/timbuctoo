package nl.knaw.huygens.timbuctoo.rml;

import nl.knaw.huygens.timbuctoo.rml.rmldata.RmlLogicalSource;

import java.util.function.Function;

public class DataSourceFactory implements Function<RmlLogicalSource, DataSource> {
  @Override
  public DataSource apply(RmlLogicalSource rmlLogicalSource) {
    throw new UnsupportedOperationException("");//FIXME: implement
  }
}
