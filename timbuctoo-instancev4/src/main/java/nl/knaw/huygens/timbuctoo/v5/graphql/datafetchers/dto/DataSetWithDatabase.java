package nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto;

import nl.knaw.huygens.timbuctoo.v5.dataset.dto.DataSet;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.DataSetMetaData;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.berkeleydb.dto.LazyTypeSubjectReference;

public class DataSetWithDatabase extends LazyTypeSubjectReference implements DataSetMetaData {
  private final DataSetMetaData dataSetMetaData;

  @Override
  public String getDataSetId() {
    return dataSetMetaData.getDataSetId();
  }

  @Override
  public String getOwnerId() {
    return dataSetMetaData.getOwnerId();
  }

  @Override
  public String getBaseUri() {
    return dataSetMetaData.getBaseUri();
  }

  @Override
  public String getUriPrefix() {
    return dataSetMetaData.getUriPrefix();
  }

  @Override
  public String getCombinedId() {
    return dataSetMetaData.getCombinedId();
  }

  public boolean isPromoted() {
    return dataSetMetaData.isPromoted();
  }

  public boolean isPublic() {
    return dataSetMetaData.isPublic();
  }

  public DataSetWithDatabase(DataSet dataSet) {
    super(dataSet.getMetadata().getBaseUri(), dataSet);
    this.dataSetMetaData = dataSet.getMetadata();
  }

  @Override
  public String getSubjectUri() {
    return dataSetMetaData.getBaseUri();
  }

}
