package opticraft.network.packets.out;

import opticraft.network.buffers.BufferOut;
import opticraft.network.packets.Packet;

public class PacketAnimation implements Packet {
	@Override
	public int getPacketInID() {
		return 0x0b;
	}

	@Override
	public int getPacketOutID() {
		return 0x0a;
	}

	@Override
	public void writePacket(BufferOut out) {

	}
}
