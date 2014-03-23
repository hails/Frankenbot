package ca.franky.frankenbot_bot;

import jpcap.JpcapCaptor;
import jpcap.NetworkInterface;
import jpcap.NetworkInterfaceAddress;
import org.xbill.DNS.*;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * A class that defines the network parameters of the host.
 * Actually, for the purpose of this pedagogical exercise,
 * we make some pretty significant assumptions:
 *
 * First we consider that the interface that is directly connected
 * to the default gateway is our 'main/primary' interface. Second,
 * we assume that there is only one.
 *
 * Of course, neither assumption is 100% accurate but it is a good
 * enough assumption in the context of our work. It allows us to
 * concentrate on a single NIC when we do things such as passively
 * lister to tcp connections and when we run a scan of our network.
 *
 * @author fbegin1
 */

public class HostNetParams {
    // Name of the main interface e.g. eth0, eth1
    String primaryInterfaceName;

    // IP of the main interface e.g. 192.168.1.100
    InetAddress primaryInterfaceIP;

    // Netmask of the main interface e.g. 255.255.255.0
    InetAddress primaryInterfaceSubnetMask;

    //Network address of the main interface e.g. 192.168.1.0
    InetAddress primaryInterfaceNetwork;

    // Host default gateway
    String defaultGateway;

    // The ID of the main interface e.g. 0, 1, etc
    int primaryInterfaceID;

    // The host's router (from netstat -rn)
    String hostRoutes;

    // The host's FQDN;
    String hostFQDN;

    // The host's domain name
    String hostDomainName;


    /**
     * Our main constructor
     */
    public HostNetParams () {

        /**
         * We rely on netstat -rn to get host routing information
         */
        hostRoutes = Tools.runCmd("netstat -rn");
        String[] multiLineRoutes = hostRoutes.split("\n");
        String defaultRoute = "";

        /**
         * The ine that start with 0.0.0.0 contains our default route and
         * primary interface (see comments at the top of this class for our
         * definition of 'primary')
         */
        for (int i = 0; i < multiLineRoutes.length; i++) {
            if (multiLineRoutes[i].substring(0, 7).equals("0.0.0.0")) {
                defaultRoute = multiLineRoutes[i]
            }
        }

        /**
         * The second element of the line that starts with 0.0.0.0 should
         * have the default gateway and the last element should have the name
         * of our main interface.
         * e.g. 0.0.0.0         192.168.254     0.0.0.0             UG      00
         *          0   eth1
         */
        if (defaultRoute.trim().length() > 0) {
            String defaultRouteElements[] = defaultRoute.split("\\s+");
            primaryInterfaceName = defaultRouteElements[defaultRouteElements
                    .length - 1];
            defaultGateway = defaultRouteElements[i];
        } else {
            primaryInterfaceName = "unknown";
            defaultGateway = "unknown";
        }

        /**
         * We use jpcap to get the interface names. We match the name to the
         * interface ID, with we will need when we sniff traffic
         */
        NetworkInterface[] devices = JpcapCaptor.getDeviceList();
        primaryInterfaceID = -1;

        for (int i = 0; i < devices.length; i++) {
            if (devices[i].name.contains((primaryInterfaceName))) {
                primaryInterfaceID = i;
            }
        }

        /**
         * We determine the IP address of the main interface and its subnet mask
         */
        int n = 0;
        for (NetworkInterfaceAddress a : devices[primaryInterfaceID]
                .addresses) {
            if (n == 0) {
                primaryInterfaceIP = a.address;
                primaryInterfaceSubnetMask = a.subnet;
                n++;
            }
        }

        /**
         * We get the network address for the main interface
         */
        try {
            primaryInterfaceNetwork = InetAddress.getByName(binaryStringToIP
                    (logicalAND(ipToBinary(primaryInterfaceIP.getAddress()),
                     ipToBinary(primaryInterfaceSubnetMask.getAddress()))));
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    /**
     * A method that sets DNS data related to this host, namely the host FQDN
     * and the name of the domain where it resides
     * @param myPublicIP
     */
    public void findDNSdata (InetAddress myPublicIP) {

        // Find the FQDN
        hostFQDN = myPublicIP.getCanonicalHostName();

        // Find the domain (assumption here is .com, .net, etc so we use the
        // last 2 parts of the FQDN)
        String[] nameParts = hostFQDN.split("\\.");
        hostDomainName = nameParts[nameParts.length-2] + "." +
                nameParts[nameParts.length-1];

    }

    /**
     * A method that attempts to find the SMTP server based on the domain where
     * the host resides
     * @param myDomain
     * @return
     */
    public String findSMTPserver (String myDomain) {
        String SMTPServer = null;
        int preference = 1000;

        Record[] records = null;
        try {
            records = new Lookup(myDomain, Type.MX).run();
        } catch (TextParseException e) {
            e.printStackTrace();
        }

        if (records != null) {
            for (int i = 0; i < records.length; i++) {
                MXRecord mx = (MXRecord) records[i];
                if (mx.getPriority() < preference) {
                    SMTPServer = mx.getTarget().toString();
                    preference = mx.getPriority();
                }
            }
        }

        return SMTPServer;
    }

    /**
     * A method that takes a string containing the binary representation of
     * an IP address and changes it to the usual decimal representation
     * @param myBinaryIP
     * @return
     */
    private String binaryStringToIP (String myBinaryIP) {
        String myResult = "";

        if (! (myBinaryIP.length() == 32)) {
            return "";
        } else {
            for (int i = 0; i < 4; i++) {
                String octet = myBinaryIP.substring(8*i, 8*i+8);
                int octetValue = Integer.parseInt(octet, 2);
                if (! (i == 0)) {
                    myResult += ".";
                }
                myResult += Integer.toString(octetValue);
            }

            return myResult;
        }
    }

    /**
     * Method that translates a byte array (representing a IP) into a binary
     * number (saved as a string)
     * @param ipInBytesArray
     * @return
     */
    private static String ipToBinary (byte[] ipInBytesArray) {
        String ipAddress = "";

        for (int i = 0; i < ipInBytesArray.length; i++) {
            StringBuilder binaryValue = new StringBuilder("00000000");
            for (int bit = 0; bit < 8; bit++) {
                if (((ipInBytesArray[i] >> bit) & 1) > 0) {
                    binaryValue.setCharAt(7 - bit, '1');
                }
            }
            ipAddress += binaryValue;
        }

        return ipAddress;
    }

    /**
     * Method that translates a subnet mask to its CIDR notation form
     * @param ipInBytesArray
     * @return
     */
    public static String toCIDR (InetAddress myNetmask) {
        String maskString = ipToBinary(myNetmask.getAddress());
        return "/" + maskString.replace("0", "").trim().length();
    }
}
