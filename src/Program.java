import javax.swing.JOptionPane;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;

public class Program {

	public static void main(String[] args) throws LWJGLException{
		if(args.length != 1) System.out.println("java -jar asteroids_mp.jar [server ip]");
		System.setProperty("org.lwjgl.librarypath", System.getProperty("user.dir") + "/natives");
		AsteroidField game = new AsteroidField();
		
		String serverIP = "localhost";
		//String serverIP = JOptionPane.showInputDialog("Enter server IP:");
		//String serverIP = args[0];
		int port = 25565;
		String username = "Player-" + (int) Math.floor(Math.random()*50);
		
		Network network = new Network(game, serverIP, port);
		game.network = network;
		network.connect(username);
		
		while(!network.client.isConnected()){
			try{
				Thread.sleep(1000);
			}catch(Exception e){
				continue;
			}
		}
		
		Display.setDisplayMode(new DisplayMode(720,720));
		Display.create();
		
		initGL2();
		game.init();
		while(!Display.isCloseRequested()){
			game.update();
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
			GL11.glLoadIdentity();
			game.render();
			Display.update();
			Display.sync(60);
		}
		
		Display.destroy();
		System.exit(0);
	}

	private static void initGL2() {
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GL11.glOrtho(0, Display.getWidth(), Display.getHeight(), 0, -1, 1);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
	}
}
