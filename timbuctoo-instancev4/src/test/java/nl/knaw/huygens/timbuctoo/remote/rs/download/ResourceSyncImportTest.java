package nl.knaw.huygens.timbuctoo.remote.rs.download;

import nl.knaw.huygens.timbuctoo.remote.rs.download.ResourceSyncFileLoader.RemoteFilesList;
import nl.knaw.huygens.timbuctoo.remote.rs.download.ResourceSyncImport.ResourceSyncReport;
import nl.knaw.huygens.timbuctoo.remote.rs.exceptions.CantDetermineDataSetException;
import nl.knaw.huygens.timbuctoo.v5.dataset.ImportManager;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.DataSet;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.DataSetMetaData;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.endsWith;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class ResourceSyncImportTest {

  private static final String BASE_URL = "http://127.0.0.1:8080/v5/resourcesync/u33707283d426f900d4d33707283d426f900d4d0d/clusius/";



  private Metadata metadata = new Metadata();
  private ImportManager importManager;
  private ResourceSyncFileLoader resourceSyncFileLoader;
  private ResourceSyncImport instance;
  private static final String CAPABILITY_LIST_URI = BASE_URL + "capabilitylist.xml";

  @Before
  public void setUp() {
    resourceSyncFileLoader = mock(ResourceSyncFileLoader.class);
    importManager = mock(ImportManager.class);
    given(importManager.isRdfTypeSupported(any(MediaType.class))).willReturn(true);
    DataSet dataSet = mock(DataSet.class);
    DataSetMetaData dataSetMetaData = mock(DataSetMetaData.class);
    given(dataSetMetaData.getBaseUri()).willReturn(BASE_URL);
    given(dataSet.getMetadata()).willReturn(dataSetMetaData);
    given(dataSet.getImportManager()).willReturn(importManager);
    instance = new ResourceSyncImport(resourceSyncFileLoader, dataSet, true);
  }

  @Test
  public void filterAndImportImportsAllChanges() throws Exception {
    List<RemoteFile> changes = new ArrayList<>();
    changes.add(RemoteFile.create(BASE_URL + "files/changes1.nqud", () -> null, "", metadata));
    changes.add(RemoteFile.create(BASE_URL + "files/changes2.nqud", () -> null, "", metadata));
    List<RemoteFile> resources = new ArrayList<>();
    resources.add(RemoteFile.create(BASE_URL + "files/dataset.nq", () -> null, "", metadata));
    RemoteFilesList remoteFilesList = new RemoteFilesList(changes, resources);
    given(resourceSyncFileLoader.getRemoteFilesList(CAPABILITY_LIST_URI, null)).willReturn(remoteFilesList);

    ResourceSyncReport resourceSyncReport = instance.filterAndImport(CAPABILITY_LIST_URI, null, false, null);

    verify(importManager).addLog(any(), any(), endsWith("changes1.nqud"), any(), any(), any());
    verify(importManager).addLog(any(), any(), endsWith("changes2.nqud"), any(), any(), any());
    assertThat(resourceSyncReport.importedFiles, containsInAnyOrder(
      is(BASE_URL + "files/changes1.nqud"),
      is(BASE_URL + "files/changes2.nqud")
    ));
  }

  @Test
  public void filterAndImportTheSingleDataSetFile() throws Exception {
    List<RemoteFile> changes = Collections.emptyList();
    List<RemoteFile> resources = new ArrayList<>();
    resources.add(RemoteFile.create(BASE_URL + "files/dataset.nq", () -> null, "", metadata));
    RemoteFilesList remoteFilesList = new RemoteFilesList(changes, resources);
    given(resourceSyncFileLoader.getRemoteFilesList(CAPABILITY_LIST_URI, null)).willReturn(remoteFilesList);

    ResourceSyncReport filteredFiles = instance.filterAndImport(CAPABILITY_LIST_URI, null, false, null);

    verify(importManager).addLog(any(), any(), endsWith("dataset.nq"), any(), any(), any());
    assertThat(filteredFiles.importedFiles, contains(BASE_URL + "files/dataset.nq"));
  }

  @Test
  public void filterReturnsListWithSingleDataSetFileIfChangesNotPresentAndDataSetPropertySet() throws Exception {
    List<RemoteFile> resources = new ArrayList<>();
    resources.add(RemoteFile.create(BASE_URL + "files/dataset.nq", () -> null, "", metadata));
    Metadata metadata2 = new Metadata();
    metadata2.setIsDataset(true);
    resources.add(RemoteFile.create(BASE_URL + "files/dataset.rdf", () -> null, "", metadata2));
    List<RemoteFile> changes = Collections.emptyList();
    RemoteFilesList remoteFilesList = new RemoteFilesList(changes, resources);
    given(resourceSyncFileLoader.getRemoteFilesList(CAPABILITY_LIST_URI, null)).willReturn(remoteFilesList);

    ResourceSyncReport filteredFiles = instance.filterAndImport(CAPABILITY_LIST_URI, null, false, null);

    verify(importManager).addLog(any(), any(), endsWith("dataset.rdf"), any(), any(), any());
    assertThat(filteredFiles.importedFiles, contains(BASE_URL + "files/dataset.rdf"));
  }

  @Test
  public void filterReturnsListWithUserSpecifiedDatasetFileIfPresent() throws Exception {
    List<RemoteFile> changes = Collections.emptyList();
    List<RemoteFile> resources = new ArrayList<>();
    resources.add(RemoteFile.create(BASE_URL + "files/dataset.nq", () -> null, "", metadata));
    resources.add(RemoteFile.create(BASE_URL + "files/dataset.rdf", () -> null, "", metadata));
    RemoteFilesList remoteFilesList = new RemoteFilesList(changes, resources);
    given(resourceSyncFileLoader.getRemoteFilesList(CAPABILITY_LIST_URI, null)).willReturn(remoteFilesList);
    String userSpecifiedDataSet = BASE_URL + "files/dataset.rdf";

    ResourceSyncReport filteredFiles = instance.filterAndImport(CAPABILITY_LIST_URI, userSpecifiedDataSet, false, null);

    verify(importManager).addLog(any(), any(), endsWith("dataset.rdf"), any(), any(), any());
    assertThat(filteredFiles.importedFiles, contains(BASE_URL + "files/dataset.rdf"));
  }

  @Test(expected = CantDetermineDataSetException.class)
  public void filterReturnsMessageWithFilesListIfMultipleFilesAndNoneSpecifiedAsDataset() throws Exception {
    String capabilityListUri = BASE_URL + "capabilitylist.xml";
    List<RemoteFile> changes = Collections.emptyList();
    List<RemoteFile> resources = new ArrayList<>();
    resources.add(RemoteFile.create(BASE_URL + "files/dataset.nq", () -> null, "", metadata));
    resources.add(RemoteFile.create(BASE_URL + "files/dataset.rdf", () -> null, "", metadata));
    RemoteFilesList remoteFilesList = new RemoteFilesList(changes, resources);
    given(resourceSyncFileLoader.getRemoteFilesList(capabilityListUri, null)).willReturn(remoteFilesList);


    instance.filterAndImport(capabilityListUri, null, false, null);

  }
}
