import java.awt.Polygon;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;


public class Network extends Listener {

	String serverIP, username;
	int port;
	Client client;
	Agent[] agents = new Agent[200];
	AsteroidField game;
	
	public Network(AsteroidField game, String serverIP, int port) {
		this.game = game;
		this.serverIP = serverIP;
		this.port = port;
		client = new Client(16384, 12384);
		Kryo k = client.getKryo();
		k.register(int[].class);
		k.register(byte[].class);
		k.register(PacketJoin.class);
		k.register(PacketMakeAsteroid.class);
		k.register(PacketUpdateAsteroid.class);
		k.register(PacketDropPlayer.class);
		k.register(PacketUpdatePlayer.class);
		k.register(PacketPlayerState.class);
		k.register(PacketDrawText.class);
		k.register(PacketClearText.class);
		k.register(PacketUpdateText.class);
		client.addListener(this);
		client.start();
	}
	
	public void connect(String username){
		if(client == null) return;
		if(client.isConnected()) return;
		System.out.println("Connecting to " + serverIP + ":" + port + " as " + username);
		this.username = username;
		try {
			client.connect(5000, serverIP, port, port);
			
			PacketJoin packet = new PacketJoin();
			packet.username = username;
			if(AsteroidField.customAvatarFile.exists()){
				packet.hasAvatar = true;
				
				byte[] data = new byte[(int) AsteroidField.customAvatarFile.length()];
				FileInputStream fis = new FileInputStream(AsteroidField.customAvatarFile);
				fis.read(data);
				fis.close();
				
				packet.avatarData = data;
				
			}else{
				packet.hasAvatar = false;
			}
			client.sendTCP(packet);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void received(Connection c, Object o){
		if(o instanceof PacketJoin){
			PacketJoin packet = (PacketJoin) o;
			if(packet.id == -1){
				if(packet.accepted){
					System.out.println("Successfully joined the game.");
				}else{
					System.out.println("Server rejected our connection. Closing...");
					client.close();
					System.exit(0);
				}
				
			}else{
				agents[packet.id] = new Agent(packet.id, packet.username);
				agents[packet.id].avatarFile = null;
				if(packet.hasAvatar){
					agents[packet.id].avatarFile = "avatars/" + Program.md5(packet.avatarData) + ".png";
					try {
						FileOutputStream fos = new FileOutputStream(new File(agents[packet.id].avatarFile));
						fos.write(packet.avatarData);
						fos.flush();
						fos.close();
					} catch (IOException e) {
						e.printStackTrace();
						agents[packet.id].avatarFile = null;
					}
				}
				System.out.println("Added new agent with name " + packet.username + " (" + packet.id + ") with avatar: " + packet.hasAvatar);
				
			}
			
		}else if(o instanceof PacketMakeAsteroid){
			PacketMakeAsteroid packet = (PacketMakeAsteroid) o;
			Asteroid newAsteroid = new Asteroid(packet.id, packet.x, packet.y);
			newAsteroid.vx = packet.vx;
			newAsteroid.vy = packet.vy;
			newAsteroid.shape = new Polygon(packet.xpoints, packet.ypoints, packet.pointCount);
			game.asteroids.add(newAsteroid);
			System.out.println("Created asteroid with id " + packet.id + " at " + packet.x + " " + packet.y);
			
		}else if(o instanceof PacketUpdateAsteroid) {
			PacketUpdateAsteroid packet = (PacketUpdateAsteroid) o;
			Asteroid a = game.getAsteroidByID(packet.id);
			if(a == null) return; // Ghost packet/asteroid?
			
			a.x = packet.x;
			a.y = packet.y;
			a.vx = packet.dx;
			a.vy = packet.dy;
			
		}else if(o instanceof PacketDropPlayer){
			PacketDropPlayer packet = (PacketDropPlayer) o;
			agents[packet.id].removed = true;
			System.out.println("Dropped player " + packet.id);
			
		}else if(o instanceof PacketUpdatePlayer){
			PacketUpdatePlayer packet = (PacketUpdatePlayer) o;
			if(packet.id == -1){
				game.player.x = packet.x;
				game.player.y = packet.y;
				game.player.dx = packet.dx;
				game.player.dy = packet.dy;
				game.player.angle = packet.angle;
			}else{
				Agent agent = agents[packet.id];
				if(agent == null) return;
				agent.x = packet.x;
				agent.y = packet.y;
				agent.dx = packet.dx;
				agent.dy = packet.dy;
				agent.angle = packet.angle;
			}
			
		}else if(o instanceof PacketPlayerState){
			PacketPlayerState packet = (PacketPlayerState) o;
			if(packet.id == -1){ // update my life state
				if(packet.dead){
					game.createExplosion(game.player.x, game.player.y, game.player.dx, game.player.dy, 120);
					game.player.kill();
					game.respawnTimer = System.currentTimeMillis() + packet.respawnTime;
					game.respawnMaxTime = packet.respawnTime;
				}else{
					game.player.respawn();
				}
			}else{
				if(packet.dead){
					game.createExplosion(agents[packet.id].x, agents[packet.id].y, agents[packet.id].dx, agents[packet.id].dy, 120);
					agents[packet.id].kill();
				}else{
					agents[packet.id].respawn();
				}
			}
			
		}else if(o instanceof PacketDrawText){
			PacketDrawText packet = (PacketDrawText) o;
			if(AsteroidField.serverSideTexts.containsKey(packet.id)){
				ServerText text = AsteroidField.serverSideTexts.get(packet.id);
				text.setMessage(packet.text);
				text.setSize(packet.size);
				text.setX(packet.x);
				text.setY(packet.y);
			}else{
				ServerText text = new ServerText(packet.id, packet.text, packet.x, packet.y, packet.size, PacketDrawText.colors.get(packet.color));
				AsteroidField.serverSideTexts.put(packet.id, text);
			}
			
		}else if(o instanceof PacketUpdateText){
			PacketUpdateText packet = (PacketUpdateText) o;
			if(AsteroidField.serverSideTexts.containsKey(packet.id)){
				AsteroidField.serverSideTexts.get(packet.id).setMessage(packet.text);
			}
			
		}else if(o instanceof PacketClearText){
			PacketClearText packet = (PacketClearText) o;
			if(AsteroidField.serverSideTexts.containsKey(packet.id)){
				AsteroidField.serverSideTexts.get(packet.id).setDeleted(true);
			}
			
		}
	}
	
	public void disconnected(Connection c){
		System.out.println("disconnected");
	}

}
