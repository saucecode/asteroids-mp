package net.saucecode.asteroidsmp;
import java.awt.Color;


public class ServerText {

	private short id;
	private String message;
	private float x, y, size;
	private boolean deleted = false;
	private Color color;
	
	public ServerText(short id, String message, float x, float y, float size, Color color){
		this.id = id;
		this.message = message;
		this.x = x;
		this.y = y;
		this.size = size;
		this.color = color;
	}

	public short getID() {
		return id;
	}

	public void setID(short id) {
		this.id = id;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public float getX() {
		return x;
	}

	public void setX(float x) {
		this.x = x;
	}

	public float getY() {
		return y;
	}

	public void setY(float y) {
		this.y = y;
	}

	public float getSize() {
		return size;
	}

	public void setSize(float size) {
		this.size = size;
	}

	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}
}
