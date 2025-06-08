package util;

public class UUID {

	public static String gen() {
		return part(8) +
				part(4) +
				part(4) +
				part(4) +
				part(12);
	}

	private static String part(int len) {
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < len; i++) sb.append(String.format("%x", (int) Math.round(Math.random()*16)));
		return sb.toString();
	}
}
