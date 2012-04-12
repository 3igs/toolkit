package bigs.core.utils;

public class Network {
	
    public static String getHostName() {
        String hostName = "";
        try {
            hostName = java.net.InetAddress.getLocalHost().getHostName();
        } catch (java.net.UnknownHostException e) {
            try {
                hostName = java.net.InetAddress.getLocalHost().getAddress().toString();
            } catch (java.net.UnknownHostException ee) {
                hostName = "unknown-host";
            }
        }
        return hostName;
    }
}
