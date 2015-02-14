import java.awt.Color;
import java.util.HashMap;
import java.util.Map;


public class PacketDrawText {

	public static Map<Byte, Color> colors = new HashMap<Byte, Color>();
	static {
		colors.put((byte) 0, Color.white);
		colors.put((byte) 1, Color.yellow);
		colors.put((byte) 2, Color.red);
		colors.put((byte) 3, new Color(0.3f, 0.3f, 1.0f));
	}
	
	short id;
	public String text = "undefined packet string";
	public float x, y, size;
	public byte color = 0;
}
