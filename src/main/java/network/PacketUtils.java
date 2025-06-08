package network;

import util.UUID;

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
import java.util.Arrays;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;

public class PacketUtils {

	public PacketHandler handler;
	public Socket socket;
	public DataOutputStream out;
	public DataInputStream in;
	int compressionThreshold = -1;

	public PacketUtils() {
		this.handler = new PacketHandler(this);
	}
	public long ping(String host, int port) {
		try {
			if(socket == null) {
				socket = new Socket(host, port);
				out = new DataOutputStream(socket.getOutputStream());
				in = new DataInputStream(socket.getInputStream());
			}

			// === HANDSHAKE ===
			ByteArrayOutputStream handshakeBytes = new ByteArrayOutputStream();
			DataOutputStream handshake = new DataOutputStream(handshakeBytes);
			writeVarInt(handshake, 0x00);          // Packet ID
			writeVarInt(handshake, 47);            // Protocol version (1.8.x)
			writeString(handshake, host);          // Host
			handshake.writeShort(port);            // Port
			writeVarInt(handshake, 1);             // Next state (status)
			writePacket(out, handshakeBytes.toByteArray());

			// === STATUS REQUEST ===
			ByteArrayOutputStream statusRequestBytes = new ByteArrayOutputStream();
			DataOutputStream statusRequest = new DataOutputStream(statusRequestBytes);
			writeVarInt(statusRequest, 0x00); // Packet ID
			writePacket(out, statusRequestBytes.toByteArray());

			// === RESPONSE ===
			readVarInt(in); // total packet length
			readVarInt(in); // packet ID
			int jsonLength = readVarInt(in);
			byte[] jsonBytes = new byte[jsonLength];
			in.readFully(jsonBytes);
			String json = new String(jsonBytes, StandardCharsets.UTF_8);
			System.out.println("Server Response:\n" + json);

			// === PING ===
			long pingStart = System.currentTimeMillis();
			ByteArrayOutputStream pingBytes = new ByteArrayOutputStream();
			DataOutputStream ping = new DataOutputStream(pingBytes);
			writeVarInt(ping, 0x01); // Packet ID
			ping.writeLong(pingStart);
			writePacket(out, pingBytes.toByteArray());

			// === PONG ===
			readVarInt(in); // packet length
			readVarInt(in); // packet ID
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
			out = new DataOutputStream(socket.getOutputStream());
			in = new DataInputStream(socket.getInputStream());

			// Send Handshake
			ByteArrayOutputStream handshakeBytes = new ByteArrayOutputStream();
			DataOutputStream handshake = new DataOutputStream(handshakeBytes);
			writeVarInt(handshake, 0x00); // Handshake packet ID
			writeVarInt(handshake, 47); // Protocol version
			writeString(handshake, host);
			handshake.writeShort(port);
			writeVarInt(handshake, 2); // Login state
			writePacket(out, handshakeBytes.toByteArray());

			// Send Login Start
			ByteArrayOutputStream loginStart = new ByteArrayOutputStream();
			DataOutputStream login = new DataOutputStream(loginStart);
			writeVarInt(login, 0x00); // Login Start
			writeString(login, username);
			writePacket(out, loginStart.toByteArray());

			// Read Encryption Request
			readVarInt(in); // packet length
			int packetId = readVarInt(in);
			if (packetId != 0x01) throw new IOException("Expected Encryption Request");

			String serverId = readString(in);
			byte[] publicKeyBytes = readArray(in);
			byte[] verifyToken = readArray(in);

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
			DataOutputStream encResp = new DataOutputStream(encResponseBytes);
			writeVarInt(encResp, 0x01); // Encryption Response
			writeArray(encResp, encryptedSecret);
			writeArray(encResp, encryptedToken);
			writePacket(out, encResponseBytes.toByteArray());

			// Enable AES encryption
			enableEncryption(sharedSecret);

			int len = readVarInt(in);
			int packID = readVarInt(in);
			if (packID == 0x03) {
				int threshold = readVarInt(in);
				System.out.println("Server set compression threshold: " + threshold);
				this.compressionThreshold = threshold;
			}

			DataInputStream packetIn = new DataInputStream(new ByteArrayInputStream(readPacket()));

			packetId = readVarInt(packetIn);
			if (packetId != 0x02) throw new IOException("Expected Login Success");

			String uuid = readString(packetIn);
			String name = readString(packetIn);
			System.out.printf("Login success: %s (%s)%n", name, uuid);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void play() {
		try {
			while(socket.isConnected()) {
				DataInputStream packetIn = new DataInputStream(new ByteArrayInputStream(readPacket()));
				int packetId = readVarInt(packetIn);

				switch (packetId) {
					case 0x00 -> handler.handleKeepAlive(packetIn);
					case 0x01 -> handler.handleJoinGame(packetIn);
					case 0x02 -> handler.handleChatMessage(packetIn);
					case 0x03 -> handler.handleTimeUpdate(packetIn); // NEW
					case 0x05 -> handler.handleEntityEquipment(packetIn); // NEW
					case 0x08 -> handler.handlePlayerPositionLook(packetIn); // NEW
					case 0x09 -> handler.handleUseBed(packetIn); // NEW
					case 0x2F -> handler.handleSetSlot(packetIn); // NEW
					case 0x30 -> handler.handleWindowItems(packetIn); // NEW
					case 0x37 -> handler.handleStatistics(packetIn); // NEW
					case 0x38 -> handler.handlePlayerInfo(packetIn); // NEW
					case 0x3F -> handler.handlePluginMessage(packetIn); // NEW
					case 0x41 -> handler.handleServerDifficulty(packetIn);
					case 0x26 -> handler.handlePlayerAbilities(packetIn);
					default -> System.out.printf("Unknown play packet ID 0x%02X, bytes: %s\n", packetId, new String(packetIn.readAllBytes()));
				}
			}
		} catch (IOException | DataFormatException e) {
			e.printStackTrace();
		}
	}

	public byte[] readPacket() throws IOException, DataFormatException {
		int packetLength = readVarInt(in);
		int dataLength = readVarInt(in); // uncompressed length

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

		in = new DataInputStream(new CipherInputStream(socket.getInputStream(), aesIn));
		out = new DataOutputStream(new CipherOutputStream(socket.getOutputStream(), aesOut));
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
	public void writePacket(DataOutputStream out, byte[] rawPacketData) throws IOException {
		ByteArrayOutputStream packetWithLength = new ByteArrayOutputStream();
		DataOutputStream packetOut = new DataOutputStream(packetWithLength);

		if (compressionThreshold >= 0) {
			if (rawPacketData.length >= compressionThreshold) {
				// Compressed
				ByteArrayOutputStream compressedBuffer = new ByteArrayOutputStream();
				Deflater deflater = new Deflater();
				DeflaterOutputStream deflaterStream = new DeflaterOutputStream(compressedBuffer, deflater);
				deflaterStream.write(rawPacketData);
				deflaterStream.close();
				deflater.end();

				byte[] compressedData = compressedBuffer.toByteArray();
				writeVarInt(packetOut, rawPacketData.length); // uncompressed length
				packetOut.write(compressedData);
			} else {
				// Not compressed but must include 0 uncompressed length
				writeVarInt(packetOut, 0);
				packetOut.write(rawPacketData);
			}
		} else {
			// No compression
			packetOut.write(rawPacketData);
		}

		// Wrap with total packet length
		byte[] finalPayload = packetWithLength.toByteArray();
		writeVarInt(out, finalPayload.length);
		out.write(finalPayload);
	}

	public void writeVarInt(OutputStream out, int value) throws IOException {
		while ((value & -128) != 0) {
			out.write((value & 127) | 128);
			value >>>= 7;
		}
		out.write(value);
	}

	public int readVarInt(InputStream in) throws IOException {
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

	public void writeArray(DataOutputStream out, byte[] data) throws IOException {
		writeVarInt(out, data.length);
		out.write(data);
	}

	public byte[] readArray(DataInputStream in) throws IOException {
		int len = readVarInt(in);
		byte[] buf = new byte[len];
		in.readFully(buf);
		return buf;
	}

	public boolean readBool(InputStream in) throws IOException {
		return (byte) in.read() == 1;
	}

	public void writeString(DataOutputStream out, String s) throws IOException {
		byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
		writeVarInt(out, bytes.length);
		out.write(bytes);
	}
	public String readString(InputStream in) throws IOException {
		int length = readVarInt(in);
		byte[] data = new byte[length];
		in.read(data);
		return new String(data, StandardCharsets.UTF_8);
	}
}
