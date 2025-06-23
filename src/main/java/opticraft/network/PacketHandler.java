package opticraft.network;

import opticraft.network.buffers.BufferIn;
import opticraft.network.buffers.BufferOut;
import opticraft.network.packets.out.PacketKeepAlive;

import java.io.*;
import java.util.UUID;

public class PacketHandler {
	PacketUtils pack;

	public PacketHandler(PacketUtils pack) {
		this.pack = pack;
	}

	public void handleKeepAlive(BufferIn in) throws IOException {
		int id = in.readVarInt();
		this.pack.sendPacket(new PacketKeepAlive(id));
	}

	public void handleJoinGame(BufferIn in) throws IOException {
		int entityId = in.readInt();
		byte gamemode = in.readByte();
		byte dimension = in.readByte();
		byte difficulty = in.readByte();
		byte maxPlayers = in.readByte();
		String levelType = in.readString();
		boolean reducedDebugInfo = in.readBoolean();

		System.out.printf("Join Game: EntityID=%d Gamemode=%d Dimension=%d Difficulty=%d MaxPlayers=%d LevelType=%s ReducedDebugInfo=%b%n",
				entityId, gamemode, dimension, difficulty, maxPlayers, levelType, reducedDebugInfo);
	}

	public void handleTime(BufferIn in) throws IOException {
		long totalWorldTime = in.readLong();
		long worldTime = in.readLong();

	}

	public void handleChat(BufferIn in) throws IOException {
		String msg = in.readString();
		System.out.println("Chat message: " + msg);
	}

	public void handlePlayerAbilities(BufferIn in) throws IOException {
		byte flag = in.readByte();
		boolean invulnerable = (flag & 1) > 0;
		boolean flying = (flag & 2) > 0;
		boolean allowFlying = (flag & 4) > 0;
		boolean creativeMode = (flag & 8) > 0;
		float walkSpeed = in.readFloat();
		float flySpeed = in.readFloat();

		System.out.printf("%b %b %b %b %f %f%n", invulnerable, flying, allowFlying, creativeMode, walkSpeed, flySpeed);
	}

	public void handleChunkData(BufferIn in) throws IOException {
		int cx = in.readInt();
		int cz = in.readInt();
		boolean groundUp = in.readBool();
		short primaryBitmask = in.readShort();
		int dataLength = in.readVarInt();
		byte[] data = in.readNBytes(dataLength);

		BufferIn stream = new BufferIn(new ByteArrayInputStream(data));
		DataInputStream dis = new DataInputStream(stream);

		for (int sectionY = 0; sectionY < 16; sectionY++) {
			if ((primaryBitmask & (1 << sectionY)) != 0) {
				// 4096 blocks, 2 bytes each = 8192 bytes
				byte[] blockData = new byte[8192];
				dis.readFully(blockData);

				int[] blockIDs = new int[16 * 16 * 16]; // 4096 blocks in a section

				for (int i = 0, block = 0; block < 4096; i += 2, block++) {
					int high = blockData[i] & 0xFF;         // unsigned byte
					int low = blockData[i + 1] & 0x0F;      // only lower 4 bits

					int blockID = (high << 4) | low;
					blockIDs[block] = blockID;
				}

				byte[] blockLight = new byte[2048];
				dis.readFully(blockLight);

				byte[] skyLight = new byte[2048];
				dis.readFully(skyLight);
			}
		}

		if (groundUp) {
			byte[] biomeData = new byte[256];
			dis.readFully(biomeData);
		}
	}
	public void handlePlayerList(BufferIn in) throws IOException {
		/**
		 * 0 - add player
		 * 1 - update gamemode
		 * 2 - update latency
		 * 3 - update display name
		 * 4 - remove player
		 */
		int action = in.readVarInt();
		switch(action) {
			case 0 -> {
				UUID uuid = in.readUUID();
				String name = in.readString(16);

				int l = in.readVarInt();
				for(int i = 0; i < l; i++) {
					String key = in.readString(32767);
					String value = in.readString(32767);

					if(in.readBool()) {
						String signature = in.readString(32767);
						// add property key, value, signature
					} else {
						// add property key, value
					}
				}
			}
			case 1 -> {
				UUID uuid = in.readUUID();
				int gamemode = in.readVarInt();
			}
			case 2 -> {
				UUID uuid = in.readUUID();
				int lat = in.readVarInt();
			}
			case 3 -> {
				UUID uuid = in.readUUID();
				if (in.readBoolean())
				{
					String name = in.readString(32767);
				}
			}
			case 4 -> {
				UUID uuid = in.readUUID();
				// remove player with this uuid
			}
		}
	}

	public void handleChangeGameState(BufferIn in) throws IOException {
		int state = in.readUnsignedByte();
		float smth = in.readFloat();
	}
}
