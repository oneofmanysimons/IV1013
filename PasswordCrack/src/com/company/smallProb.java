package com.company;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Locale;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class smallProb {
    smallProb(){
        super();
    }

    public static String[] store(File crack) throws FileNotFoundException {
        Scanner scan = new Scanner(crack);
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

    public static void mangle(String pass, String[] password){
        for (int i = 0; i < password.length; i++) {
            String[] saltAndPassword = saltAndPass(password[i]);
            String mightBe = jcrypt.crypt(saltAndPassword[0], pass);
            if (mightBe.equals(password[i])) {
                System.out.println("we found it : " + pass);
                password = decrementList(password, pass);
                System.out.println("pass list : " + password.length);
                break;
            }
        }
    }
    public static void toMangle(File dic, String[] passwords) throws FileNotFoundException {
        System.out.println("Here we start toMangle");
        Scanner scan = new Scanner(dic);
        String[] password = passwords;
        ExecutorService executorService = Executors.newFixedThreadPool(3);
        while (scan.hasNextLine()) {
            StringBuilder line = new StringBuilder(scan.nextLine());
            executorService.submit(() -> {
                try {
                    mangle(line.deleteCharAt(0).toString(),passwords);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            executorService.submit(() -> {
                try {
                    mangle(line.reverse().toString(),passwords);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            executorService.submit(() -> {
                try {
                    mangle(line.toString().toLowerCase(Locale.ROOT),passwords);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            executorService.submit(() -> {
                try {
                    mangle(line.toString().toUpperCase(Locale.ROOT),passwords);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            executorService.submit(() -> {
                try {
                    mangle(line.toString()+line.toString(),passwords);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
        executorService.shutdown();
        scan.close();
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

    public static void hashing2(File dic, String[] passwords) throws FileNotFoundException {
        Scanner scan = new Scanner(dic);
        String[] password = passwords;
        System.out.println("hello");
        while (scan.hasNextLine()){
            String line = scan.nextLine();
            for (int i = 0; i < passwords.length; i++) {
                String[] saltAndPassword = saltAndPass(password[i]);
                String mightBe = jcrypt.crypt(saltAndPassword[0], line);
                System.out.println(line + " " + saltAndPassword[0]);
                if (mightBe.equals(password[i])) {
                    System.out.println(line);
                    password = decrementList(password,line);
                    break;
                }
            }
        }
    }

    public static void breakPass(File crack, File dic) throws FileNotFoundException {
        Scanner crack1 = new Scanner(crack);
        String salt;
        String pass;
        String saltPass;
        while (crack1.hasNextLine()){
            String line = crack1.nextLine();
            String[] allOfIt = line.split(":");
            saltPass = allOfIt[1];
            salt = String.valueOf(saltPass.charAt(0)) + saltPass.charAt(1);
            char[] array = new char[saltPass.length()-2];
            for (int i = 0; i < saltPass.length()-2; i++) {
                array[i] = saltPass.charAt(i+2);
            }
            pass = new String(array);
            //System.out.println(salt+pass);
            hashing(dic, salt,pass);
        }
    }
    private static void hashing(File dic, String salt, String pass) throws FileNotFoundException {
        String[] array = {salt,pass};
        String password;
        String toCrack = salt+pass;
        Scanner scan = new Scanner(dic);
        while (scan.hasNextLine()){
            password = scan.nextLine();
            password = jcrypt.crypt(salt,password);
            int count = 0;
            if (password.equals(toCrack)){
                System.out.println(count);
                System.out.println(password);
                System.out.println(toCrack);
                System.out.println("found it");
                break;
            }
            count++;
        }
        //jcrypt.main(array);
    }
    public static void breaking(File dic, File pass) throws FileNotFoundException {
        String[] password = store(pass);
        Scanner scan = new Scanner(dic);
        while (scan.hasNextLine()){
            String line = scan.nextLine();
            for (int i = 0; i < password.length; i++) {
                String[] saltAndPassword = saltAndPass(password[i]);
                String mightBe = jcrypt.crypt(saltAndPassword[0], line);/*
                System.out.println(saltAndPassword[0]);
                System.out.println(saltAndPassword[1]);
                System.out.println(mightBe);*/
                if (mightBe.equals(password[i])) {
                    System.out.println("we found it " + line);
                    password = decrementList(password, line);
                    break;
                }
            }
        }
        scan.close();
    }
}
