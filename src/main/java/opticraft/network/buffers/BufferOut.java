package opticraft.network.buffers;

import opticraft.OptiCraft;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

public class BufferOut extends DataOutputStream {

	public BufferOut(OutputStream out) {
		super(out);
	}

	public void writeVarInt(int value) throws IOException {
		while ((value & -128) != 0) {
			out.write((value & 127) | 128);
			value >>>= 7;
		}
		out.write(value);
	}

	public void writeArray(byte[] data) throws IOException {
		writeVarInt(data.length);
		out.write(data);
	}
	public void writeString(String s) throws IOException {
		byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
		writeVarInt(bytes.length);
		out.write(bytes);
	}

	public void writePacket(byte[] rawPacketData) throws IOException {
		ByteArrayOutputStream packetWithLength = new ByteArrayOutputStream();
		BufferOut packetOut = new BufferOut(packetWithLength);

		if (OptiCraft.get().pack.compressionThreshold >= 0) {
			if (rawPacketData.length >= OptiCraft.get().pack.compressionThreshold) {
				// Compressed
				ByteArrayOutputStream compressedBuffer = new ByteArrayOutputStream();
				Deflater deflater = new Deflater();
				DeflaterOutputStream deflaterStream = new DeflaterOutputStream(compressedBuffer, deflater);
				deflaterStream.write(rawPacketData);
				deflaterStream.close();
				deflater.end();

				byte[] compressedData = compressedBuffer.toByteArray();
				packetOut.writeVarInt(rawPacketData.length); // uncompressed length
				packetOut.write(compressedData);
			} else {
				// Not compressed but must include 0 uncompressed length
				packetOut.writeVarInt(0);
				packetOut.write(rawPacketData);
			}
		} else {
			// No compression
			packetOut.write(rawPacketData);
		}

		// Wrap with total packet length
		byte[] finalPayload = packetWithLength.toByteArray();
		writeVarInt(finalPayload.length);
		out.write(finalPayload);
	}
}
