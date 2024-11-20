package com.company;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.security.*;
import java.util.Arrays;

public class HiddecTest {
    private byte[] key;
    private byte[] data;
    private byte[] hPrime;
    private boolean isCTRmode = false;
    private byte[] input;
    private File output;
    private Cipher cipher;
    private byte[] ctr;

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
    public void decrypt() throws InvalidAlgorithmParameterException, DigestException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        init();
        byte[] array = cipher.doFinal(input);
        input = array;
    }
    public byte[] decrypt(byte[] block) throws InvalidAlgorithmParameterException, DigestException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        init();
        return cipher.doFinal(block);
    }
    public void init() throws NoSuchPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, DigestException, InvalidAlgorithmParameterException {
        //get decrypted data
        SecretKeySpec secKey = new SecretKeySpec(key,CRYPT_ALGO);
        if (isCTRmode){
            this.cipher = Cipher.getInstance(AES_CTR);
            IvParameterSpec ivParamSpec = new IvParameterSpec(this.ctr); // init vector for AES in CTR-mode
            this.cipher.init(Cipher.DECRYPT_MODE, secKey, ivParamSpec);
        } else {
            this.cipher = Cipher.getInstance(AES_ECB);
            this.cipher.init(Cipher.DECRYPT_MODE, secKey);
        }
    }
    public int findHk() throws NoSuchAlgorithmException, InvalidAlgorithmParameterException, DigestException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        MessageDigest ms = MessageDigest.getInstance(HASH_ALGO);
        byte[] hashKey = ms.digest(key);
        Integer index = null;
        byte[] check = new byte[Block_SIZE];
        if (isCTRmode) {
            for (int i = 0; i < input.length - Block_SIZE; i += Block_SIZE) {
                for (int j = 0; j < check.length; j++) {
                    check[j] = input[j + i];
                    check = decrypt(check);
                    System.out.println(new String(check));
                }
                if (arrayEqual(hashKey, check)) {
                    index = i;
                    break;
                }
            }
        }else {
            decrypt();
            for (int i = 0; i < input.length - Block_SIZE; i += 1) {
                for (int j = 0; j < check.length; j++) {
                    check[j] = input[j + i];
                }
                if (arrayEqual(hashKey, check)) {
                    index = i;
                    break;
                }
            }
        }
        System.out.println();
        System.out.println(new String(key));
        System.out.println(new String(hashKey));
        return index;
    }
    public int findNextHk(int startindex) throws NoSuchAlgorithmException, InvalidAlgorithmParameterException, DigestException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        MessageDigest ms = MessageDigest.getInstance(HASH_ALGO);
        byte[] hashKey = ms.digest(key);
        int index = 0;
        byte[] check = new byte[Block_SIZE];
        if (isCTRmode) {
            for (int i = startindex; i < startindex + Block_SIZE; i++) {
                check[i] = input[i];
            }
            check = decrypt(check);
            for (int i = startindex + Block_SIZE; i < input.length - Block_SIZE; i+= Block_SIZE) {
                for (int j = 0; j < Block_SIZE; j++) {
                    check[j] = input[j+i];
                }
                check = decrypt(check);
                if (arrayEqual(hashKey, check)) {
                    index = i;
                    break;
                }
            }
        }else {
            for (int i = startindex+Block_SIZE; i < input.length - Block_SIZE; i += 1) {
                for (int j = 0; j < check.length; j++) {
                    check[j] = input[j + i];
                }
                if (arrayEqual(hashKey, check)) {
                    index = i;
                    break;
                }
            }
        }
        return index;
    }
    public void setData(int index1,int index2) throws NoSuchAlgorithmException, InvalidAlgorithmParameterException, DigestException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        byte[] dat = new byte[index2 - index1 - Block_SIZE];
        int count = 0;
        if (isCTRmode){
            for (int i = index1 + Block_SIZE; i < index2; i+=Block_SIZE) {
                byte[] send = new byte[Block_SIZE];
                for (int j = 0; j < Block_SIZE; j++) {
                    send[j] = input[count+j];
                    send = decrypt(send);
                }
                for (int j = 0; j < Block_SIZE; j++) {
                    dat[count++] = send[j];
                }
            }
        } else {
            for (int i = index1 + Block_SIZE; i < index2; i++) {
                dat[count++] = input[i];
            }
            data = dat;
        }
    }
    public void getnextdata(int index) throws InvalidAlgorithmParameterException, DigestException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        byte[] dat = new byte[Block_SIZE];
        int count = 0;
        if (isCTRmode){
            for (int i = index + Block_SIZE; i < index + Block_SIZE + Block_SIZE; i++) {
                dat[count++] = input[i];
            }
            //dat = decrypt(dat,(index+Block_SIZE)%Block_SIZE);
        }else {
            for (int i = index + Block_SIZE; i < index + Block_SIZE + Block_SIZE; i++) {
                dat[count++] = input[i];
            }
        }
        hPrime = dat;
    }
    public void compare() throws NoSuchAlgorithmException, IOException {
        MessageDigest ms = MessageDigest.getInstance(HASH_ALGO);
        if (arrayEqual(ms.digest(data),hPrime)){
            FileOutputStream fw = new FileOutputStream(output);
            fw.write(data);
            fw.write(hPrime);
        }
    }
    public static void main(String[] args) throws Exception {
        HiddecTest test = new HiddecTest();
        test.verifyContentAndCollect(args);
        int index1 = test.findHk();
        int index2 = test.findNextHk(index1);
        System.out.println(index1 + " " + index2);
        test.setData(index1,index2);
        test.getnextdata(index2);
        test.compare();
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
