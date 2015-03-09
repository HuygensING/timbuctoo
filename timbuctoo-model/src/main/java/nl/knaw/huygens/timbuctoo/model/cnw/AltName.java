package nl.knaw.huygens.timbuctoo.model.cnw;


//@JsonIgnoreProperties(ignoreUnknown = true)
public class AltName {
	private String nametype;
	private String name;

//	public AltName() {}

	public String getNametype() {
		return nametype;
	}

	public void setNametype(String nametype) {
		this.nametype = nametype;
	}

	public String getDisplayName() {
		return name;
	}

	public void setDisplayName(String name) {
		this.name = name;
	}
}
