import java.awt.Font;
import java.awt.FontFormatException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.lwjgl.opengl.GL11;

import tools.FontTT;
import tools.Texture;
import tools.TextureLoader;


public class AsteroidField {

	List<Asteroid> asteroids = new ArrayList<Asteroid>();
	List<Projectile> projectiles = new ArrayList<Projectile>();
	Player player = new Player(this, 720/2, 720/2);
	Network network;
	Random random = new Random();
	long respawnTimer = 0, respawnMaxTime = 0;
	static TextureLoader loader = new TextureLoader();
	static Texture defaultPlayer;
	static FontTT font;
	
	public void init() {
		try {
			defaultPlayer = loader.getTexture("avatars/spaceship.png", false);
			if(new File("avatars/custom.png").exists()){
				player.texture = loader.getTexture("avatars/custom.png", false);
				player.hasCustomTexture = true;
			}else{
				player.texture = defaultPlayer;
			}
			
			font = new FontTT(Font.createFont(Font.TRUETYPE_FONT, new File("res/kenvector_future.ttf")), 16, 0);
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (FontFormatException e) {
			e.printStackTrace();
		}
	}
	
	public void update() {
		for(Asteroid a : asteroids){
			a.x += a.vx;
			a.y += a.vy;
			
			if(a.x > 720) a.vx = -a.vx;
			if(a.x < 0) a.vx = -a.vx;
			if(a.y > 720) a.vy = -a.vy;
			if(a.y < 0) a.vy = -a.vy;
		}
		
		player.update();
		for(Agent a : network.agents){
			if(a == null) continue;
			a.update();
		}
		
		for(int i=0; i<network.agents.length; i++){
			if(network.agents[i] == null) continue;
			if(network.agents[i].removed){
				network.agents[i] = null;
				continue;
			}
			
			Agent agent = network.agents[i];
			if(agent.dead) continue;
			if(agent.ddx != agent.dx || agent.ddy != agent.dy){
				// determine if accelerating forwards or backwards, produce projectiles in the appropriate direction
				double ax, ay; // acceleration, x/y
				ax = agent.ddx-agent.dx;
				ay = agent.ddy-agent.dy;
				//boolean forward = !(Math.abs(agent.ddx) - Math.abs(agent.dx) > 0 || Math.abs(agent.ddy) - Math.abs(agent.dy) > 0);
				boolean forward = Math.hypot(ax,ay) > 0.03f;
				createProjectiles(agent.x, agent.y, forward ? (short) (agent.angle + 180) : agent.angle, 2.5f, 2);
				agent.ddx = agent.dx;
				agent.ddy = agent.dy;
			}
		}
		
		for(int i=0; i<projectiles.size(); i++){
			if(projectiles.get(i) == null) continue;
			if(projectiles.get(i).destroyed){
				projectiles.remove(i);
				i--;
			}
		}
	}

	public void render() {
		GL11.glColor3f(1, 1, 1);
		for(Asteroid a : asteroids){
			GL11.glBegin(GL11.GL_LINES);
			for(int i=0; i<a.shape.npoints; i++){
				GL11.glVertex2f(a.x + a.shape.xpoints[i], a.y + a.shape.ypoints[i]);
				GL11.glVertex2f(a.x + a.shape.xpoints[(i+1)%a.shape.npoints], a.y + a.shape.ypoints[(i+1)%a.shape.npoints]);
			}
			GL11.glEnd();
		}
		
		player.render();
		for(Agent a : network.agents){
			if(a == null) continue;
			a.render();
		}
		GL11.glBegin(GL11.GL_POINTS);
		for(int i=0; i<projectiles.size(); i++){
			Projectile j = projectiles.get(i);
			if(j == null) continue;
			j.draw();
		}
		GL11.glEnd();
		
	}
	
	public Asteroid getAsteroidByID(int id) {
		for(Asteroid a : asteroids){
			if(a.id == id) return a;
		}
		return null;
	}

	public void createProjectiles(float x, float y, short angle, float speed, int count) {
		for(int i=0; i<count; i++){
			Projectile p = new Projectile(x, y, (short) (Math.round(Math.random() * 60) * 2));
			p.addMotionAngle((float) (speed - 0.5 + Math.random()), angle - 15 + (short) Math.round(30*Math.random()));
			projectiles.add(p);
		}
	}
	
	// Create an explosion at x,y, with [size] particles moving at speed vx,vy
	public void createExplosion(float x, float y, float vx, float vy, int size){
		for(int i=0; i<size; i++){
			Projectile p = new Projectile(x, y, (short) (Math.round(Math.random() * 60) * 2));
			double angle = 180 + calcDeclinationAngle(x, y, x+vx, y+vy) - 50 + random.nextInt(100);
			float motionVariance = 1.7f;
			//p.addMotion(vx-motionVariance + random.nextFloat()*motionVariance*2f, vy-motionVariance + random.nextFloat()*motionVariance*2f);
			p.addMotionAngle((float) Math.hypot(vx, vy)*.5f - motionVariance + random.nextFloat()*motionVariance*2, (float) angle);
			projectiles.add(p);
		}
		System.out.println("Created explosion");
	}
	
	public static double calcDeclinationAngle(float p1x, float p1y, float p2x, float p2y){
		double x = 360-(Math.toDegrees(Math.atan2(p1y-p2y, p1x-p2x)) + 270);
		while(x < 0) x+=360;
		while(x > 360) x-=360;
		return x;
	}
}
