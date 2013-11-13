/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tadamski.glassfish.mongo.realm;

import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 *
 * @author tmszdmsk
 */
public class PasswordHasher {

    public static String hash(char[] password, String alghorithm) throws NoSuchAlgorithmException {
        final MessageDigest digester = MessageDigest.getInstance(alghorithm);
        byte[] passwordAsBytesArray = Charset.forName("UTF-8").encode(CharBuffer.wrap(password)).array();
        final byte[] digest = digester.digest(passwordAsBytesArray);
        String digestHex = digestToHex(digest);
        return digestHex;
    }

    private static String digestToHex(byte[] digest) {
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            String hex = Integer.toHexString(0xFF & b);
            if (hex.length() == 1) {
                // could use a for loop, but we're only dealing with a single byte
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }
}
