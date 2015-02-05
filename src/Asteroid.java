import java.awt.Polygon;

import org.lwjgl.opengl.GL11;


public class Asteroid {

	Polygon shape;
	int id;
	float x, y, vx, vy;
	
	public Asteroid(int id, float x, float y){
		shape = new Polygon();
		this.id = id;
	}
	
	public void render(){
		GL11.glBegin(GL11.GL_LINES);
		for(int i=0; i<shape.npoints; i++){
			GL11.glVertex2f(x + shape.xpoints[i], y + shape.ypoints[i]);
			GL11.glVertex2f(x + shape.xpoints[(i+1)%shape.npoints], y + shape.ypoints[(i+1)%shape.npoints]);
		}
		GL11.glEnd();
	}
}
