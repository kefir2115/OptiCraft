package opticraft.network.packets.handshake;

import opticraft.network.buffers.BufferOut;
import opticraft.network.packets.Packet;

import java.io.IOException;

public class PacketHandshake implements Packet {
	private String host;
	private int port, state, protocolVersion = 47;

	public PacketHandshake(String host, int port, int state) {
		this.host = host;
		this.port = port;
		this.state = state;
	}

	public PacketHandshake(String host, int port) {
		this(host, port, 1);
	}

	@Override
	public int getPacketInID() {
		return 0x00;
	}

	@Override
	public int getPacketOutID() {
		return 0x00;
	}

	@Override
	public void writePacket(BufferOut out) throws IOException {
		out.writeVarInt(protocolVersion);
		out.writeString(host);
		out.writeShort(port);
		out.writeVarInt(state);
	}
}
