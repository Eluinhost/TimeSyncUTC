package me.malory.plugin.SyncTimeUTC;

import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;

import java.net.InetAddress;
import java.util.concurrent.Callable;

public class TimeSynchronizer implements Callable<TimeInfo>
{

    private final String timeServer;

    public TimeSynchronizer(String serverAddress)
    {
        timeServer = serverAddress;
    }

    @Override
    public TimeInfo call() throws Exception
    {
        NTPUDPClient client = new NTPUDPClient();
        InetAddress address = InetAddress.getByName(timeServer);
        TimeInfo info = client.getTime(address);
        info.computeDetails();

        return info;
    }
}
