

import java.io.File;
import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.Scanner;
import java.io.IOException;

public class PasswordCrack {
    public LinkedList<String> list = new LinkedList<String>();
    public LinkedList<String> listNames = new LinkedList<String>();
    public synchronized void decrementList(LinkedList<String> passwords, String password){
        if (passwords.remove(password)){
            this.list = passwords;
        }
    }
    public static String salt(String pass){
        String salty = pass.substring(0,2);
        return salty;
    }
    public static String[] mangle(String line) {
        String[] mangles = new String[12];
        int count = 0;
        if (line.length() > 0) {
            mangles[count++] = line.toUpperCase();
            mangles[count++] = line.toLowerCase();
        }
        if (line.length() > 1) {
            mangles[count++] = line.substring(0, line.length() - 1);
            mangles[count++] = line.substring(1);
            mangles[count++] = new StringBuilder(line).reverse().toString();
            mangles[count++] = line.substring(0, 1).toUpperCase() + line.substring(1);
            mangles[count++] = line.substring(0, 1).toLowerCase() + line.substring(1).toUpperCase();
            String toggled = "";
            for (int i = 0; i < line.length(); i++) {
                if (i % 2 == 0) {
                    toggled += line.substring(i, i + 1).toUpperCase();
                } else {
                    toggled += line.substring(i, i + 1);
                }
            }
            mangles[count++] = toggled;
            toggled = "";
            for (int i = 0; i < line.length(); i++) {
                if (i % 2 != 0) {
                    toggled += line.substring(i, i + 1).toUpperCase();
                } else {
                    toggled += line.substring(i, i + 1);
                }
            }
            mangles[count++] = toggled;
        }
        if (line.length() < 9 && line.length() > 1) {
            mangles[count++] = line + line;
            mangles[count++] = new StringBuilder(line).reverse().toString() + line;
            mangles[count++] = line + new StringBuilder(line).reverse().toString();
        }
        String[] array = new String[count];
        for (int i = 0; i < count; i++) {
            array[i] = mangles[i];
        }
        mangles = array;
        return mangles;
    }
    public void store(File password) throws FileNotFoundException {
        Scanner scan = new Scanner(password);
        while (scan.hasNextLine()){
            String line = scan.nextLine();
            String[] allOfIt = line.split(":");
            this.list.add(allOfIt[1]);
            this.listNames.addFirst(allOfIt[0]);
        }
        scan.close();
    }
    public void hashing() {
        for (String line : this.listNames) {
            for (String password : this.list) {
                String mightBe = jcrypt.crypt(salt(password), line);
                if (mightBe.equals(password)) {
                    System.out.println(line);
                    decrementList(this.list, password);
                    break;
                }
            }
        }
    }
    public void hashingMangle() {
        for (String line : this.listNames) {
            for (String mangle : mangle(line)) {
                for (String password : this.list) {
                    String mightBe = jcrypt.crypt(salt(password), mangle);
                    if (mightBe.equals(password)) {
                        System.out.println(mangle);
                        decrementList(this.list, password);
                        break;
                    }
                }
            }
        }
    }
    public void hashing(File dic) throws FileNotFoundException {
        Scanner scan = new Scanner(dic);
        // Collect passwords to remove
        while (scan.hasNextLine()) {
            String line = scan.nextLine();
            for (String password : this.list) {
                String mightBe = jcrypt.crypt(salt(password), line);
                if (mightBe.equals(password)) {
                    System.out.println(line);
                    decrementList(this.list,password);
                    break;
                }
            }
        }
        scan.close();
    }
    public void hashingMangle(File dic) throws FileNotFoundException {
        Scanner scan = new Scanner(dic);
        // Collect passwords to remove
        while (scan.hasNextLine()) {
            String line = scan.nextLine();
            for (String mangle : mangle(line)) {
                for (String password : this.list) {
                    String mightBe = jcrypt.crypt(salt(password), mangle);
                    if (mightBe.equals(password)) {
                        System.out.println(mangle);
                        decrementList(this.list, password);
                        break;
                    }
                }
            }
        }
        scan.close();
    }
    public void hashingMangleMangle(File dic) throws FileNotFoundException {
        Scanner scan = new Scanner(dic);
        // Collect passwords to remove
        while (scan.hasNextLine()) {
            String line = scan.nextLine();
            for (String mangle : mangle(line)) {
                for (String doubleMangle : mangle(mangle)) {
                    for (String password : this.list) {
                        String mightBe = jcrypt.crypt(salt(password), doubleMangle);
                        if (mightBe.equals(password)) {
                            System.out.println(doubleMangle);
                            decrementList(this.list, password);
                            break;
                        }
                    }
                }
            }
        }
        scan.close();
    }
    public void filesCheck(File dic, File pass) throws Exception{
        if (!dic.exists() && !pass.exists()){
            throw new Exception("The " +dic.getName()+ " file does not exist " +
                    "and The "+ pass.getName() +" file does not exist");
        }
        if (!dic.exists()){
            throw new Exception("The " + dic.getName() + " file does not exist");
        }
        if (!pass.exists()){
            throw new Exception("The "+ pass.getName() +" file does not exist");
        }
        if (!dic.canRead()){
            throw new Exception("The " + dic.getName() + " file can not be read from");
        }
        if (!pass.canRead()){
            throw new Exception("The "+ pass.getName() +" file can not be read from");
        }
        Scanner scanPass = new Scanner(pass);
        if (!scanPass.hasNextLine()){
            throw new Exception("The "+ pass.getName() +" file is empty");
        }
        String line = scanPass.nextLine();
        if (!line.contains(":")){
            throw new Exception("The "+ pass.getName() +" file does not contain :");
        }
        Scanner scanDic = new Scanner(pass);
        if (!scanDic.hasNextLine()){
            throw new Exception("The " + dic.getName() + " file is empty");
        }
    }
    public static void main(String[] args) throws Exception {
        //double t0 = System.nanoTime();
        if (args.length != 2){
            System.out.println("dictionary and password pls." + "\n" +
                    "like this : " + "PasswordCrack <dict.txt> <passwd2.txt>");
        }
        File dic = new File(args[0]);
        File pass = new File(args[1]);
        PasswordCrack test = new PasswordCrack();
        test.filesCheck(dic,pass);
        test.store(pass);
        test.hashing();
        test.hashingMangle();
        test.hashing(dic);
        test.hashingMangle(dic);
        test.hashingMangleMangle(dic);
        //double t1 = System.nanoTime();
        //System.out.println("it takes " + Math.round((t1-t0)/(Math.pow(10,9)*60)) + " mins");
    }
}
