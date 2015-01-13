import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;


public class Agent extends Vector2f {

	private static final long serialVersionUID = -2034873889589095071L;
	int id;
	String username;
	float dx = 0, dy = 0;
	float ddx = 0, ddy = 0; // change in change in velocity, to detect projectile production
	short angle = 0;
	boolean removed = false;
	boolean dead = false;
	
	public Agent(int id, String username){
		super(720/2,720/2);
		this.id = id;
		this.username = username;
	}
	
	public void update(){
		if(dead) return;
		x += dx;
		y += dy;
		
		if(x > 720) dx = -dx;
		if(x < 0) dx = -dx;
		if(y > 720) dy = -dy;
		if(y < 0) dy = -dy;
	}
	
	public void render() {
		if(dead) return;
		GL11.glColor3f(1, 1, 0);
		GL11.glBegin(GL11.GL_TRIANGLES);
		
		GL11.glVertex2d(x + Math.sin(Math.toRadians(angle))*8, y + Math.cos(Math.toRadians(angle))*8);
		GL11.glVertex2d(x + Math.sin(Math.toRadians(angle+140))*8, y + Math.cos(Math.toRadians(angle+140))*8);
		GL11.glVertex2d(x + Math.sin(Math.toRadians(angle+220))*8, y + Math.cos(Math.toRadians(angle+220))*8);
		
		GL11.glEnd();
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
