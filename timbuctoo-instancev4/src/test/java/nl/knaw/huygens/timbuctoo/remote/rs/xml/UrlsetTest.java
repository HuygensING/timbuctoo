package nl.knaw.huygens.timbuctoo.remote.rs.xml;

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
      .withAt(ZonedDateTime.now(ZoneOffset.UTC))
      .withCompleted(ZonedDateTime.now(ZoneOffset.UTC))
      .withFrom(ZonedDateTime.now(ZoneOffset.UTC))
      .withUntil(ZonedDateTime.now(ZoneOffset.UTC));

    RsMd urlRsMd = new RsMd("capabilitylist")
      .withChange("updated")
      .withEncoding("encoding")
      .withHash("hash")
      .withLength(2L)
      .withPath("another/path")
      .withType("text/plain");

    RsLn urlLink1 = new RsLn("describedby", "http://example.com/info_about_set1_of_resources.xml")
      .withEncoding("UTF-8")
      .withHash("md5:1584abdf8ebdc9802ac0c6a7402c03b6")
      .withLength(1000L)
      .withModified(ZonedDateTime.now(ZoneOffset.UTC))
      .withPath("foo/bar")
      .withPri(1)
      .withType("application/xml");

    UrlItem url1 = new UrlItem("http://example.com/capabilitylist1.xml")
      .withMetadata(urlRsMd)
      .addLink(urlLink1)
      .addLink(new RsLn("duplicate", "http://also.com/capabilitylist1.xml"));

    UrlItem url2 = new UrlItem("http://example.com/capabilitylist2.xml");

    return new Urlset(rsMd)
      .addLink(new RsLn("describedby", "http://example.com/info_about_source.xml"))
      .addLink(new RsLn("up", "http://example.com/attic"))
      .addItem(url1)
      .addItem(url2);

  }

}
