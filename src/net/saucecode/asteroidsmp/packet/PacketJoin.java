package net.saucecode.asteroidsmp.packet;

public class PacketJoin {

	public int id;
	public String username;
	public boolean accepted;
	public boolean hasAvatar = false;
	public byte[] avatarData;
}
