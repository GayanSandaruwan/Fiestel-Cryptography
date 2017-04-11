/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fiestel.cipher;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileSystemView;

/**
 *
 * @author Gayan Sandaruwan
 */
public class FiestelCipher {
    
    public static Scanner scanner = new Scanner(System.in);

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        encrypt();
        decrypt();
       
    }
    
    public static boolean  encrypt(){
        
        System.out.println("Encryption Key , Key Should be 8 charactors only");
        String keyString = scanner.next();
        byte[] key = toASCII(keyString);

        JFileChooser jfc = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
        File file;
        int returnValue = jfc.showOpenDialog(null);

        if (returnValue == JFileChooser.APPROVE_OPTION) {
            file = jfc.getSelectedFile();
            System.out.println(file.getAbsolutePath());
        } else {
            file = null;
        }

        String text = readFile(file);
        String fileName = file.getName();
        byte[] left;
        byte[] right;
        String lastcchar ="";
        int length= text.length();
        
        if (length % 2 == 0) {
           left =   toASCII(text.substring(0,length/2));
           right =   toASCII(text.substring(length/2,length));

        } else {
           left =   toASCII(text.substring(0,length/2));
           right =   toASCII(text.substring(length/2,length-1));
           lastcchar    =   text.substring(length-1,length);
        }
            byte[] encrypt = nRoundEncrypt(left, right, key, 2);
            String encryptedText    =   ASCIItoString(encrypt);
            encryptedText+=lastcchar;
            
            writeToFile(fileName+"encrypted.txt", encryptedText);
            return true;
    }
    
    public static boolean decrypt(){
    
    System.out.println("Encryption Key , Key Should be 8 charactors only");
        String keyString = scanner.next();
        byte[] key = toASCII(keyString);

        JFileChooser jfc = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
        File file;
        int returnValue = jfc.showOpenDialog(null);

        if (returnValue == JFileChooser.APPROVE_OPTION) {
            file = jfc.getSelectedFile();
            System.out.println(file.getAbsolutePath());
        } else {
            file = null;
        }

        String text = readFile(file);
        String fileName = file.getName();
        byte[] left;
        byte[] right;
        String lastcchar ="";
        int length= text.length();
        
        if (length % 2 == 0) {
           left =   toASCII(text.substring(0,length/2));
           right =   toASCII(text.substring(length/2,length));

        } else {
           left =   toASCII(text.substring(0,length/2));
           right =   toASCII(text.substring(length/2,length-1));
           lastcchar    =   text.substring(length-1,length);
        }
            byte[] decrypted = nRoundDecrypt(right, left, key, 2);
            String decryptedText    =   ASCIItoString(decrypted);
            decryptedText+=lastcchar;
            
            writeToFile(fileName+"decrypted.txt", decryptedText);
            return true;
    }

    private static byte[] getXOR(byte[] left, byte[] right) {
        //System.out.println(left.length);
        //System.out.println(right.length);
        byte[] XOR = new byte[left.length];

        for (int i = 0; i < left.length; i++) {
            XOR[i] = (byte) (left[i] ^ right[i]);
        }
        return XOR;
    }
       private static byte[] getXORKey(byte[] left, byte[] right) {
        //System.out.println(left.length);
        //System.out.println(right.length);
        byte[] XOR = new byte[left.length];

        for (int i = 0; i < left.length; i++) {
            XOR[i] = (byte) (left[i] ^ right[i%8]);
        }
        return XOR;
    }

    private static byte[] feistelFunction(byte[] key, byte[] right) {

        byte[] feisted = getXORKey(right,key);
//        for (int i = 0; i < right.length; i++) {
//            feisted[i] = (byte) (right[i] ^ key[i]);
//        }
         //System.out.println(ASCIItoString(feisted)+"  feisted");
        return feisted;

    }

    private static byte[] oneRoundEncrypt(byte[] left, byte[] right, byte[] key) {

        byte[] newRight = getXOR(feistelFunction(key, right), left);
        //System.out.println(ASCIItoString(key)+"  one Round");
       // System.out.println(ASCIItoString(newRight));
        return newRight;
    }

    private static byte[] nRoundEncrypt(byte[] left, byte[] right, byte[] key, int n) {  // n is the number of feistel rounds

        byte[] leftNew = left;
        byte[] rightNew = right;

        for (int j = 0; j < n; j++) {
            byte[] temp = rightNew;
            rightNew = oneRoundEncrypt(leftNew, rightNew, getRoundKey(key, j));
            leftNew = temp;
            // System.out.println(ASCIItoString(temp)+" encrypted");

        }

        byte[] encrypted = new byte[left.length + right.length];
        for (int k = 0; k < leftNew.length; k++) {

            encrypted[k] = leftNew[k];
        }
        for (int l = 0; l < rightNew.length; l++) {
            encrypted[l + leftNew.length] = rightNew[l];
        }

        return encrypted;
    }

    private static byte[] nRoundDecrypt(byte[] left, byte[] right, byte[] key, int n) {  // n is the number of feistel rounds

        byte[] leftNew = left;
        byte[] rightNew = right;

        for (int j = 0; j < n; j++) {
            byte[] temp = rightNew;
            rightNew = oneRoundEncrypt(leftNew, rightNew, getRoundKeyReverse(key, j, n));
            leftNew = temp;
            // System.out.println(ASCIItoString(temp)+" encrypted");

        }

        byte[] encrypted = new byte[left.length + right.length];
        for (int k = 0; k < rightNew.length; k++) {

            encrypted[k] = rightNew[k];
        }
        for (int l = 0; l < leftNew.length; l++) {
            encrypted[l + rightNew.length] = leftNew[l];
        }

        return encrypted;
    }

    private static byte[] getRoundKey(byte[] key, int round) {

        byte[] newKey = new byte[key.length];

        for (int i = 0; i < key.length; i++) {
            newKey[i] = (byte) (key[i] << round);
            //newKey[i]   =   key[i]
            //  System.out.println(ASCIItoString(newKey)+"   Key n");
        }
                 for(byte x:newKey){
                System.out.print(x+"  ");
            }
             System.out.println("");
        return newKey;
    }

    private static byte[] getRoundKeyReverse(byte[] key, int round, int n) {

        byte[] newKey = new byte[key.length];

        for (int i = 0; i < key.length; i++) {
            newKey[i] = (byte) (key[i] << n - round-1);
            //newKey[i]   =   key[i]
            //  System.out.println(ASCIItoString(newKey)+"   Key n");
        }
         for(byte x:newKey){
                System.out.print(x+"  ");
            }
            System.out.println("");
        return newKey;
    }

    private static byte[] toASCII(String str) {

        byte[] ASCII;

        try {
            ASCII = str.getBytes("US-ASCII");
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(FiestelCipher.class.getName()).log(Level.SEVERE, null, ex);
            ASCII = new byte[8];
        }
        return ASCII;
    }

    private static String ASCIItoString(byte[] encrpted) {

        String str;
        try {
            str = new String(encrpted, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(FiestelCipher.class.getName()).log(Level.SEVERE, null, ex);
            str = new String();
        }
        return str;
    }

    private static String readFile(File file) {

        BufferedReader br = null;
        String str = "";

        try {
            FileReader fr = new FileReader(file);

            br = new BufferedReader(fr);

            String sCurrentLine;

            br = new BufferedReader(new FileReader(file));

            while ((sCurrentLine = br.readLine()) != null) {
                str += sCurrentLine;
            }

        } catch (IOException e) {

            e.printStackTrace();

        } finally {

            try {

                if (br != null) {
                    br.close();
                }
//
//                if (fr != null) {
//                    fr.close();
//                }

            } catch (IOException ex) {

                ex.printStackTrace();

            }

        }
        return str;
    }

    private static boolean writeToFile(String filename, String text) {
        try {
            PrintWriter writer = new PrintWriter(filename, "UTF-8");
            writer.println(text);
            writer.close();
        } catch (IOException e) {
            // do something
            System.out.println("Error  ");
        }

        return true;
    }

}
