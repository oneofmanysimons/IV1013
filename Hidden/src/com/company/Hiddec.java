

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.*;
import java.util.Arrays;

public class Hiddec {
    private byte[] key;
    private boolean isCTRmode = false;
    private byte[] input;
    private byte[] rest;
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
    public byte[] decrypt(byte[] block) throws InvalidAlgorithmParameterException, DigestException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        return cipher.doFinal(block);
    }
    public int findHk() throws NoSuchAlgorithmException, InvalidAlgorithmParameterException, DigestException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        Integer index = null;
        MessageDigest ms = MessageDigest.getInstance(HASH_ALGO);
        byte[] hashKey = ms.digest(key);
        byte[] check = new byte[Block_SIZE];
        init();
        for (int i = 0; i < input.length - Block_SIZE; i += Block_SIZE) {
            for (int j = 0; j < Block_SIZE; j++) {
                check[j] = input[j + i];
            }
            check = decrypt(check);
            if (arrayEqual(hashKey, check)) {
                index = i;
                return index;
            }
        }
        return index;
    }
    public int findNextHk(int startindex) throws NoSuchAlgorithmException, InvalidAlgorithmParameterException, DigestException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        MessageDigest ms = MessageDigest.getInstance(HASH_ALGO);
        byte[] hashKey = ms.digest(key);
        int index = 0;
        byte[] check = new byte[Block_SIZE];
        rest = new byte[input.length - startindex];
        int idx = 0;
        for (int i = startindex; i < input.length; i++) {
            rest[idx++] = input[i];
        }
        rest = decrypt(rest);
        for (int i = Block_SIZE; i < rest.length - Block_SIZE; i += Block_SIZE) {
            for (int j = 0; j < check.length; j++) {
                check[j] = rest[j + i];
            }
            if (arrayEqual(hashKey, check)) {
                index = i;
                break;
            }
        }
        return index;
    }
    public byte[] setData(int index2) throws NoSuchAlgorithmException, InvalidAlgorithmParameterException, DigestException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        byte[] data = new byte[index2 - Block_SIZE];
        int count = 0;
        for (int i = Block_SIZE; i < index2; i++) {
            data[count++] = rest[i];
        }
        return data;
    }
    public byte[] getnextdata(int index) throws InvalidAlgorithmParameterException, DigestException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        byte[] data = new byte[Block_SIZE];
        int count = 0;
        for (int i = index + Block_SIZE; i < index + Block_SIZE*2; i++) {
            data[count++] = rest[i];
        }
        return data;
    }
    public void compare(byte[] data, byte[] hPrime) throws NoSuchAlgorithmException, IOException {
        MessageDigest ms = MessageDigest.getInstance(HASH_ALGO);
        if (arrayEqual(ms.digest(data),hPrime)){
            FileOutputStream fw = new FileOutputStream(output);
            fw.write(data);
        }
    }
    public static void main(String[] args) throws Exception {
        Hiddec test = new Hiddec();
        test.verifyContentAndCollect(args);
        int index1 = test.findHk();
        int index2 = test.findNextHk(index1);
        byte[] data = test.setData(index2);
        byte[] next = test.getnextdata(index2);
        test.compare(data,next);
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
