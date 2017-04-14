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
//    public static void main(String[] args) {
//
////        FiestelCipher fc = new FiestelCipher();
////        fc.encrypt(10);     // specify the number of rounds to be used in side the brackets
////        fc.decrypt(10);
//
//    }

    public boolean encrypt(int rounds,String keyString,String text, String fileName ) {                                                  
        /*This function is used to connect internal private functions and provide interface to outside 
        World!, To protect data, obviously you have to.*/
//        System.out.println("Encryption Key , Key Should be 8 charactors only");
//        String keyString = scanner.next();
        byte[] key = toASCII(keyString);

//        JFileChooser jfc = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
//        File file;
//        int returnValue = jfc.showOpenDialog(null);
//
//        if (returnValue == JFileChooser.APPROVE_OPTION) {
//            file = jfc.getSelectedFile();
//            System.out.println(file.getAbsolutePath());
//        } else {
//            file = null;
//        }

//        String text = readFile(file);
//        String fileName = file.getName();
        byte[] left;
        byte[] right;
        String lastcchar = "";
        int length = text.length();

        if (length % 2 == 0) {
            left = toASCII(text.substring(0, length / 2));
            right = toASCII(text.substring(length / 2, length));

        } else {
            left = toASCII(text.substring(0, length / 2));
            right = toASCII(text.substring(length / 2, length - 1));
            lastcchar = text.substring(length - 1, length);
        }
        byte[] encrypt = nRoundEncrypt(left, right, key, rounds);
        String encryptedText = ASCIItoEncryptedString(encrypt);
        encryptedText += lastcchar;

        writeToFile(fileName + "encrypted.txt", encryptedText);
        System.out.println("Encrption Successfull");
        return true;
    }

    public boolean decrypt(int rounds, String keyString) {

//        System.out.println("Encryption Key , Key Should be 8 charactors only");
//        String keyString = scanner.next();
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
        String lastcchar = "";
        int length = text.length();

        if (length % 2 == 0) {
            left = fromEncryptedToASCII(text.substring(0, length / 2));
            right = fromEncryptedToASCII(text.substring(length / 2, length));

        } else {
            left = fromEncryptedToASCII(text.substring(0, length / 2));
            right = fromEncryptedToASCII(text.substring(length / 2, length - 1));
            lastcchar = text.substring(length - 1, length);
        }
        byte[] decrypted = nRoundDecrypt(right, left, key, rounds);
        String decryptedText = ASCIItoString(decrypted);
        decryptedText += lastcchar;

        writeToFile(fileName + "decrypted.txt", decryptedText);
        return true;
    }

    private byte[] getXOR(byte[] left, byte[] right) {
        //System.out.println(left.length);
        //System.out.println(right.length);
        byte[] XOR = new byte[left.length];

        for (int i = 0; i < left.length; i++) {
            XOR[i] = (byte) (left[i] ^ right[i]);
        }
        return XOR;
    }

    private byte[] getXORKey(byte[] left, byte[] right) {
        //System.out.println(left.length);
        //System.out.println(right.length);
        byte[] XOR = new byte[left.length];

        for (int i = 0; i < left.length; i++) {
            XOR[i] = (byte) (left[i] ^ right[i % 8]);
        }
        return XOR;
    }

    private byte[] feistelFunction(byte[] key, byte[] right) {

        byte[] feisted = getXORKey(right, key);
        //System.out.println(ASCIItoString(feisted)+"  feisted");
        return feisted;

    }

    private byte[] oneRoundEncrypt(byte[] left, byte[] right, byte[] key) {

        byte[] newRight = getXOR(feistelFunction(key, right), left);
        //System.out.println(ASCIItoString(key)+"  one Round");
        // System.out.println(ASCIItoString(newRight));
        return newRight;
    }

    private byte[] nRoundEncrypt(byte[] left, byte[] right, byte[] key, int n) {  // n is the number of feistel rounds

        byte[] leftNew = left;
        byte[] rightNew = right;

        for (int j = 0; j < n; j++) {
            byte[] temp = rightNew;
            rightNew = oneRoundEncrypt(leftNew, rightNew, getRoundKey(key, j));
            leftNew = temp;
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

    private byte[] nRoundDecrypt(byte[] left, byte[] right, byte[] key, int n) {  // n is the number of feistel rounds

        byte[] leftNew = left;
        byte[] rightNew = right;
        System.out.println(ASCIItoEncryptedString(leftNew) + " LeftNew");
        for (byte x : leftNew) {
            System.out.print(x + " ");
        }
        System.out.println("");
        for (int j = 0; j < n; j++) {
            byte[] temp = rightNew;
            rightNew = oneRoundEncrypt(leftNew, rightNew, getRoundKeyReverse(key, j, n));   //Deference between nRoundDecrypt & nRoundEncrypt
            leftNew = temp;                                                                 // Is that nRoundDecrypt uses getRoundKeyReverse
            System.out.println(ASCIItoEncryptedString(leftNew) + " LeftNew");
            for (byte x : leftNew) {
                System.out.print(x + " ");
            }
            System.out.print(ASCIItoEncryptedString(rightNew) + " RightNew");

            for (byte x : rightNew) {
                System.out.print(x + " ");
            }
            System.out.println("");
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

    private byte[] getRoundKey(byte[] key, int round) {

        byte[] newKey = new byte[key.length];

        for (int i = 0; i < key.length; i++) {
            newKey[i] = (byte) (key[i] << round);
            //newKey[i]   =   key[i]
            //  System.out.println(ASCIItoString(newKey)+"   Key n");
        }
//                 for(byte x:newKey){
//                System.out.print(x+"  ");
//            }
//             System.out.println("");
        return newKey;
    }

    private byte[] getRoundKeyReverse(byte[] key, int round, int n) {

        byte[] newKey = new byte[key.length];

        for (int i = 0; i < key.length; i++) {
            newKey[i] = (byte) (key[i] << n - round - 1);
            //newKey[i]   =   key[i]
            //  System.out.println(ASCIItoString(newKey)+"   Key n");
        }
//         for(byte x:newKey){
//                System.out.print(x+"  ");
//            }
//            System.out.println("");
        return newKey;
    }

    private byte[] fromEncryptedToASCII(String str) {
//        System.out.println(str);
        byte[] ASCII = new byte[str.length() / 8];

        for (int i = 0; i < str.length() / 8; i++) {
            try {
                String tempStr = str.substring(0 + 8 * i, 8 + 8 * i);
//                System.out.println(tempStr);
                String intStr = "";
                byte[] byteVal = new byte[4];
                for (int j = 0; j < 4; j++) {

                    byteVal[j] = (byte) Integer.parseInt(tempStr.substring(0 + 2 * j, 2 + 2 * j));
//                    System.out.print(byteVal[j]+ " bv ");
                }
                intStr = new String(byteVal, "US-ASCII");
//                System.out.println(intStr);
                ASCII[i] = (byte) Integer.parseInt(intStr);
//                System.out.print(ASCII[i] + "  bytes");
            } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(FiestelCipher.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return ASCII;
    }

    private byte[] toASCII(String str) {

        byte[] ASCII;
        try {
            ASCII = str.getBytes("US-ASCII");
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(FiestelCipher.class.getName()).log(Level.SEVERE, null, ex);
            ASCII = new byte[8];
        }

        return ASCII;
    }

    private String ASCIItoEncryptedString(byte[] encrypted) {

        String textToBePrinted = "";
        for (byte x : encrypted) {
            if (x <= 0) {
                try {
                    String tempByteStr = String.valueOf(x);
                    if (tempByteStr.length() == 3) {
                        tempByteStr = "-0" + tempByteStr.substring(1, 3);
                    } else if (tempByteStr.length() == 2) {
                        tempByteStr = "-00" + tempByteStr.substring(1, 2);
                    } else if (tempByteStr.length() == 1) {
                        tempByteStr = "-000" + tempByteStr.substring(1);
                    }
                    byte[] byteVal = tempByteStr.getBytes("US-ASCII");
//                    System.out.println(tempByteStr);
//                    System.out.println(byteVal[3]);
                    String finalStrBlock = String.valueOf(byteVal[0]) + String.valueOf(byteVal[1]) + String.valueOf(byteVal[2]) + String.valueOf(byteVal[3]);
                    textToBePrinted += finalStrBlock;
                } catch (UnsupportedEncodingException ex) {
                    Logger.getLogger(FiestelCipher.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                try {
                    String tempByteStr = String.valueOf(x);
                    if (tempByteStr.length() == 2) {
                        tempByteStr = "+0" + tempByteStr;
                    } else if(tempByteStr.length()==1){
                        tempByteStr = "+00" + tempByteStr;
                    }
                    else{
                        tempByteStr = "+" + tempByteStr;
                    }
                    System.out.print(tempByteStr);
                    byte[] byteVal = tempByteStr.getBytes("US-ASCII");
                    String  finalStrBlock    =   String.valueOf(byteVal[0])+String.valueOf(byteVal[1])+String.valueOf(byteVal[2])+String.valueOf(byteVal[3]);
                    textToBePrinted += finalStrBlock;
                    //System.out.print(tempByteStr + "   temp");
                    
                } catch (UnsupportedEncodingException ex) {
                    Logger.getLogger(FiestelCipher.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        }
//            System.out.println(str);
        return textToBePrinted;
    }

    private String ASCIItoString(byte[] encrypted) {
        String str = "";
        try {
            str = new String(encrypted, "US-ASCII");
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(FiestelCipher.class.getName()).log(Level.SEVERE, null, ex);
            str = new String();
        }
        return str;
    }

    private String readFile(File file) {

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

    private boolean writeToFile(String filename, String text) {
        try {
            PrintWriter writer = new PrintWriter(filename, "US-ASCII");
            writer.println(text);
            writer.close();
        } catch (IOException e) {
            // do something
            System.out.println("Error  ");
        }

        return true;
    }

}
