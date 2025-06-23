package opticraft.network;

import opticraft.network.buffers.BufferIn;
import opticraft.network.buffers.BufferOut;
import opticraft.network.packets.Packet;
import opticraft.network.packets.handshake.PacketHandshake;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

public class PacketUtils {

	public PacketHandler handler;
	public Socket socket;
	public BufferOut out;
	public BufferIn in;
	public int compressionThreshold = -1;

	public PacketUtils() {
		this.handler = new PacketHandler(this);
	}
	public long ping(String host, int port) {
		try {
			if(socket == null) {
				socket = new Socket(host, port);
				out = new BufferOut(socket.getOutputStream());
				in = new BufferIn(socket.getInputStream());
			}

			sendPacket(new PacketHandshake(host, port, 1));

			// === STATUS REQUEST ===
			ByteArrayOutputStream statusRequestBytes = new ByteArrayOutputStream();
			BufferOut statusRequest = new BufferOut(statusRequestBytes);
			statusRequest.writeVarInt(0x00);
			out.writePacket(statusRequestBytes.toByteArray());

			// === RESPONSE ===
			in.readVarInt(); // total packet length
			in.readVarInt(); // packet ID
			int jsonLength = in.readVarInt();
			byte[] jsonBytes = new byte[jsonLength];
			in.readFully(jsonBytes);
			String json = new String(jsonBytes, StandardCharsets.UTF_8);
			System.out.println("Server Response:\n" + json);

			// === PING ===
			long pingStart = System.currentTimeMillis();
			ByteArrayOutputStream pingBytes = new ByteArrayOutputStream();
			BufferOut ping = new BufferOut(pingBytes);
			ping.writeVarInt(0x01); // Packet ID
			ping.writeLong(pingStart);
			out.writePacket(pingBytes.toByteArray());

			// === PONG ===
			in.readVarInt(); // packet length
			in.readVarInt(); // packet ID
			long pongPayload = in.readLong();
			long pingEnd = System.currentTimeMillis();

			long latency = pingEnd - pingStart;
			System.out.println("Ping successful. Time echoed: " + pongPayload + " | Round-trip: " + latency + "ms");
			return latency;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	public void login(String host, int port, String username, String uuidNoDashes, String accessToken) {
		try {
			socket = new Socket(host, port);
			out = new BufferOut(socket.getOutputStream());
			in = new BufferIn(socket.getInputStream());

			// Send Handshake
			ByteArrayOutputStream handshakeBytes = new ByteArrayOutputStream();
			BufferOut handshake = new BufferOut(handshakeBytes);
			handshake.writeVarInt(0x00); // Handshake packet ID
			handshake.writeVarInt(47); // Protocol version
			handshake.writeString(host);
			handshake.writeShort(port);
			handshake.writeVarInt(2); // Login state
			out.writePacket(handshakeBytes.toByteArray());

			// Send Login Start
			ByteArrayOutputStream loginStart = new ByteArrayOutputStream();
			BufferOut login = new BufferOut(loginStart);
			login.writeVarInt(0x00); // Login Start
			login.writeString(username);
			out.writePacket(loginStart.toByteArray());

			// Read Encryption Request
			in.readVarInt(); // packet length
			int packetId = in.readVarInt();
			if (packetId != 0x01) throw new IOException("Expected Encryption Request");

			String serverId = in.readString();
			byte[] publicKeyBytes = in.readArray();
			byte[] verifyToken = in.readArray();

			PublicKey publicKey = KeyFactory.getInstance("RSA")
					.generatePublic(new X509EncodedKeySpec(publicKeyBytes));

			// Generate shared secret
			byte[] sharedSecret = new byte[16];
			new SecureRandom().nextBytes(sharedSecret);

			// Encrypt secret and token
			Cipher cipher = Cipher.getInstance("RSA");
			cipher.init(Cipher.ENCRYPT_MODE, publicKey);
			byte[] encryptedSecret = cipher.doFinal(sharedSecret);
			byte[] encryptedToken = cipher.doFinal(verifyToken);

			// Compute server hash
			String serverHash = getServerHash(serverId, sharedSecret, publicKeyBytes);

			// Join session server
			sendSessionJoinRequest(accessToken, uuidNoDashes, serverHash);

			// Send Encryption Response
			ByteArrayOutputStream encResponseBytes = new ByteArrayOutputStream();
			BufferOut encResp = new BufferOut(encResponseBytes);
			encResp.writeVarInt(0x01); // Encryption Response
			encResp.writeArray(encryptedSecret);
			encResp.writeArray(encryptedToken);
			out.writePacket(encResponseBytes.toByteArray());

			// Enable AES encryption
			enableEncryption(sharedSecret);

			int len = in.readVarInt();
			int packID = in.readVarInt();
			if (packID == 0x03) {
				int threshold = in.readVarInt();
				System.out.println("Server set compression threshold: " + threshold);
				this.compressionThreshold = threshold;
			}

			BufferIn packetIn = new BufferIn(new ByteArrayInputStream(readPacket()));

			packetId = packetIn.readVarInt();
			if (packetId != 0x02) throw new IOException("Expected Login Success");

			String uuid = packetIn.readString();
			String name = packetIn.readString();
			System.out.printf("Login success: %s (%s)%n", name, uuid);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void play() {
		try {
			while(socket.isConnected()) {
				BufferIn packetIn = new BufferIn(new ByteArrayInputStream(readPacket()));
				int packetId = packetIn.readVarInt();

				switch (packetId) {
					case 0x00 -> handler.handleKeepAlive(packetIn);
					case 0x01 -> handler.handleJoinGame(packetIn);
					case 0x02 -> handler.handleChat(packetIn);
					case 0x03 -> handler.handleTime(packetIn);
					case 0x26 -> handler.handlePlayerAbilities(packetIn);
					case 0x21 -> handler.handleChunkData(packetIn);
					case 0x38 -> handler.handlePlayerList(packetIn);
					case 0x2b -> handler.handleChangeGameState(packetIn);
					default -> System.out.printf("Unknown play packet ID 0x%02X, bytes: %s\n", packetId, new String(packetIn.readAllBytes()));
				}
			}
		} catch (IOException | DataFormatException e) {
			e.printStackTrace();
		}
	}

	public void sendPacket(Packet p) throws IOException {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		BufferOut o = new BufferOut(bytes);

		o.writeVarInt(p.getPacketOutID());
		p.writePacket(o);
		out.writePacket(bytes.toByteArray());
	}

	public byte[] readPacket() throws IOException, DataFormatException {
		int packetLength = in.readVarInt();
		int dataLength = in.readVarInt(); // uncompressed length

		byte[] payload;
		if (dataLength == 0) {
			// Not compressed
			payload = new byte[packetLength - varIntSize(dataLength)];
			in.readFully(payload);
		} else {
			// Compressed
			byte[] compressed = new byte[packetLength - varIntSize(dataLength)];
			in.readFully(compressed);

			Inflater inflater = new Inflater();
			inflater.setInput(compressed);
			byte[] uncompressed = new byte[dataLength];
			inflater.inflate(uncompressed);
			inflater.end();

			payload = uncompressed;
		}

		return payload;
	}

	public int varIntSize(int value) {
		int size = 0;
		do {
			value >>>= 7;
			size++;
		} while (value != 0);
		return size;
	}

	public void enableEncryption(byte[] secret) throws Exception {
		SecretKeySpec key = new SecretKeySpec(secret, "AES");
		Cipher aesIn = Cipher.getInstance("AES/CFB8/NoPadding");
		aesIn.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(secret));

		Cipher aesOut = Cipher.getInstance("AES/CFB8/NoPadding");
		aesOut.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(secret));

		in = new BufferIn(new CipherInputStream(socket.getInputStream(), aesIn));
		out = new BufferOut(new CipherOutputStream(socket.getOutputStream(), aesOut));
	}

	public void sendSessionJoinRequest(String accessToken, String uuid, String serverHash) throws IOException {
		String body = String.format(
				"{\"accessToken\":\"%s\",\"selectedProfile\":\"%s\",\"serverId\":\"%s\"}",
				accessToken, uuid, serverHash
		);

		HttpURLConnection con = (HttpURLConnection) new URL("https://sessionserver.mojang.com/session/minecraft/join").openConnection();
		con.setDoOutput(true);
		con.setRequestMethod("POST");
		con.setRequestProperty("Content-Type", "application/json");
		con.getOutputStream().write(body.getBytes(StandardCharsets.UTF_8));
		con.connect();

		System.out.println("Response: " + con.getResponseCode());
		if (con.getResponseCode() != 204)
			throw new IOException("Session join failed: HTTP " + con.getResponseCode());
	}

	public String getServerHash(String serverId, byte[] sharedSecret, byte[] publicKey) {
		try {
			MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
			sha1.update(serverId.getBytes(StandardCharsets.ISO_8859_1));
			sha1.update(sharedSecret);
			sha1.update(publicKey);
			byte[] digest = sha1.digest();

			BigInteger num = new BigInteger(digest);
			return num.toString(16);
		} catch (Exception e) {
			throw new RuntimeException("Unable to compute server hash", e);
		}
	}
}
