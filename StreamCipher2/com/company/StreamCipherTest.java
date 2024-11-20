package com.company;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import com.company.MyRandom;

public class StreamCipherTest {

    public static void main(String[] args) throws Exception {

        //handeling the input
        if (args.length != 6){
            throw new Exception("Format: --key <key> --in <infile> --out <outfile>");
        }
        //Global variables
        Long key = null;
        String infile = "";
        String outfile = "";

        //Handeling input
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--key")){
                key = Long.valueOf(args[i+1]);
            }
            if (args[i].equals("--in")){
                infile = args[i+1];
            }
            if (args[i].equals("--out")){
                outfile = args[i+1];
            }
        }

        Path input = Paths.get(infile);
        File inputFile = new File(String.valueOf(input));
        if (!inputFile.exists()){
            throw new Exception("There need to exist an input file.");
        }
        String message = Files.readString(input);

        //handeling the message
        char[] mesStream = new char[message.length()];
        MyRandom random = new MyRandom(key);

        for (int i = 0; i < message.length(); i++) {
            mesStream[i] = (char) (random.next(3) ^ message.charAt(i));
        }
        message = new String(mesStream);
        //output file part
        Path output = Paths.get(outfile);
        File file = new File(String.valueOf(output));
        FileOutputStream outputStream = new FileOutputStream(file);
        if (file.exists()){
            file.delete();
            file.createNewFile();
            //Files.writeString(output,message);
            for (int i = 0; i < mesStream.length; i++) {
                outputStream.write(mesStream[i]);
            }
        } else {
            file.createNewFile();
            //Files.writeString(output,message);
            for (int i = 0; i < mesStream.length; i++) {
                outputStream.write(mesStream[i]);
            }
        }
    }
}
