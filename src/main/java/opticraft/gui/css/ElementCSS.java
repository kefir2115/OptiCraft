package opticraft.gui.css;

import java.util.ArrayList;

public class ElementCSS {

	public boolean isOnHover;
	public String query;

	public ArrayList<CSSEntry> entries;

	public ElementCSS(String query) {
		this.query = query;
		isOnHover = this.query.endsWith(":hover");
		entries = new ArrayList<>();
	}

	@Override
	public String toString() {
		return "ElementCSS{" +
				"query='" + query + '\'' +
				", entries=" + entries +
				'}';
	}

	public static class CSSEntry {

		public String key, value;

		public CSSEntry(String key, String value) {
			this.key = key;
			this.value = value;
		}

		@Override
		public String toString() {
			return "CSSEntry{" +
					"key='" + key + '\'' +
					", value='" + value + '\'' +
					'}';
		}
	}
}
