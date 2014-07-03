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

public class SyncTimeUTC extends JavaPlugin {
    
    public static long offset = 0;

    public static final String PREFIX = ChatColor.GRAY + "[" + ChatColor.GOLD + "SyncTimeUTC" + ChatColor.GRAY + "] " + ChatColor.DARK_GREEN;
    public static final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd HH:mm:ss z");

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender.hasPermission("synctimeutc.use")) {
            if (cmd.getName().equalsIgnoreCase("SyncTimeUTC")) {
        		try {
        			NTPUDPClient client = new NTPUDPClient();
        			InetAddress address = InetAddress.getByName("time.nist.gov");
        			TimeInfo info = client.getTime(address);
                    info.computeDetails();

                    offset = info.getOffset();
                    long returnTime = info.getReturnTime();
                    long serverTime = info.getReturnTime() + offset;

                    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
	        		Date offsetDate = new Date(info.getReturnTime() + offset);
	        		String offsetString = dateFormat.format(offsetDate);

	                sender.sendMessage(PREFIX + "Successful");
	                sender.sendMessage(PREFIX + ChatColor.AQUA + "Current system time in millis is " + ChatColor.DARK_GREEN + returnTime);
	                sender.sendMessage(PREFIX + ChatColor.AQUA + "Current time from NIST is " + ChatColor.DARK_GREEN + serverTime);
	                sender.sendMessage(PREFIX + ChatColor.AQUA + "Fixed system time is " + ChatColor.DARK_GREEN + offsetString);
	                return true;
				} catch (Exception e) {
					e.printStackTrace();
					return false;
				}
            }
        }
        if (cmd.getName().equalsIgnoreCase("UTC")) {
    		Date offsetDate = new Date(System.currentTimeMillis() + offset);
    		String offsetString = dateFormat.format(offsetDate);
        	sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7[&6SyncTimeUTC&7] &bCurrently it is " + "&2" + offsetString));
        	return true;
        }
    return false;
    }
}