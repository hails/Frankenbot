package ca.franky.frankenbot_bot;

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
        }
    }
}
