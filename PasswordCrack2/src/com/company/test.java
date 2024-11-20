package com.company;

public class test implements Runnable{
    int x = 0;
    test(int x){
        this.x = x;
    }
    public void run(){
        x = 7;
        System.out.println("Hello " +
                "Thread " + Thread.currentThread().getId()
                + " is running " + this.x);
    }
}
