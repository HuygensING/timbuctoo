package nl.knaw.huygens.timbuctoo.datastores.rssource;

import nl.knaw.huygens.timbuctoo.remote.rs.xml.Capability;
import nl.knaw.huygens.timbuctoo.remote.rs.xml.ResourceSyncConstants;
import nl.knaw.huygens.timbuctoo.remote.rs.xml.ResourceSyncContext;
import nl.knaw.huygens.timbuctoo.remote.rs.xml.RsBuilder;
import nl.knaw.huygens.timbuctoo.remote.rs.xml.Urlset;
import nl.knaw.huygens.timbuctoo.util.UriHelper;
import nl.knaw.huygens.timbuctoo.dataset.DataSetRepository;
import nl.knaw.huygens.timbuctoo.dataset.ImportManager;
import nl.knaw.huygens.timbuctoo.dataset.dto.DataSet;
import nl.knaw.huygens.timbuctoo.dataset.dto.DataSetMetaData;
import nl.knaw.huygens.timbuctoo.dataset.dto.EntryImportStatus;
import nl.knaw.huygens.timbuctoo.dataset.dto.LogEntry;
import nl.knaw.huygens.timbuctoo.dataset.dto.LogList;
import nl.knaw.huygens.timbuctoo.datastores.implementations.bdb.UpdatedPerPatchStore;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RsDocumentBuilderTest {
  private static RsBuilder rsBuilder;

  private DataSetRepository dataSetRepository;
  private DataSet dataSet;
  private RsDocumentBuilder rsDocumentBuilder;

  @BeforeAll
  public static void initialize() throws Exception {
    rsBuilder = new RsBuilder(new ResourceSyncContext());
  }

  @BeforeEach
  public void init() throws Exception {
    dataSetRepository = mock(DataSetRepository.class);
    rsDocumentBuilder = new RsDocumentBuilder(dataSetRepository, new UriHelper(URI.create("http://example.com")));

    dataSet = mock(DataSet.class);
    DataSetMetaData dataSetMetaData1 = mock(DataSetMetaData.class);
    DataSetMetaData dataSetMetaData2 = mock(DataSetMetaData.class);
    when(dataSetMetaData1.getOwnerId()).thenReturn("u1");
    when(dataSetMetaData1.getDataSetId()).thenReturn("ds1");
    when(dataSetMetaData1.getBaseUri()).thenReturn("http://example.com/u1/ds1/");
    when(dataSetMetaData2.getOwnerId()).thenReturn("u2");
    when(dataSetMetaData2.getDataSetId()).thenReturn("ds2");
    when(dataSetMetaData2.getBaseUri()).thenReturn("http://example.com/u2/ds2/");
    when(dataSet.getMetadata()).thenReturn(dataSetMetaData1).thenReturn(dataSetMetaData2);
  }

  @Test
  public void emptySourceDescription() throws Exception {
    when(dataSetRepository.getDataSetsWithReadAccess(null)).thenReturn(Collections.emptyList());

    Urlset sourceDescription = rsDocumentBuilder.getSourceDescription(null);
    String xml = rsBuilder.toXml(sourceDescription, true);
    //System.out.println(xml);
    rsBuilder.setXmlString(xml).build();
    Urlset sd = rsBuilder.getUrlset().get();
    assertThat(sd.getCapability().get(), is(Capability.DESCRIPTION));
    assertThat(sd.getItemList().size(), is(0));
  }

  @Test
  public void sourceDescriptionWithCapabilityLists() throws Exception {
    when(dataSetRepository.getDataSetsWithReadAccess(null)).thenReturn(Arrays.asList(dataSet, dataSet));

    Urlset sourceDescription = rsDocumentBuilder.getSourceDescription(null);
    String xml = rsBuilder.toXml(sourceDescription, true);
    //System.out.println(xml);
    rsBuilder.setXmlString(xml).build();
    Urlset sd = rsBuilder.getUrlset().get();
    assertThat(sd.getCapability().get(), is(Capability.DESCRIPTION));
    assertThat(sd.getItemList().size(), is(2));
    assertThat(sd.getItemList().get(0).getLoc(), is("http://example.com/resourcesync/u1/ds1/capabilitylist.xml"));
    assertThat(sd.getItemList().get(1).getLoc(), is("http://example.com/resourcesync/u2/ds2/capabilitylist.xml"));
    assertThat(sd.getItemList().get(0).getMetadata().get().getCapability().get(),
      is(Capability.CAPABILITYLIST.xmlValue));
    assertThat(sd.getItemList().get(1).getLink("describedby").get().getHref(),
      is("http://example.com/resourcesync/u2/ds2/description.xml"));
    assertThat(sd.getItemList().get(1).getLink("describedby").get().getType().get(),
      is("application/rdf+xml"));
  }

  @Test
  public void capabilityList() throws Exception {
    when(dataSetRepository.getDataSet(null, "u1", "ds1")).thenReturn(Optional.of(dataSet));

    Urlset capabilityList = rsDocumentBuilder.getCapabilityList(null, "u1", "ds1").get();
    String xml = rsBuilder.toXml(capabilityList, true);
    //System.out.println(xml);
    rsBuilder.setXmlString(xml).build();
    Urlset cl = rsBuilder.getUrlset().get();
    assertThat(cl.getCapability().get(), is(Capability.CAPABILITYLIST));
    assertThat(cl.getLink("up").get().getHref(),
      is("http://example.com/.well-known/resourcesync"));
    assertThat(cl.getLink("describedby").get().getHref(),
      is("http://example.com/resourcesync/u1/ds1/description.xml"));
    assertThat(cl.getLink("describedby").get().getType().get(),
      is("application/rdf+xml"));
    assertThat(cl.getItemList().size(), is(2));

    assertThat(cl.getItemList().get(0).getLoc(), is("http://example.com/resourcesync/u1/ds1/resourcelist.xml"));
    assertThat(cl.getItemList().get(0).getMetadata().get().getCapability().get(),
      is(Capability.RESOURCELIST.xmlValue));
    assertThat(cl.getItemList().get(1).getLoc(), is("http://example.com/resourcesync/u1/ds1/changelist.xml"));
    assertThat(cl.getItemList().get(1).getMetadata().get().getCapability().get(),
      is(Capability.CHANGELIST.xmlValue));
  }

  @Test
  public void getChangeListGeneratesChangeListWithChangeFileNames() throws Exception {
    when(dataSetRepository.getDataSet(null, "u1", "ds1")).thenReturn(Optional.of(dataSet));

    UpdatedPerPatchStore updatedPerPatchStore = mock(UpdatedPerPatchStore.class);

    given(updatedPerPatchStore.getVersions()).willReturn(Stream.of(0, 1));

    LogList logList = new LogList();
    logList.setLastImportDate("2018-03-21T10:11:53.811Z");
    ImportManager importManager = mock(ImportManager.class);
    given(importManager.getLogList()).willReturn(logList);

    EntryImportStatus entryImportStatus1 = new EntryImportStatus();
    entryImportStatus1.setDate("2018-02-21T10:11:53.811Z");
    LogEntry logEntry1 = mock(LogEntry.class);
    given(logEntry1.getImportStatus()).willReturn(entryImportStatus1);

    EntryImportStatus entryImportStatus2 = new EntryImportStatus();
    entryImportStatus2.setDate("2018-01-21T10:11:53.811Z");
    LogEntry logEntry2 = mock(LogEntry.class);
    given(logEntry2.getImportStatus()).willReturn(entryImportStatus2);

    logList.addEntry(logEntry1);
    logList.addEntry(logEntry2);

    DataSetMetaData dataSetMetaData = mock(DataSetMetaData.class);
    given(dataSetMetaData.getBaseUri()).willReturn("http://example.com/u1/ds1/");
    given(dataSetMetaData.getOwnerId()).willReturn("u1");
    given(dataSetMetaData.getDataSetId()).willReturn("ds1");

    given(dataSet.getUpdatedPerPatchStore()).willReturn(updatedPerPatchStore);
    given(dataSet.getMetadata()).willReturn(dataSetMetaData);
    given(dataSet.getImportManager()).willReturn(importManager);

    Urlset changeList = rsDocumentBuilder.getChangeList(null, "u1", "ds1").get();

    String xml = rsBuilder.toXml(changeList, true);
    rsBuilder.setXmlString(xml).build();
    Urlset changeListSet = rsBuilder.getUrlset().get();
    assertThat(changeListSet.getCapability().get(), is(Capability.CHANGELIST));
    assertThat(changeList.getItemList().size(), is(2));
    assertThat(changeList.getItemList().get(0).getLoc(),
      is("http://example.com/resourcesync/u1/ds1/dataset.nq"));
    assertThat(changeList.getItemList().get(0).getLink(ResourceSyncConstants.PATCH_LINK).get().getHref(),
      is("http://example.com/resourcesync/u1/ds1/changes/changes0.nqud"));
    assertThat(changeList.getItemList().get(1).getLink(ResourceSyncConstants.PATCH_LINK).get().getHref(),
      is("http://example.com/resourcesync/u1/ds1/changes/changes1.nqud"));
  }
}
