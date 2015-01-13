import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;


public class AsteroidField {

	List<Asteroid> asteroids = new ArrayList<Asteroid>();
	List<Projectile> projectiles = new ArrayList<Projectile>();
	Player player = new Player(this, 720/2, 720/2);
	Network network;
	long respawnTimer = 0, respawnMaxTime = 0;
	
	public void init() {
		
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
		for(Projectile j : projectiles){
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
}
