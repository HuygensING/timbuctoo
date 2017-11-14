package nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto;

import nl.knaw.huygens.timbuctoo.v5.dataset.dto.DataSet;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.PromotedDataSet;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.berkeleydb.dto.LazyTypeSubjectReference;

public class DataSetWithDatabase extends LazyTypeSubjectReference implements PromotedDataSet {
  private final PromotedDataSet promotedDataSet;

  @Override
  public String getDataSetId() {
    return promotedDataSet.getDataSetId();
  }

  @Override
  public String getOwnerId() {
    return promotedDataSet.getOwnerId();
  }

  @Override
  public String getBaseUri() {
    return promotedDataSet.getBaseUri();
  }

  @Override
  public String getUriPrefix() {
    return promotedDataSet.getUriPrefix();
  }

  @Override
  public String getCombinedId() {
    return promotedDataSet.getCombinedId();
  }

  public boolean isPromoted() {
    return promotedDataSet.isPromoted();
  }

  public boolean isPrivate() {
    return promotedDataSet.isPrivate();
  }

  public DataSetWithDatabase(DataSet dataSet) {
    super(dataSet.getMetadata().getBaseUri(), dataSet);
    this.promotedDataSet = dataSet.getMetadata();
  }

  @Override
  public String getSubjectUri() {
    return promotedDataSet.getBaseUri();
  }

}
