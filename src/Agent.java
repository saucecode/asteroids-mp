import java.io.IOException;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;

import tools.Texture;


public class Agent extends Vector2f {

	private static final long serialVersionUID = -2034873889589095071L;
	int id;
	String username;
	float dx = 0, dy = 0;
	float ddx = 0, ddy = 0; // change in change in velocity, to detect projectile production
	short angle = 0;
	boolean removed = false;
	boolean dead = false;
	Texture texture = null;
	String avatarFile = null;
	
	public Agent(int id, String username){
		super(720/2,720/2);
		this.id = id;
		this.username = username;
	}
	
	public void update(){
		if(dead) return;
		if(texture == null && avatarFile != null){
			// load the player's texture
			try {
				texture = AsteroidField.loader.getTexture(avatarFile, false);
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("FAILED TO LOAD TEXTURE FOR PLAYER " + username + ": " + id + " from " + avatarFile);
				texture = null;
				avatarFile = null;
			}
		}
		x += dx;
		y += dy;
		
		if(x > 720) dx = -dx;
		if(x < 0) dx = -dx;
		if(y > 720) dy = -dy;
		if(y < 0) dy = -dy;
	}
	
	public void render() {
		if(dead) return;
		if(texture == null){
			/*GL11.glColor3f(1, 1, 0);
			GL11.glBegin(GL11.GL_TRIANGLES);
			
			GL11.glVertex2d(x + Math.sin(Math.toRadians(angle))*8, y + Math.cos(Math.toRadians(angle))*8);
			GL11.glVertex2d(x + Math.sin(Math.toRadians(angle+140))*8, y + Math.cos(Math.toRadians(angle+140))*8);
			GL11.glVertex2d(x + Math.sin(Math.toRadians(angle+220))*8, y + Math.cos(Math.toRadians(angle+220))*8);
			
			GL11.glEnd();*/
			GL11.glLoadIdentity();
			GL11.glTranslatef(x, y, 0);
			GL11.glRotatef(360-angle, 0, 0, 1);
			AsteroidField.defaultPlayer.bind();
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
	
	public void kill(){
		dead = true;
		dx = 0;
		dy = 0;
		x = -10;
		y = -10;
		
		System.out.println("Agent " + id + " is dead.");
	}
	
	public void respawn(){
		dead = false;
		System.out.println("Agent " + id + " is alive.");
	}
}
