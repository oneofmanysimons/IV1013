

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.util.Scanner;

public class helpHiddec {
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
