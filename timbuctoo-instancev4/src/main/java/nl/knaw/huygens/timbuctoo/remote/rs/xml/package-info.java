@XmlSchema(
  namespace = "http://www.sitemaps.org/schemas/sitemap/0.9",
  xmlns = {
    @XmlNs(prefix = "", namespaceURI = "http://www.sitemaps.org/schemas/sitemap/0.9"),
    @XmlNs(prefix = "rs", namespaceURI = "http://www.openarchives.org/rs/terms/")
  },
  elementFormDefault = XmlNsForm.QUALIFIED,
  attributeFormDefault = XmlNsForm.UNQUALIFIED)

@XmlJavaTypeAdapters({
  @XmlJavaTypeAdapter(type = ZonedDateTime.class, value = ZonedDateTimeAdapter.class)
  })

package nl.knaw.huygens.timbuctoo.remote.rs.xml;

import javax.xml.bind.annotation.XmlNs;
import javax.xml.bind.annotation.XmlNsForm;
import javax.xml.bind.annotation.XmlSchema;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapters;
import java.time.ZonedDateTime;
