package net.saucecode.asteroidsmp;
import net.saucecode.asteroidsmp.packet.PacketUpdatePlayer;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;

import tools.Texture;


public class Player extends Vector2f {

	private static final long serialVersionUID = -439822516021332734L;
	AsteroidField game;
	float dx, dy;
	short angle;
	Texture texture;
	
	float networkdx, networkdy;
	short networkAngle;
	boolean dead = false;
	boolean hasCustomTexture;
	
	public Player(AsteroidField asteroidField, float x, float y) {
		super(x,y);
		game = asteroidField;
	}

	public void update(){
		if(dead) return;
		x += dx;
		y += dy;
		if(x > 720) dx = -dx;
		if(x < 0) dx = -dx;
		if(y > 720) dy = -dy;
		if(y < 0) dy = -dy;
		
		if(Keyboard.isKeyDown(Keyboard.KEY_LEFT)){
			angle+=5;
		}
		if(Keyboard.isKeyDown(Keyboard.KEY_RIGHT)){
			angle-=5;
		}
		while(angle < 0) angle += 360;
		while(angle > 360) angle -= 360;
		if(Keyboard.isKeyDown(Keyboard.KEY_UP)){
			dx += (float) Math.sin(Math.toRadians(angle)) * 0.04f;
			dy += (float) Math.cos(Math.toRadians(angle)) * 0.04f;
			
			game.createProjectiles(x, y, (short) (angle+180), 2.5f,2);
		}
		if(Keyboard.isKeyDown(Keyboard.KEY_DOWN)){
			dx += -(float) Math.sin(Math.toRadians(angle)) * 0.02f;
			dy += -(float) Math.cos(Math.toRadians(angle)) * 0.02f;
			game.createProjectiles(x, y, angle, 1.6f,1);
		}
		
		if(networkAngle != angle || dx != networkdx || dy != networkdy){
			networkAngle = angle;
			networkdx = dx;
			networkdy = dy;
			PacketUpdatePlayer packet = new PacketUpdatePlayer();
			packet.angle = angle;
			packet.dx = networkdx;
			packet.dy = networkdy;
			game.network.client.sendUDP(packet);
		}
	}

	public void render() {
		if(dead) return;
		GL11.glColor3f(1, 1, 1);
		if(texture == null){
			GL11.glBegin(GL11.GL_TRIANGLES);

			GL11.glVertex2d(x + Math.sin(Math.toRadians(angle))*8, y + Math.cos(Math.toRadians(angle))*8);
			GL11.glVertex2d(x + Math.sin(Math.toRadians(angle+140))*8, y + Math.cos(Math.toRadians(angle+140))*8);
			GL11.glVertex2d(x + Math.sin(Math.toRadians(angle+220))*8, y + Math.cos(Math.toRadians(angle+220))*8);

			GL11.glEnd();
		}else{
			GL11.glLoadIdentity();
			GL11.glTranslatef(x, y, 0);
			GL11.glRotatef(360-angle, 0, 0, 1);
			texture.bind();
			GL11.glBegin(GL11.GL_QUADS);
			GL11.glTexCoord2f(0, 0);
			GL11.glVertex2f(-16, 16);

			GL11.glTexCoord2f(1, 0);
			GL11.glVertex2f(16, 16);

			GL11.glTexCoord2f(1, 1);
			GL11.glVertex2f(16, -16);

			GL11.glTexCoord2f(0, 1);
			GL11.glVertex2f(-16, -16);
			GL11.glEnd();
			
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
			GL11.glLoadIdentity();
		}
	}

	public void kill() {
		dead = true;
		x = -10;
		y = -10;
		dx = 0;
		dy = 0;
	}

	public void respawn() {
		dead = false;
		x = 720/2;
		y = 720/4;
	}
}
