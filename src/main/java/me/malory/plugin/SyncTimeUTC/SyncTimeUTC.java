package me.malory.plugin.SyncTimeUTC;


import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.logging.Logger;

public class SyncTimeUTC extends JavaPlugin {
    
    Logger logger;
	public static long offset = 0;
    
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
                    info.computeDetails();

                    offset = info.getOffset();
                    long returnTime = info.getReturnTime();
                    long serverTime = info.getReturnTime() + offset;

                    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
	        		SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd HH:mm:ss z");

	        		Date offsetDate = new Date(info.getReturnTime() + offset);
	        		String offsetString = dateFormat.format(offsetDate);

	                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7[&6SyncTimeUTC&7] &2Successful"));
	                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7[&6SyncTimeUTC&7] &bCurrent system time in millis is " + "&2" + returnTime));
	                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7[&6SyncTimeUTC&7] &bCurrent time from NIST is " + "&2" + serverTime));
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