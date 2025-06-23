package opticraft.gui.items;

public class Attribute {

	public String key, value;

	public Attribute(String key, String value) {
		this.key = key;
		this.value = value.replaceAll("^['\"]|['\"]$", "");;
	}

	public String getKey() {
		return key;
	}

	public String getString() {
		return value;
	}

	public String getInt() {
		return value;
	}

	public String getFloat() {
		return value;
	}
}
