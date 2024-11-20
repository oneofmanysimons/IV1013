

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

public class Hidenc {
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
    private boolean sizeStated = false;

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
                    if ((template.length()%Block_SIZE) != 0){
                        System.out.println(new Exception("template size must be " +
                                "divisable by " + Block_SIZE));
                    }
                    break;
                case "--size":
                    size = Integer.valueOf(argument[1]);
                    sizeStated = true;
                    if ((size%Block_SIZE) != 0){
                        System.out.println(new Exception("size must be divisable by " + Block_SIZE));
                    }
                    break;
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
                        System.out.println(new Exception("can not read from ctrFile"));
                    }
                    break;
                case "--input":
                    File inputFile = new File(argument[1]);
                    if (canReadFromFile(inputFile)){
                        input = readFromFile(inputFile);
                    }else {
                        System.out.println(new Exception("can not read from input"));
                    }
                    if (inputFile.length() % Block_SIZE != 0){
                        System.out.println(new Exception("inputFile wrong size, should be x%16 == 0"));
                    }
                    break;
                case "--output":
                    File outputFile = new File(argument[1]);
                    outputFile.createNewFile();
                    if (canWriteToFile(outputFile)){
                        output = outputFile;
                    }else {
                        System.out.println(new Exception("no such output file can be written to"));
                    }
                    break;
            }
        }
        if (sizeStated && templateTrue){
            System.out.println(new Exception("pick size of template"));
        }
        if (!(sizeStated || templateTrue)){
            System.out.println(new Exception("you must pick size or template"));
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
    private byte[] createContainer() throws InvalidAlgorithmParameterException, DigestException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, IOException {
        byte[] container = new byte[size];
        Random rand = new Random();
        if (templateTrue){
            container = Files.readAllBytes(template.toPath());
            return container;
        }
        for (int i = 0; i < container.length; i++) {
            container[i] = (byte) rand.nextInt(0,255);
        }
        init();
        container = cipher.doFinal(container);
        return container;
    }
    private byte[] createBlob(byte[] container) throws NoSuchAlgorithmException, InvalidAlgorithmParameterException, DigestException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        MessageDigest ms = MessageDigest.getInstance(HASH_ALGO);
        byte[] encKey = ms.digest(this.key); // H(k)
        byte[] data = this.input; // d
        byte[] hashData = ms.digest(data); //H(d)
        byte[] blob = new byte[encKey.length*2 + data.length + hashData.length];
        if ((blob.length + offset) > container.length){
            System.out.println(new Exception("the offset "+offset+" is to big " +
                    "for container "+container.length));
        }
        int idx = 0;
        for (int i = 0; i < encKey.length; i++) {
            blob[idx++] = encKey[i];
        }
        for (int i = 0; i < data.length; i++) {
            blob[idx++] = data[i];
        }
        for (int i = 0; i < encKey.length; i++) {
            blob[idx++] = encKey[i];
        }
        for (int i = 0; i < hashData.length; i++) {
            blob[idx++] = hashData[i];
        }
        //decrypt blob data and place in container at offset
        init();
        blob = cipher.doFinal(blob);
        idx = 0;
        for (int i = offset; i < blob.length + offset && idx < blob.length; i++) {
            container[i] = blob[idx++];
        }
        return container;
    }
    private void writeToFile(byte[] container) throws IOException {
        FileOutputStream write = new FileOutputStream(output,false);
        write.write(container);
        write.close();
    }
    public static void main(String[] args) throws Exception {
        Hidenc test = new Hidenc();
        test.verifyContentAndCollect(args);
        byte[] container = test.createContainer();
        container = test.createBlob(container);
        test.writeToFile(container);
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
}
