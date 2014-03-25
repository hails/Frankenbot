package ca.franky.frankenbot_bot;

import jpcap.JpcapCaptor;
import jpcap.NetworkInterface;
import jpcap.packet.Packet;
import jpcap.packet.TCPPacket;

import java.io.IOError;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * A class that allows the bot to listen promiscuously for TCP
 * connections to and from this host
 * @author fbegin1
 */
public class NICListener extends Thread {
    Set<String> connectionTriplets = new HashSet<String>();
    boolean pleaseWait = false;

    public void run () {
        while (true) {
            /*
             * Has a pause been requested?
             */
            synchronized (this) {
                while (pleaseWait) {
                    try {
                        wait();
                    } catch (Exception e){
                    }
                }
            }
            /*
             * If not pause, start snooping on the interface, keeping
             * a running tally of (srcIP, dstIP, dstPort) tcp triplets
             */
            try {
                NetworkInterface[] devices = JpcapCaptor.getDeviceList();
                JpcapCaptor captor;
                captor = JpcapCaptor.openDevice(devices[2], 65535, false, 20);
                while (true) {
                    Packet myCapturedPacket = captor.getPacket();
                    if (myCapturedPacket != null) {
                        final TCPPacket tcpPacket = (TCPPacket)
                                myCapturedPacket;
                        connectionTriplets.add("(" + tcpPacket.src_ip
                                .toString().replace("/", "") + "," +
                                "" + tcpPacket.dst_ip.toString().replace
                                ("/", "") + "," + tcpPacket.dst_port + ")");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
