package opticraft.network.packets;

import opticraft.network.buffers.BufferOut;

import java.io.IOException;

public interface Packet {
	int getPacketInID();
	int getPacketOutID();
	void writePacket(BufferOut out) throws IOException;
}
