package net.saucecode.asteroidsmp;
import java.awt.Font;
import java.awt.FontFormatException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.lwjgl.opengl.GL11;

import tools.FontTT;
import tools.Texture;
import tools.TextureLoader;


public class AsteroidField {

	public static final File fontFile = new File("res/kenvector_future.ttf");
	public static final File customAvatarFile = new File("avatars/custom.png");
	public static final File defaultAvatarFile = new File("avatars/spaceship.png");
	
	List<Asteroid> asteroids = new ArrayList<Asteroid>();
	List<Projectile> projectiles = new ArrayList<Projectile>();
	Player player = new Player(this, 720/2, 720/2);
	Network network;
	Random random = new Random();
	long respawnTimer = 0, respawnMaxTime = 0;
	static TextureLoader loader = new TextureLoader();
	static Texture defaultPlayer;
	static FontTT font;
	static Map<Short, ServerText> serverSideTexts = new HashMap<Short, ServerText>();
	
	public void init() {
		try {
			defaultPlayer = loader.getTexture(defaultAvatarFile.getAbsolutePath(), false);
			if(customAvatarFile.exists()){
				player.texture = loader.getTexture(customAvatarFile.getAbsolutePath(), false);
				player.hasCustomTexture = true;
			}else{
				player.texture = defaultPlayer;
			}
			
			font = new FontTT(Font.createFont(Font.TRUETYPE_FONT, fontFile), 16, 0);
			
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
			a.render();
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
		
		// Server side texts
		Iterator<Entry<Short, ServerText>> entryIterator = serverSideTexts.entrySet().iterator();
		for(int i=0; i<serverSideTexts.size(); i++){
			if(!entryIterator.hasNext()) continue;
			Entry<Short, ServerText> entry = entryIterator.next();
			if(entry.getValue().isDeleted()){
				serverSideTexts.remove(entry.getKey());
			}
		}
		for(Short key : serverSideTexts.keySet()){
			if(!serverSideTexts.containsKey(key)) continue;
			ServerText text = serverSideTexts.get(key);
			font.drawText(text.getMessage(), text.getSize(), text.getX(), text.getY(), 0, text.getColor(), 0, 180, 180, false);
		}
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
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
