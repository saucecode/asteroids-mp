import java.awt.Polygon;


public class Asteroid {

	Polygon shape;
	int id;
	float x, y, vx, vy;
	
	public Asteroid(int id, float x, float y){
		shape = new Polygon();
		this.id = id;
	}
}
