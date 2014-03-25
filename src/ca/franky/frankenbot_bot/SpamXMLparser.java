package ca.franky.frankenbot_bot;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.util.ArrayList;
import java.util.jar.Attributes;

/**
 * Created by vonn on 3/24/14.
 */
public class SpamXMLparser {
    public ArrayList<SpamXMLparser> spamConfigArrayList = new
            ArrayList<SpamXMLparser>();
    public String victimAddress;
    public String victimFirstName;
    public String victimLastName;
    public String emailFromAddress;
    public String emailSubject;
    public String emailBody;


    public SpamXMLparser() {

    }

    public SpamXMLparser (String myVictimAddress, String myVictimFirstName,
                          String myVictimLastName, String myEmailSubject,
                          String myEmailFromAddress, String myEmailBody) {

        spamConfigArrayList = null;
        victimAddress = myVictimAddress;
        victimFirstName = myVictimFirstName;
        victimLastName = myVictimLastName;
        emailSubject = myEmailSubject;
        emailFromAddress = myEmailFromAddress;
        emailBody = myEmailBody;
    }

    public ArrayList<SpamXMLparser> returnSpamConfigs(String myFile) {

        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();

            DefaultHandler handler = new DefaultHandler() {

                boolean bvictimdetails = false;
                @SuppressWarnings("unused")
                boolean bemaildetails = false;
                boolean baddr = false;
                boolean bfname = false;
                boolean blname = false;
                boolean bsubject = false;
                boolean bfromaddr = false;
                boolean bbody = false;

                public void startElement (String uri, String localName,
                                         String qName,
                                         Attributes attributes) throws
                        SAXException {

                    if (qName.equalsIgnoreCase("victimDetails")) {
                        bvictimdetails = false;
                    }
                    if (qName.equalsIgnoreCase("emailDetails")) {
                        bemaildetails = false;
                    }
                    if (qName.equalsIgnoreCase("address")) {
                        baddr = true;
                    }
                    if (qName.equalsIgnoreCase("firstName")) {
                        blname = true;
                    }
                    if (qName.equalsIgnoreCase("subject")) {
                        bsubject = true;
                    }
                    if (qName.equalsIgnoreCase("fromAddress")) {
                        bfromaddr = true;
                    }
                    if (qName.equalsIgnoreCase("body")) {
                        bbody = true;
                    }
                }

                public void endElement (String uri, String localName,
                                        String qName) throws SAXException {

                    if (qName.equalsIgnoreCase("victimDetails")) {
                        bvictimdetails = true;
                    }
                    if (qName.equalsIgnoreCase("emailDetails")) {
                        bemaildetails = true;
                    }
                }

                public void characters (char ch[], int start,
                                        int length) throws SAXException {

                    if (bvictimdetails) {
                        SpamXMLparser myCurrentVictim = new SpamXMLparser
                                (victimAddress, victimFirstName,
                                        victimLastName, emailSubject,
                                        emailFromAddress, emailBody);
                        if (myCurrentVictim != null) {
                            spamConfigArrayList.add(myCurrentVictim);
                        }

                        bvictimdetails = false;
                    }
                    if (baddr) {
                        victimAddress = new String(ch, start, length);
                        baddr = false;
                    }
                    if (bfname) {
                        victimFirstName = new String(ch, start, length);
                        bfname = false;
                    }
                    if (blname) {
                        victimLastName = new String(ch, start, length);
                        blname = false;
                    }
                    if (bsubject) {
                        emailSubject = new String(ch, start, length);
                        bsubject = false;
                    }
                    if (bfromaddr) {
                        emailFromAddress = new String(ch, start, length);
                        bfromaddr = false;
                    }
                    if (bbody) {
                        emailBody = new String(ch, start, length);
                        bbody = false;
                    }
                }
            };

            saxParser.parse(myFile, handler);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return spamConfigArrayList;
    }
}
