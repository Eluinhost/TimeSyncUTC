package me.malory.plugin.SyncTimeUTC;


import com.comphenix.executors.BukkitExecutors;
import com.comphenix.executors.BukkitScheduledExecutorService;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import org.apache.commons.net.ntp.TimeInfo;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class SyncTimeUTC extends JavaPlugin
{

    private final BukkitScheduledExecutorService async = BukkitExecutors.newAsynchronous(this);

    public static final String PREFIX = ChatColor.GRAY + "[" + ChatColor.GOLD + "SyncTimeUTC" + ChatColor.GRAY + "] " + ChatColor.DARK_GREEN;
    public static final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd HH:mm:ss z");

    private TimeZone defaultTimezone = null;
    private long offset = 0;
    private String timeserverUrl = "";

    public void onEnable()
    {
        FileConfiguration configuration = getConfig();
        configuration.options().copyDefaults(true);
        saveConfig();

        defaultTimezone = TimeZone.getTimeZone(configuration.getString("default timezone"));
        timeserverUrl = configuration.getString("timeserver");
    }

    @Override
    public boolean onCommand(final CommandSender sender, Command cmd, String label, String[] args)
    {
        if(sender.hasPermission("synctimeutc.use")) {
            if(cmd.getName().equalsIgnoreCase("SyncTimeUTC")) {
                //create a future for fetching the TimeInfo
                ListenableFuture<TimeInfo> futureTimeInfo = async.submit(new TimeSynchronizer(timeserverUrl));

                //when the time is fetched trigger the callback on the main thread
                Futures.addCallback(futureTimeInfo, new FutureCallback<TimeInfo>()
                {
                    @Override
                    public void onSuccess(TimeInfo info)
                    {
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
                    }

                    @Override
                    public void onFailure(Throwable throwable)
                    {
                        throwable.printStackTrace();
                        sender.sendMessage(PREFIX + ChatColor.RED + "Error fetching time data");
                    }
                });

                return true;
            }
        }
        if(cmd.getName().equalsIgnoreCase("UTC")) {
            TimeZone chosenZone = defaultTimezone;
            if(args.length > 0) {
                //load the timezone from args[0], if invalid supplid it reverts to GMT
                chosenZone = TimeZone.getTimeZone(args[0]);
            }
            Date offsetDate = new Date(System.currentTimeMillis() + offset);
            dateFormat.setTimeZone(chosenZone);
            String offsetString = dateFormat.format(offsetDate);
            sender.sendMessage(PREFIX + ChatColor.AQUA + "Currently it is " + ChatColor.DARK_GREEN + offsetString);
            return true;
        }
        return false;
    }
}