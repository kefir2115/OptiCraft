package network;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class PacketHandler {
	PacketUtils pack;

	public PacketHandler(PacketUtils pack) {
		this.pack = pack;
	}

	public void handleKeepAlive(DataInputStream in) throws IOException {
		int id = this.pack.readVarInt(in);
		System.out.println("KeepAlive received: " + id);
		sendKeepAliveResponse(id);
	}

	public void sendKeepAliveResponse(int keepAliveId) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);

		pack.writeVarInt(dos, 0x0f);
		pack.writeVarInt(dos, keepAliveId);

		pack.writePacket(pack.out, baos.toByteArray());
	}

	public void handleJoinGame(DataInputStream in) throws IOException {
		int entityId = in.readInt();
		byte gamemode = in.readByte();
		byte dimension = in.readByte();
		byte difficulty = in.readByte();
		byte maxPlayers = in.readByte();
		String levelType = pack.readString(in);
		boolean reducedDebugInfo = in.readBoolean();

		System.out.printf("Join Game: EntityID=%d Gamemode=%d Dimension=%d Difficulty=%d MaxPlayers=%d LevelType=%s ReducedDebugInfo=%b%n",
				entityId, gamemode, dimension, difficulty, maxPlayers, levelType, reducedDebugInfo);
	}

	public void handleChatMessage(DataInputStream in) throws IOException {
		String json = pack.readString(in);
		System.out.println("Chat Message: " + json);
	}

	public void handleServerDifficulty(DataInputStream in) throws IOException {
		byte difficulty = in.readByte();
		boolean locked = in.readBoolean();
		System.out.println("Server Difficulty: " + difficulty + ", locked: " + locked);
	}

	public void handlePlayerAbilities(DataInputStream in) throws IOException {
		byte flag = in.readByte();
		boolean invulnerable = (flag & 1) > 0;
		boolean flying = (flag & 2) > 0;
		boolean allowFlying = (flag & 4) > 0;
		boolean creativeMode = (flag & 8) > 0;
		float walkSpeed = in.readFloat();
		float flySpeed = in.readFloat();

		System.out.printf("%b %b %b %b %f %f", invulnerable, flying, allowFlying, creativeMode, walkSpeed, flySpeed);
	}

	public void handleTimeUpdate(DataInputStream in) throws IOException {}
	public void handleSpawnPainting(DataInputStream in) throws IOException {}
	public void handlePlayerPositionLook(DataInputStream in) throws IOException {}
	public void handleUseBed(DataInputStream in) throws IOException {}
	public void handleSetSlot(DataInputStream in) throws IOException {}
	public void handleWindowItems(DataInputStream in) throws IOException {}
	public void handleStatistics(DataInputStream in) throws IOException {}
	public void handlePluginMessage(DataInputStream in) throws IOException {}
}
