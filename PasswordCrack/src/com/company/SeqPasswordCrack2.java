package com.company;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Scanner;

public class SeqPasswordCrack2 {
    public LinkedList<String> list = new LinkedList<String>();
    public synchronized void decrementList(LinkedList<String> passwords, String password){
        if (passwords.remove(password)){
            this.list = passwords;
        }
    }
    public static String salt(String pass){
        String salty = pass.substring(0,2);
        return salty;
    }
    private static String[] mangle(String line) {
        String[] mangles = new String[12];
        int count = 0;
        mangles[count++] = line.toUpperCase();
        mangles[count++] = line.toLowerCase();
        mangles[count++] = line.substring(0, line.length() - 1);
        mangles[count++] = line.substring(1);
        mangles[count++] = new StringBuilder(line).reverse().toString();
        mangles[count++] = line + line;
        mangles[count++] = new StringBuilder(line).reverse().toString() + line;
        mangles[count++] = line + new StringBuilder(line).reverse().toString();
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
        return mangles;
    }
    public void store(File password) throws FileNotFoundException {
        Scanner scan = new Scanner(password);
        while (scan.hasNextLine()){
            String line = scan.nextLine();
            String[] allOfIt = line.split(":");
            this.list.add(allOfIt[1]);
        }
        scan.close();
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
            String[] mangles = mangle(line);
            for (String mangle : mangles) {
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
            String[] mangles = mangle(line);
            for (String mangle : mangles) {
                String[] doubleMangles = mangle(mangle);
                for (String doubleMangle : doubleMangles) {
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
    public static void main(String[] args) throws IOException {
        double t0 = System.nanoTime();
        File dic = new File("C:\\Users\\simon\\Documents\\KTHUppgifter\\IV1013\\" +
                "PasswordCrack\\src\\com\\company\\dict.txt");
        File pass = new File("C:\\Users\\simon\\Documents\\KTHUppgifter\\IV1013\\" +
                "PasswordCrack\\src\\com\\company\\passwd2.txt");
        SeqPasswordCrack2 test = new SeqPasswordCrack2();
        test.store(pass);
        test.hashing(dic);
        test.hashingMangle(dic);
        test.hashingMangleMangle(dic);
        double t1 = System.nanoTime();
        System.out.println("passwords craked : " + (20 - test.list.size()));
        System.out.println("it takes " + Math.round((t1-t0)/(Math.pow(10,9)*60)) + " mins");
    }
}
