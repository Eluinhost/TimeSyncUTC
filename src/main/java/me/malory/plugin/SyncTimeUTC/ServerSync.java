package me.malory.plugin.SyncTimeUTC;

import java.net.InetAddress;

import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.NtpV3Packet;
import org.apache.commons.net.ntp.TimeInfo;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class ServerSync extends BukkitRunnable {
	
	private final JavaPlugin plugin; 
	
	public ServerSync(JavaPlugin plugin) {
		this.plugin = plugin;
	}
	
	@Override
	public void run() {
		try {
			NTPUDPClient client = new NTPUDPClient();
			InetAddress address = InetAddress.getByName("time.nist.gov");
			TimeInfo info = client.getTime(address);
			NtpV3Packet packet = info.getMessage();
			SyncTimeUTC.offset = packet.getTransmitTimeStamp().getTime() - System.currentTimeMillis();
			SyncTimeUTC.utcMillis = packet.getTransmitTimeStamp().getTime();
			plugin.getLogger().log(null, "Syncing");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}