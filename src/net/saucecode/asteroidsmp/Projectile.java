package net.saucecode.asteroidsmp;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;


public class Projectile extends Vector2f {

	private static final long serialVersionUID = 7026180057933263945L;
	short lifetime;
	float dx=0, dy=0;
	boolean destroyed = false;
	
	public Projectile(float x, float y, short lifetime){
		super(x,y);
		this.lifetime = lifetime;
	}
	
	public void draw(){
		if(destroyed) return;
		GL11.glVertex2f(x, y);
		x += dx;
		y += dy;
		lifetime--;
		if(lifetime < 0) destroyed = true;
	}
	
	public void addMotion(float ddx, float ddy){
		this.dx += ddx;
		this.dy += ddy;
	}
	
	public void addMotionAngle(float velocity, float angle){
		this.dx += Math.sin(Math.toRadians(angle)) * velocity;
		this.dy += Math.cos(Math.toRadians(angle)) * velocity;
	}
}
