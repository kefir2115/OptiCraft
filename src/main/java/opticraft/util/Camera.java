package opticraft.util;

import org.joml.Vector3f;
import org.joml.Matrix4f;

public class Camera {
	private Vector3f position;
	public float pitch, yaw;
	private float speed = 5.0f;

	public Camera(Vector3f position) {
		this.position = position;
		this.pitch = 0f;
		this.yaw = -90f; // looking forward
	}

	public Matrix4f getViewMatrix() {
		Vector3f front = getViewFront();
		return new Matrix4f().lookAt(position, new Vector3f(position).add(front), new Vector3f(0, 1, 0));
	}

	public Vector3f getViewFront() {
		float cosPitch = (float) Math.cos(Math.toRadians(pitch));
		float sinPitch = (float) Math.sin(Math.toRadians(pitch));
		float cosYaw = (float) Math.cos(Math.toRadians(yaw));
		float sinYaw = (float) Math.sin(Math.toRadians(yaw));

		return new Vector3f(
				cosPitch * cosYaw,
				sinPitch,
				cosPitch * sinYaw
		).normalize();
	}

	public Vector3f getFront() {
		float cosYaw = (float) Math.cos(Math.toRadians(yaw));
		float sinYaw = (float) Math.sin(Math.toRadians(yaw));

		return new Vector3f(
				cosYaw,
				0,
				sinYaw
		).normalize();
	}


	public Vector3f getRight() {
		return getFront().cross(new Vector3f(0, 1, 0), new Vector3f()).normalize();
	}

	public Vector3f getUp() {
		return getRight().cross(getFront(), new Vector3f()).normalize();
	}

	public void processKeyboard(boolean forward, boolean backward, boolean left, boolean right, boolean up, boolean down, float deltaTime) {
		float velocity = speed * deltaTime;
		Vector3f dir = new Vector3f(0, 0, 0);
		if (forward) dir.add(new Vector3f(getFront()).mul(velocity));
		if (backward) dir.sub(new Vector3f(getFront()).mul(velocity));
		if (left) dir.sub(new Vector3f(getRight()).mul(velocity));
		if (right) dir.add(new Vector3f(getRight()).mul(velocity));
		if (up) dir.add(new Vector3f(0, 1, 0).mul(velocity));
		if (down) dir.add(new Vector3f(0, -1, 0).mul(velocity));

		if(!dir.equals(0, 0, 0)) position.add(dir.normalize());
	}

	public void processMouseMovement(float dx, float dy) {
		yaw += dx * 0.1f;
		pitch -= dy * 0.1f;

		if (pitch > 89.0f) pitch = 89.0f;
		if (pitch < -89.0f) pitch = -89.0f;
	}

	public Vector3f getPosition() {
		return position;
	}
}
