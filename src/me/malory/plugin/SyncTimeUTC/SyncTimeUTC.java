package me.malory.plugin.SyncTimeUTC;


import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.logging.Logger;

import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.NtpV3Packet;
import org.apache.commons.net.ntp.TimeInfo;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class SyncTimeUTC extends JavaPlugin {
    
    Logger logger;
	public static long offset = 0;
	public static long utcMillis = 0;
    
    @Override
    public void onEnable() { 
    	logger = getLogger();
    	logger.info(String.format("Enabled"));
    }
    
    @Override
    public void onDisable() {
        logger.info(String.format("Disabled"));
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender.hasPermission("synctimeutc.use")) {
            if (cmd.getName().equalsIgnoreCase("SyncTimeUTC") || (cmd.getName().equalsIgnoreCase("st"))) {
        		try {
        			NTPUDPClient client = new NTPUDPClient();
        			InetAddress address = InetAddress.getByName("time.nist.gov");
        			TimeInfo info = client.getTime(address);
        			NtpV3Packet packet = info.getMessage();
        			offset = packet.getTransmitTimeStamp().getTime() - System.currentTimeMillis();
        			utcMillis = packet.getTransmitTimeStamp().getTime();
	        		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
	        		SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd HH:mm:ss z");
	        		Date offsetDate = new Date(System.currentTimeMillis() + offset);
	        		String offsetString = dateFormat.format(offsetDate);
	                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7[&6SyncTimeUTC&7] &2Successful"));
	                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7[&6SyncTimeUTC&7] &bCurrent system time in millis is " + "&2" + System.currentTimeMillis()));            
	                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7[&6SyncTimeUTC&7] &bCurrent time from NIST is " + "&2" + utcMillis));
	                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7[&6SyncTimeUTC&7] &bFixed system time is " + "&2" + offsetString));    
	                return true;
				} catch (Exception e) {
					e.printStackTrace();
					return false;
				}
            }
        }
        if (cmd.getName().equalsIgnoreCase("UTC")) {
    		SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd HH:mm:ss z");
    		Date offsetDate = new Date(System.currentTimeMillis() + offset);
    		String offsetString = dateFormat.format(offsetDate);
        	sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7[&6SyncTimeUTC&7] &bCurrently it is " + "&2" + offsetString));
        	return true;
        }
    return false;
    }
}