package com.company;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.Scanner;

public class notes {
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
    public static String[] store(File password) throws FileNotFoundException {
        Scanner scan = new Scanner(password);
        String[] passwords = new String[20];
        int count = 0;
        while (scan.hasNextLine()){
            String line = scan.nextLine();
            String[] allOfIt = line.split(":");
            passwords[count++] = allOfIt[1];
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
    public static String[] decrementList(String[] passwords, String password){
        String[] newList = new String[passwords.length-1];
        int count = 0;
        for (int i = 0; i < newList.length; i++) {
            if (!passwords[i].equals(password)){
                newList[i] = passwords[i];
            }
            else {
                count = i;
                break;
            }
        }
        for (int i = count; i < newList.length; i++) {
            newList[i] = passwords[i+1];
        }
        return newList;
    }
    public static void hashing(File dic, String[] passwords) throws FileNotFoundException {
        Scanner scan = new Scanner(dic);
        String[] password = passwords;
        while (scan.hasNextLine()) {
            String line = scan.nextLine();
            for (int i = 0; i < password.length; i++) {
                String[] saltAndPassword = saltAndPass(password[i]);
                String mightBe = jcrypt.crypt(saltAndPassword[0], line);
                if (mightBe.equals(password[i])) {
                    System.out.println("we found it : " + line);
                    password = decrementList(password, mightBe);
                    System.out.println("pass list : " + password.length);
                    break;
                }
            }
        }
        scan.close();
        toMangle(dic,password);
    }
    public static void toMangle(File dic,String[] passwords) throws FileNotFoundException {
        System.out.println("toMangle start here");
        Scanner scan = new Scanner(dic);
        String[] password = passwords;
        boolean found = false;
        while (scan.hasNextLine()) {
            String line = scan.nextLine();
            String[] words = mangle(line);
            for (int j = 0; j < words.length; j++) {
                line = words[j];
                for (int i = 0; i < password.length; i++) {
                    String[] saltAndPassword = saltAndPass(password[i]);
                    String mightBe = jcrypt.crypt(saltAndPassword[0], line);
                    if (mightBe.equals(password[i])) {
                        System.out.println("we found it : " + line);
                        password = decrementList(password, mightBe);
                        System.out.println("pass list : " + password.length);
                        found = true;
                        break;
                    }
                }
                if (found){
                    found = false;
                    break;
                }
            }
        }
        scan.close();
        toToMangle(dic,password);
    }
    public static void toToMangle(File dic,String[] passwords) throws FileNotFoundException {
        System.out.println("toToMangle start here");
        Scanner scan = new Scanner(dic);
        String[] password = passwords;
        boolean found = false;
        String[] words;
        while (scan.hasNextLine()) {
            String line = scan.nextLine();
            words = mangle(line);
            for (int j = 0; j < words.length; j++) {
                line = words[j];
                String[] words2 = mangle(line);
                for (int g = 0; g < words2.length; g++) {
                    line = words2[g];
                    for (int i = 0; i < password.length; i++) {
                        String[] saltAndPassword = saltAndPass(password[i]);
                        String mightBe = jcrypt.crypt(saltAndPassword[0], line);
                        if (mightBe.equals(password[i])) {
                            System.out.println("we found it : " + line);
                            password = decrementList(password, mightBe);
                            System.out.println("pass list : " + password.length);
                            found = true;
                            break;
                        }
                    }
                    if (found) {
                        break;
                    }
                }
                if (found) {
                }
            }
            scan.close();
        }
    }
    public static void hashing(File dic, File pass) throws FileNotFoundException {
        /*
        Scanner scan = new Scanner(dic);
        LinkedList<String> passwords = store(pass);
        LinkedList<String> passwordsToRemove = new LinkedList<>();

        // Collect passwords to remove
        while (scan.hasNextLine()) {
            String line = scan.nextLine();
            for (String password : passwords) {
                String mightBe = jcrypt.crypt(salt(password), line);
                if (mightBe.equals(password)) {
                    System.out.println("we found it : " + salt(password) + " " + line);
                    //passwordsToRemove.add(jcrypt.crypt(salt(password), line));
                    passwords = decrementList(passwords,password);
                    break;
                }
            }
        }
        scan.close();

        // Remove collected passwords

        for (String passwordToRemove : passwordsToRemove) {
            passwords = decrementList(passwords, passwordToRemove);
        }
        System.out.println("pass list : " + passwords.size());*/
    }
}
