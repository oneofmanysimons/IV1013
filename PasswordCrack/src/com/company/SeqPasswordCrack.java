package com.company;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.Scanner;

public class SeqPasswordCrack {
    public static LinkedList<String> store(File password) throws FileNotFoundException {
        Scanner scan = new Scanner(password);
        LinkedList<String> passwords = new LinkedList<>();
        int count = 0;
        while (scan.hasNextLine()){
            String line = scan.nextLine();
            String[] allOfIt = line.split(":");
            passwords.add(allOfIt[1]);
        }
        scan.close();
        return passwords;
    }
    public static String[] saltAndPass(String pass){
        StringBuilder sb = new StringBuilder(pass);
        String[] saltAndPass = new String[2];
        saltAndPass[0] = pass.substring(0,2);
        saltAndPass[1] = sb.substring(2,pass.length());
        return saltAndPass;
    }
    public static String salt(String pass){
        String salty = pass.substring(0,2);
        return salty;
    }
    public static LinkedList<String> decrementList(LinkedList<String> passwords, String password){
        LinkedList<String> newList = new LinkedList<>(passwords);;
        if (newList.remove(password)){
            return newList;
        }
        return newList;
    }
    public static String[] mangle(String line) {
        String[] mangles = new String[12];
        String[] array = new String[12-3];
        int count = 0;
        mangles[count++] = line.toUpperCase();
        mangles[count++] = line.toLowerCase();
        mangles[count++] = line.substring(0, line.length() - 1);
        mangles[count++] = line.substring(1);
        mangles[count++] = new StringBuilder(line).reverse().toString();
        if (!(line.length() > 8)) {
            mangles[count++] = line + line;
            mangles[count++] = new StringBuilder(line).reverse().toString() + line;
            mangles[count++] = line + new StringBuilder(line).reverse().toString();
        }else {
            for (int i = 0; i < array.length; i++) {
                if (mangles[i] != null) {
                    array[i] = mangles[i];
                }
            }
            mangles = array;
        }
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
    public static void hashing(File dic, File pass) throws FileNotFoundException {
        Scanner scan = new Scanner(dic);
        LinkedList<String> passwords = store(pass);
        // Collect passwords to remove
        while (scan.hasNextLine()) {
            String line = scan.nextLine();
            for (String password : passwords) {
                String mightBe = jcrypt.crypt(salt(password), line);
                if (mightBe.equals(password)) {
                    System.out.println("we found it : " + salt(password) + " " + line);
                    passwords = decrementList(passwords,password);
                    break;
                }
            }
        }
        scan.close();
        System.out.println("passwords craked : " + (20 - passwords.size()));
        hashingMangle(dic,passwords);
    }
    public static void hashingMangle(File dic, LinkedList<String> pass) throws FileNotFoundException {
        Scanner scan = new Scanner(dic);
        LinkedList<String> passwords = pass;
        // Collect passwords to remove
        while (scan.hasNextLine()) {
            String line = scan.nextLine();
            String[] mangles = mangle(line);
            for (String mangle : mangles) {
                for (String password : passwords) {
                    String mightBe = jcrypt.crypt(salt(password), mangle);
                    if (mightBe.equals(password)) {
                        System.out.println("we found it : " + salt(password) + " " + line);
                        passwords = decrementList(passwords, password);
                        break;
                    }
                }
            }
        }
        scan.close();
        System.out.println("passwords craked : " + (20 - passwords.size()));
        hashingMangleMangle(dic, passwords);
    }
    public static void hashingMangleMangle(File dic, LinkedList<String> pass) throws FileNotFoundException {
        Scanner scan = new Scanner(dic);
        LinkedList<String> passwords = pass;
        // Collect passwords to remove
        while (scan.hasNextLine()) {
            String line = scan.nextLine();
            String[] mangles = mangle(line);
            for (String mangle : mangles) {
                String[] doubleMangles = mangle(mangle);
                for (String doubleMangle : doubleMangles) {
                    for (String password : passwords) {
                        String mightBe = jcrypt.crypt(salt(password), doubleMangle);
                        if (mightBe.equals(password)) {
                            System.out.println("we found it : " + salt(password) + " " + line);
                            passwords = decrementList(passwords, password);
                            break;
                        }
                    }
                }
            }
        }
        scan.close();
        System.out.println("passwords craked : " + (20 - passwords.size()));
    }
    public static void main(String[] args) throws FileNotFoundException {
        System.out.println("Start breaking");
        File dic = new File("C:\\Users\\simon\\Documents\\KTHUppgifter\\IV1013\\" +
                "PasswordCrack\\src\\com\\company\\dict.txt");
        File pass = new File("C:\\Users\\simon\\Documents\\KTHUppgifter\\IV1013\\" +
                "PasswordCrack\\src\\com\\company\\passwd2.txt");
        double t0 = System.nanoTime();
        hashing(dic,pass);
        double t1 = System.nanoTime();
        System.out.println("it takes " + Math.round((t1-t0)/(Math.pow(10,9)*60)) + " mins");
    }
}
