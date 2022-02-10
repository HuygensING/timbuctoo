package nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto;

import nl.knaw.huygens.timbuctoo.v5.dataset.dto.DataSet;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.DataSetMetaData;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.ImportInfo;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.LogList;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.CursorQuad;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.Direction;
import nl.knaw.huygens.timbuctoo.v5.graphql.security.UserPermissionCheck;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;
import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.RDF_TYPE;

public class DataSetWithDatabase implements DataSetMetaData, SubjectReference {
  private final DataSet dataSet;
  private final DataSetMetaData dataSetMetaData;
  private final LogList logList;
  private final Set<String> userPermissions;
  private Set<String> types;


  public DataSetWithDatabase(DataSet dataSet, UserPermissionCheck userPermissionCheck) {
    this.dataSet = dataSet;
    this.dataSetMetaData = dataSet.getMetadata();
    this.logList = dataSet.getImportManager().getLogList();
    this.userPermissions = userPermissionCheck.getPermissions(dataSet.getMetadata()).stream()
                                              .map(Enum::toString)
                                              .collect(Collectors.toSet());
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
  public List<ImportInfo> getImportInfo() {
    return dataSetMetaData.getImportInfo();
  }

  @Override
  public String getSubjectUri() {
    return dataSetMetaData.getBaseUri();
  }

  @Override
  public void publish() {
    dataSetMetaData.publish();
  }

  public String getLastUpdated() {
    return logList.getLastImportDate();
  }

  @Override
  public DataSet getDataSet() {
    return dataSet;
  }

  @Override
  public Set<String> getTypes() {
    if (types == null) {
      try (Stream<CursorQuad> quads =
               dataSet.getQuadStore().getQuads(dataSetMetaData.getBaseUri(), RDF_TYPE, Direction.OUT, "")) {
        types = quads
            .map(CursorQuad::getObject)
            .collect(toSet());
      }
    }
    return types;
  }

  public Set<String> getUserPermissions() {
    return userPermissions;
  }
}
