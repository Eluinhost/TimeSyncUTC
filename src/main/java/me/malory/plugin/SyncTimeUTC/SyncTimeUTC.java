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

import java.lang.ref.WeakReference;
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

        syncOffset(getServer().getConsoleSender());
    }

    /**
     * Syncronizes the offset variable, does not update straight away as it requests from NTP
     * @param commandSender the sender to send a message to
     */
    private void syncOffset(CommandSender commandSender)
    {
        final WeakReference<CommandSender> senderReference = new WeakReference<CommandSender>(commandSender);

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

                CommandSender sender = senderReference.get();
                if(sender != null) {
                    sender.sendMessage(PREFIX + ChatColor.AQUA + "Current system time in millis is " + ChatColor.DARK_GREEN + returnTime);
                    sender.sendMessage(PREFIX + ChatColor.AQUA + "Current time from NIST is " + ChatColor.DARK_GREEN + serverTime);
                    sender.sendMessage(PREFIX + ChatColor.AQUA + "Fixed system time (local) is " + ChatColor.DARK_GREEN + localTimeZone);
                    sender.sendMessage(PREFIX + ChatColor.AQUA + "Fixed system time is " + ChatColor.DARK_GREEN + defaultTimeZone);
                }
            }

            @Override
            public void onFailure(Throwable throwable)
            {
                throwable.printStackTrace();

                CommandSender sender = senderReference.get();
                if(sender != null) {
                    sender.sendMessage(PREFIX + ChatColor.RED + "Error fetching time data");
                }
            }
        });
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {

        if(cmd.getName().equalsIgnoreCase("SyncTimeUTC")) {
            if(!sender.hasPermission("synctimeutc.sync")) {
                sender.sendMessage(ChatColor.RED + "You don't have permission to run this command");
                return true;
            }

            syncOffset(sender);

            //always return true as this will be reached before the callback
            return true;
        }

        if(cmd.getName().equalsIgnoreCase("UTC")) {
            if(!sender.hasPermission("synctimeutc.use")) {
                sender.sendMessage(ChatColor.RED + "You don't have permission to run this command");
                return true;
            }

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