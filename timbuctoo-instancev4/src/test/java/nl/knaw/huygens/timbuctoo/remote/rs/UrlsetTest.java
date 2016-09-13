package nl.knaw.huygens.timbuctoo.remote.rs;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.InputStream;
import java.io.StringWriter;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class UrlsetTest {

  @Test
  public void testSerializeDeserializeUrlset() throws Exception {

    Urlset urlset = createUrlset();

    JAXBContext jaxbContext = JAXBContext.newInstance(Urlset.class);
    Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

    jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
    StringWriter writer = new StringWriter();
    jaxbMarshaller.marshal(urlset, writer);
    String output1 = writer.toString();

    //System.out.println(output1);

    Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
    InputStream in = IOUtils.toInputStream(output1);
    RsRoot returned = (RsRoot) jaxbUnmarshaller.unmarshal(in);
    IOUtils.closeQuietly(in);

    writer = new StringWriter();
    jaxbMarshaller.marshal(returned, writer);
    String output2 = writer.toString();

    //System.out.println(output2);

    assertThat(output2, equalTo(output1));

  }

  @Test
  public void testWrappedRootElement() throws Exception {

    Urlset urlset = createUrlset();

    JAXBContext jaxbContext = JAXBContext.newInstance(ObjectFactory.class);
    Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

    jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
    StringWriter writer = new StringWriter();
    jaxbMarshaller.marshal(urlset, writer);
    String output1 = writer.toString();

    //System.out.println(output1);

    Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
    InputStream in = IOUtils.toInputStream(output1);
    JAXBElement<RsRoot> je = (JAXBElement<RsRoot>) jaxbUnmarshaller.unmarshal(in);

    IOUtils.closeQuietly(in);

    assertThat(je.getName(), equalTo(Urlset.QNAME));

    Urlset returned = (Urlset) je.getValue();
    writer = new StringWriter();
    jaxbMarshaller.marshal(returned, writer);
    String output2 = writer.toString();

    //System.out.println(output2);

    assertThat(output2, equalTo(output1));

  }

  private Urlset createUrlset() {

    RsMd rsMd = new RsMd("description")
      .setAt(ZonedDateTime.now(ZoneOffset.UTC))
      .setCompleted(ZonedDateTime.now(ZoneOffset.UTC))
      .setFrom(ZonedDateTime.now(ZoneOffset.UTC))
      .setUntil(ZonedDateTime.now(ZoneOffset.UTC));

    RsMd urlRsMd = new RsMd("capabilitylist")
      .setChange("updated")
      .setEncoding("encoding")
      .setHash("hash")
      .setLength(2L)
      .setPath("another/path")
      .setType("text/plain");

    RsLn urlLink1 = new RsLn("describedby", "http://example.com/info_about_set1_of_resources.xml")
      .setEncoding("UTF-8")
      .setHash("md5:1584abdf8ebdc9802ac0c6a7402c03b6")
      .setLength(1000L)
      .setModified(ZonedDateTime.now(ZoneOffset.UTC))
      .setPath("foo/bar")
      .setPri(1)
      .setType("application/xml");

    UrlItem url1 = new UrlItem("http://example.com/capabilitylist1.xml")
      .setMetadata(urlRsMd)
      .add(urlLink1)
      .add(new RsLn("duplicate", "http://also.com/capabilitylist1.xml"));

    UrlItem url2 = new UrlItem("http://example.com/capabilitylist2.xml");

    return new Urlset(rsMd)
      .add(new RsLn("describedby", "http://example.com/info_about_source.xml"))
      .add(new RsLn("up", "http://example.com/attic"))
      .add(url1)
      .add(url2);

  }

}
