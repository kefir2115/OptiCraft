package opticraft.network.packets;

import opticraft.network.buffers.BufferOut;

import java.io.IOException;

public class PacketChunkData implements Packet {

	@Override
	public int getPacketInID() {
		return 0x21;
	}

	@Override
	public int getPacketOutID() {
		return -1;
	}

	@Override
	public void writePacket(BufferOut out) throws IOException {

	}
}
