package com.company;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.Scanner;

public class Main {
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

    // Main Class
        public static void main(String[] args) throws FileNotFoundException {
            double t0 = System.nanoTime();
            File dic = new File("C:\\Users\\simon\\Documents\\KTHUppgifter\\IV1013\\" +
                    "PasswordCrack\\src\\com\\company\\dict.txt");
            File pass = new File("C:\\Users\\simon\\Documents\\KTHUppgifter\\IV1013\\" +
                    "PasswordCrack\\src\\com\\company\\passwd2.txt");
           // File dic2 = new File(args[0]);
            LinkedList<String> passwords = store(pass);
                Thread object
                        = new Thread(new hash1(dic,passwords));
                object.start();
                Thread thread
                        = new Thread(new hash2(dic,passwords)));
                thread.start();
            Thread thread2
                    = new Thread(new hash2(dic,passwords));
            thread2.start();
            double t1 = System.nanoTime();
            System.out.println("it takes " + Math.round((t1-t0)/(Math.pow(10,9)*60)) + " mins");
        }
}
