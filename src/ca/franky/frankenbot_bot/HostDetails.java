package ca.franky.frankenbot_bot;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * A class that hold details about this host, such as OS information,
 * host uptime, etc.
 * @author fbegin1
 */
public class HostDetails {

    // Operating system name
    String osName;
    // Operating system architecture
    String osArch;
    // Operating system version
    String osVersion;
    // Uptime of the host
    String hostUptime;
    // Name of the host
    String hostName;
    // IP address defined on interfaces of this host
    String hostIps;
    // SMTP server mode of operation. The values can be 'sef' if the host
    // can send email directly, 'isp' if the host needs to use its SMTP
    // server or 'filtered' if the host cannot send mail out
    String smtpMode;

    public HostDetails () {

        /**
         * Clear variables
         */
        osName = null;
        osArch = null;
        osVersion = null;
        hostUptime = null;
        hostName = null;
        hostIps = "";

        /**
         * Get details about host
         */
        osName = System.getProperty("os.name");
        osArch = System.getProperty("os.arch");
        osVersion = System.getProperty("os.version");
        if (osName.toUpperCase().equals("LINUX")) {
            hostUptime = Tools.runCmd("uptime");
        }
        InetAddress addrs[] = null;

        try {
            hostName = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        try {
            addrs = InetAddress.getAllByName(hostName);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        for (InetAddress addr: addrs) {
            if (! addr.isLoopbackAddress() && addr.isSiteLocalAddress()) {
                hostIps += addr.getHostAddress() + "|";
            }
        }
    }
}
