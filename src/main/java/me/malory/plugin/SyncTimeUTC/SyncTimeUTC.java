package me.malory.plugin.SyncTimeUTC;


import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class SyncTimeUTC extends JavaPlugin {

    public static final String PREFIX = ChatColor.GRAY + "[" + ChatColor.GOLD + "SyncTimeUTC" + ChatColor.GRAY + "] " + ChatColor.DARK_GREEN;
    public static final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd HH:mm:ss z");

    public TimeZone defaultTimezone = null;
    public long offset = 0;

    public void onEnable()
    {
        FileConfiguration configuration = getConfig();
        configuration.options().copyDefaults(true);
        saveConfig();

        defaultTimezone = TimeZone.getTimeZone(configuration.getString("default timezone"));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
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

                    Date offsetDate = new Date(info.getReturnTime() + offset);
                    dateFormat.setTimeZone(defaultTimezone);
                    String defaultTimeZone = dateFormat.format(offsetDate);
                    dateFormat.setTimeZone(TimeZone.getDefault());
                    String localTimeZone = dateFormat.format(offsetDate);

                    sender.sendMessage(PREFIX + "Successful");
                    sender.sendMessage(PREFIX + ChatColor.AQUA + "Current system time in millis is " + ChatColor.DARK_GREEN + returnTime);
                    sender.sendMessage(PREFIX + ChatColor.AQUA + "Current time from NIST is " + ChatColor.DARK_GREEN + serverTime);
                    sender.sendMessage(PREFIX + ChatColor.AQUA + "Fixed system time (local) is " + ChatColor.DARK_GREEN + localTimeZone);
                    sender.sendMessage(PREFIX + ChatColor.AQUA + "Fixed system time is " + ChatColor.DARK_GREEN + defaultTimeZone);
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                    sender.sendMessage(PREFIX + ChatColor.RED + "Error fetching time data");
                    return true;
                }
            }
        }
        if (cmd.getName().equalsIgnoreCase("UTC")) {
            Date offsetDate = new Date(System.currentTimeMillis() + offset);
            String offsetString = dateFormat.format(offsetDate);
            sender.sendMessage(PREFIX + ChatColor.AQUA + "Currently it is " + ChatColor.DARK_GREEN + offsetString);
            return true;
        }
        return false;
    }
}