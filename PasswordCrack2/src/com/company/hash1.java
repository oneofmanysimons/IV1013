package com.company;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.Scanner;

public class hash1 implements Runnable{
    File dic = null;
    LinkedList<String> passwords;
    public static synchronized LinkedList<String> decrementList(LinkedList<String> passwords, String password){
        LinkedList<String> newList = new LinkedList<>(passwords);;
        if (newList.remove(password)){
            return newList;
        }
        return newList;
    }
    hash1(File dic, LinkedList<String> passwords) throws FileNotFoundException {
        this.dic = dic;
        this.passwords = passwords;
    }
    public static String salt(String pass){
        String salty = pass.substring(0,2);
        return salty;
    }
    public void run() {
            Scanner scan = null;
            try {
                scan = new Scanner(this.dic);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            // Collect passwords to remove
            while (scan.hasNextLine()) {
                String line = scan.nextLine();
                for (String password : this.passwords) {
                    String mightBe = jcrypt.crypt(salt(password),line);
                    if (mightBe.equals(password)) {
                        System.out.println(line);
                        decrementList(passwords, password);
                        break;
                    }
                }
            }
            scan.close();
        }
    }
