package opticraft.network.packets.out;

import opticraft.network.buffers.BufferOut;
import opticraft.network.packets.Packet;

import java.io.IOException;

public class PacketKeepAlive implements Packet {

	int id;
	public PacketKeepAlive(int id) {
		this.id = id;
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
		out.writeVarInt(id);
	}
}
