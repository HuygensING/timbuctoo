package nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.DataSet;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.DataSetMetaData;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.berkeleydb.dto.LazyTypeSubjectReference;

public class DataSetWithDatabase extends LazyTypeSubjectReference implements DataSetMetaData {
  private final DataSetMetaData dataSetMetaData;
  private boolean published;

  public DataSetWithDatabase(DataSet dataSet) {
    super(dataSet.getMetadata().getBaseUri(), dataSet);
    this.dataSetMetaData = dataSet.getMetadata();
  }

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

  @Override
  public boolean isPromoted() {
    return dataSetMetaData.isPromoted();
  }

  @Override
  public boolean isPublished() {
    return dataSetMetaData.isPublished();
  }

  @Override
  public String getSubjectUri() {
    return dataSetMetaData.getBaseUri();
  }

  @Override
  public void publish() {
    this.published = true;
  }

}
