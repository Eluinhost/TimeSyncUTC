package me.malory.plugin.SyncTimeUTC;

import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;

import java.net.InetAddress;
import java.util.concurrent.Callable;

public class TimeSynchronizer implements Callable<TimeInfo> {

    @Override
    public TimeInfo call() throws Exception {
        NTPUDPClient client = new NTPUDPClient();
        InetAddress address = InetAddress.getByName("time.nist.gov");
        TimeInfo info = client.getTime(address);
        info.computeDetails();

        return info;
    }
}
