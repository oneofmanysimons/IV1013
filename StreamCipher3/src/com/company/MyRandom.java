package com.company;

import java.math.BigInteger;
import java.util.Random;;

public class MyRandom extends Random {
    long seed;
    int m = 97;
    int a = 5;
    int b = 0;
    public MyRandom(){
        super();
    }
    public MyRandom(long seed){
        super();
        setSeed(seed%m);
    }
    @Override
    public void setSeed(long seed){
        this.seed = seed;
    }
    @Override
    public int next(int bits){
        setSeed((a*seed + b) % m);
        long max = (long) (Math.pow(2,bits)-1);
        //does not work if seed >> max because stack die
        if (this.seed > max){
            return nextInt(bits);
        }
        return (int) this.seed;
    }
}
