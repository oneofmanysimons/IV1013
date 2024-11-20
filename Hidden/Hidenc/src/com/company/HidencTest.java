package com.company;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.file.Files;
import java.security.*;
import java.util.Random;
import java.util.Scanner;

public class HidencTest {
    private byte[] key;
    private byte[] data;
    private byte[] hPrime;
    private boolean isCTRmode = false;
    private byte[] input;
    private File output;
    private Cipher cipher;
    private byte[] ctr;
    private int offset = 0;
    private File template;
    private boolean templateTrue = false;
    private int size = 2048;

    private final int Block_SIZE = 16;
    private final String HASH_ALGO = "MD5";
    private final String CRYPT_ALGO = "AES";
    private final String AES_ECB = "AES/ECB/NoPadding";
    private final String AES_CTR = "AES/CTR/NoPadding";

    private void verifyContentAndCollect(String[] args) throws Exception{
        for (String arg : args){
            if (!arg.contains("=")){
                System.out.println(new Exception("There need to be = between all arguments" + "\n"));
                System.out.println("Like this for example : --key=abcd...");
            }
            String[] argument = arg.split("=");
            switch (argument[0]){
                case "--template":
                    template = new File(argument[1]);
                    if (canReadFromFile(template)){
                        templateTrue = true;
                    } else {
                        System.out.println(new Exception("Can not read from template file"));
                    }
                case "--size":
                    size = Integer.valueOf(argument[1]);
                case "--offset":
                    File offsetFile = new File(argument[1]);
                    if (canReadFromFile(offsetFile)){
                        Scanner scan = new Scanner(offsetFile);
                        offset = Integer.valueOf(scan.nextLine());
                    }else {
                        offset = Integer.valueOf(argument[1]);
                    }
                    break;
                case "--key":
                    File keyFile = new File(argument[1]);
                    if (canReadFromFile(keyFile)){
                        key = readFromKeyFile(keyFile);
                    }else {
                        key = stringToByte(argument[1]);
                    }
                    break;
                case "--ctr":
                    File ctrFile = new File(argument[1]);
                    if (canReadFromFile(ctrFile)) {
                        isCTRmode = true;
                        ctr = readFromKeyFile(new File(argument[1]));
                    } else {
                        ctr = stringToByte(argument[1]);
                    }
                    break;
                case "--input":
                    File inputFile = new File(argument[1]);
                    if (canReadFromFile(inputFile)){
                        input = readFromFile(inputFile);
                    } else {
                        System.out.println("Can not read from inputFile");
                    }
                    break;
                case "--output":
                    File outputFile = new File(argument[1]);
                    if (outputFile.createNewFile()){
                        System.out.println("created outputFile");
                    }
                    if (canWriteToFile(outputFile)){
                        output = outputFile;
                    }else {
                        System.out.println(new Exception("no such output file can be written to"));
                    }
                    break;
            }
        }
    }
    public void init() throws NoSuchPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, DigestException, InvalidAlgorithmParameterException {
        //get decrypted data
        SecretKeySpec secKey = new SecretKeySpec(key,CRYPT_ALGO);
        if (isCTRmode){
            this.cipher = Cipher.getInstance(AES_CTR);
            IvParameterSpec ivParamSpec = new IvParameterSpec(this.ctr); // init vector for AES in CTR-mode
            this.cipher.init(Cipher.ENCRYPT_MODE, secKey, ivParamSpec);
        } else {
            this.cipher = Cipher.getInstance(AES_ECB);
            this.cipher.init(Cipher.ENCRYPT_MODE, secKey);
        }
    }
    private byte[] createBlob() throws NoSuchAlgorithmException, InvalidAlgorithmParameterException, DigestException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, IOException {
        init();
        byte[] blob = null;
        MessageDigest ms = MessageDigest.getInstance(HASH_ALGO);
        byte[] encKey = ms.digest(this.key); // H(k)
        byte[] hashData = ms.digest(this.input); // H(d)
        byte[] data = cipher.doFinal(this.input); // data
        try {
            if (templateTrue) {
                int size = (int) template.length();
                blob = new byte[size];
                blob = Files.readAllBytes(template.toPath());
                int idx = offset;
                for (int i = 0; i < encKey.length; i++) {
                    blob[idx++] = encKey[i];
                }
                for (int i = 0; i < this.input.length; i++) {
                    blob[idx++] = data[i];
                }
                for (int i = 0; i < encKey.length; i++) {
                    blob[idx++] = encKey[i];
                }
                for (int i = 0; i < hashData.length; i++) {
                    blob[idx++] = hashData[i];
                }
            } else {
                blob = new byte[size];
                Random rand = new Random();
                for (int i = 0; i < size; i++) {
                    blob[i] = (byte) rand.nextInt(0, 255);
                }
                blob = cipher.doFinal(blob);
                int idx = offset;
                for (int i = 0; i < encKey.length; i++) {
                    blob[idx++] = encKey[i];
                }
                for (int i = 0; i < this.input.length; i++) {
                    blob[idx++] = data[i];
                }
                for (int i = 0; i < encKey.length; i++) {
                    blob[idx++] = encKey[i];
                }
                for (int i = 0; i < hashData.length; i++) {
                    blob[idx++] = hashData[i];
                }
            }
        } catch (ArrayIndexOutOfBoundsException exception){
            System.out.println(exception + " offset was to big");
        }
        return blob;
    }
    public static void main(String[] args) throws Exception {
        HidencTest test = new HidencTest();
        test.verifyContentAndCollect(args);
        byte[] blob = test.createBlob();
        test.writeToFile(blob);
    }
    private void writeToFile(byte[] blob) throws IOException {
        FileOutputStream write = new FileOutputStream(output);
        write.write(blob);
    }
    public static byte[] stringToByte(String argument){
        byte[] array = new byte[argument.length()/2];
        for (int i = 0; i < array.length; i++) {
            int pos = i * 2;
            Integer num = Integer.parseInt(argument.substring(pos,pos+2),16);
            array[i] = num.byteValue();
        }
        return array;
    }
    public static boolean canReadFromFile(File file){
        boolean statement = false;
        if (file.canRead()){
            statement = true;
        }
        return statement;
    }
    public static boolean canWriteToFile(File file){
        boolean statement = false;
        if (file.canWrite()){
            statement = true;
        }
        return statement;
    }
    public static byte[] readFromFile(File file) throws IOException {
        return Files.readAllBytes(file.toPath());
    }
    public static byte[] readFromKeyFile(File file) throws IOException {
        byte[] a = Files.readAllBytes(file.toPath());
        String key = new String(a);
        byte[] b = new byte[a.length/2];
        for (int i = 0; i < b.length; i++) {
            int pos = i * 2;
            Integer num = Integer.parseInt(key.substring(pos,pos+2),16);
            b[i] = num.byteValue();
        }
        return b;
    }
    public static boolean arrayEqual(byte[] a, byte[] b){
        if (a.length != b.length){
            return false;
        }
        for (int i = 0; i < a.length; i++) {
            if (Byte.compare(a[i], b[i]) != 0){
                return false;
            }
        }
        return true;
    }
}
