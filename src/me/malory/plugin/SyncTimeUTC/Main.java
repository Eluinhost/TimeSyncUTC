package me.malory.plugin.SyncTimeUTC;

import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.NtpV3Packet;
import org.apache.commons.net.ntp.TimeInfo;

public class Main {
	public static void main(String[] args) {
		long utcTimeMillis = 0;
		try {
			NTPUDPClient client = new NTPUDPClient();
			InetAddress address = InetAddress.getByName("time.nist.gov");
			TimeInfo info = client.getTime(address);
			NtpV3Packet packet = info.getMessage();
			utcTimeMillis = packet.getTransmitTimeStamp().getTime();
			} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error getting time.");
		}
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
		long offset = utcTimeMillis - System.currentTimeMillis();
		System.out.println(System.currentTimeMillis() + offset);
		Date utcDate = new Date(utcTimeMillis);
		SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd HH:mm:ss z");
		String utcString = dateFormat.format(utcDate);
		System.out.println(utcString);
	}
}