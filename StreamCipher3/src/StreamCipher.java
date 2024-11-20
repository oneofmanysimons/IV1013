import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.math.BigInteger;

public class StreamCipher {

    public static void main(String[] args) throws Exception {
        //handeling the input
        if (args.length != 6){
            throw new Exception("Format: --key <key> --in <infile> --out <outfile>");
        }
        //Global variables
        BigInteger key = null;
        String infile = "";
        String outfile = "";
        //Handeling input
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--key")){
                key = new BigInteger(args[i+1]);
            }
            if (args[i].equals("--in")){
                infile = args[i+1];
            }
            if (args[i].equals("--out")){
                outfile = args[i+1];
            }
        }
        //check for that an infile exist
        File inputFile = new File(infile);
        if (!inputFile.exists()){
            throw new Exception("There need to exist an input-file.");
        }
        //checking if outputFile exist
        File outFile = new File(outfile);
        if (!outFile.createNewFile()) {
            outFile.delete();
            outFile.createNewFile();
        }
        //Send message byte per byte
        FileInputStream inputStream = new FileInputStream(inputFile);
        FileOutputStream outputStream = new FileOutputStream(outFile);
        MyRandom random = new MyRandom(key);
        int part = 0;
        //handeling the message
        while ((part = inputStream.read()) != -1) {
            part = part ^ random.next(7);
            outputStream.write((byte)part);
        }
        inputStream.close();
        outputStream.close();
    }
}