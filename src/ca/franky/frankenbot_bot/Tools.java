package ca.franky.frankenbot_bot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

/**
 * Inspired from http://alvinalexander.com/java/edu/pj/pj010016
 */
public class Tools {

    public static void sleep (int cycle, int randomness, boolean debug) {

        // We will need a random generator at some points in our code
        Random randomGenerator = new Random();

        try {
            Thread.currentThread();
            int randomInt = randomGenerator.nextInt(randomness);
            int sleepTime = cycle + randomInt;
            if (debug) System.out.println("I will now sleep for " +
                    sleepTime + " senconds.");
            Thread.sleep(sleepTime * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method that returns the md5 checksum of a string
     * @param msg the string you want to compute the checksum of
     * @return the checksum as a string
     */
    public static String computeMD5 (String msg) {
        byte[] bytesOfMessage = null;
        try {
            bytesOfMessage = msg.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        MessageDigest md = null;

        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        byte[] byteDigest = md.digest(bytesOfMessage);
        BigInteger bigInt = new BigInteger(1, byteDigest);
        String hashtext = bigInt.toString(16);
        while (hashtext.length() < 32) {
            hashtext = "0" + hashtext;
        }

        return hashtext;
    }

    /**
     * A method that runs a command on the command line of the host and
     * captures the result from the console
     * @param command
     * @return
     */
    public static String runCmd (String command) {
        String cmdOutput = "";
        String s = null;

        try {
            Process p = Runtime.getRuntime().exec(command);

            BufferedReader stdInput = new BufferedReader(new
                    InputStreamReader(p.getInputStream()));

            while ((s = stdInput.readLine()) != null) {
                cmdOutput += s + "\n";
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        return  cmdOutput;
    }
}
