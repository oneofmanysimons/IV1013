package com.company;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.Scanner;

public class hash3 {
    File dic = null;
    LinkedList<String> passwords;

    public static synchronized LinkedList<String> decrementList(LinkedList<String> passwords, String password) {
        LinkedList<String> newList = new LinkedList<>(passwords);
        ;
        if (newList.remove(password)) {
            return newList;
        }
        return newList;
    }

    hash3(File dic, LinkedList<String> passwords) throws FileNotFoundException {
        this.dic = dic;
        this.passwords = passwords;
    }

    public static String salt(String pass) {
        String salty = pass.substring(0, 2);
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

    public void run() {
        Scanner scan = null;
        try {
            scan = new Scanner(this.dic);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        while (scan.hasNextLine()) {
            String line = scan.nextLine();
            String[] mangles = mangle(line);
            for (String mangle : mangles) {
                String[] doubleMangles = mangle(mangle);
                for (String doubleMangle : doubleMangles) {
                    for (String password : this.passwords) {
                        String mightBe = jcrypt.crypt(salt(password), doubleMangle);
                        if (mightBe.equals(password)) {
                            System.out.println(doubleMangle);
                            decrementList(this.passwords, password);
                            break;
                        }
                    }
                }
            }
        }
        scan.close();
    }
}

