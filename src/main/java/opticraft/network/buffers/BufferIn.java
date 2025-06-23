package opticraft.network.buffers;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class BufferIn extends DataInputStream {

	public BufferIn(InputStream in) {
		super(in);
	}

	public int readVarInt() throws IOException {
		int numRead = 0;
		int result = 0;
		byte read;
		do {
			read = (byte) in.read();
			int value = (read & 0b01111111);
			result |= (value << (7 * numRead));

			numRead++;
		} while ((read & 0b10000000) != 0);

		return result;
	}

	public byte[] readArray() throws IOException {
		int len = readVarInt();
		byte[] buf = new byte[len];
		in.read(buf);
		return buf;
	}

	public boolean readBool() throws IOException {
		return (byte) in.read() == 1;
	}
	public String readString() throws IOException {
		int length = readVarInt();
		byte[] data = new byte[length];
		in.read(data);
		return new String(data, StandardCharsets.UTF_8);
	}

	public UUID readUUID() throws IOException {
		return new UUID(readLong(), readLong());
	}

	public String readString(int maxLength) throws IOException {
		int length = Math.min(readVarInt(), maxLength);
		byte[] data = new byte[length];
		in.read(data);
		return new String(data, StandardCharsets.UTF_8);
	}
}
