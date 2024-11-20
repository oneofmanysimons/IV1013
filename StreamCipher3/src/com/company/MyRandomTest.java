package com.company;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.Random;

public class MyRandomTest extends Random {
    BigInteger seed;
    Long x0 = Long.valueOf(1);
    public MyRandomTest(){
        super();
    }
    public MyRandomTest(BigInteger seed){
        super();
        this.seed = seed;
    }

    @Override
    public void setSeed(long seed){
        this.seed = BigInteger.valueOf(seed);
    }
    @Override
    public int next(int bits){
        Long m = getLargestPrime(seed.longValue());
        Long a = findPrimitive(m);
        Long b = Long.valueOf(0);
        Long xi = (a*x0 + b) % m.longValue();
        x0 = xi;

        int max = (int) (Math.pow(2,bits)-1);
        //does not work if seed >> max because stack die
        if (xi > max){
            return nextInt(bits);
        }
        return xi.intValue();
    }
    //https://www.geeksforgeeks.org/primitive-root-of-a-prime-number-n-modulo-n/
    // Returns true if n is prime
    static boolean isPrime(Long n)
    {
        // Corner cases
        if (n <= 1)
        {
            return false;
        }
        if (n <= 3)
        {
            return true;
        }

        // This is checked so that we can skip
        // middle five numbers in below loop
        if (n % 2 == 0 || n % 3 == 0)
        {
            return false;
        }

        for (int i = 5; i * i <= n; i = i + 6)
        {
            if (n % i == 0 || n % (i + 2) == 0)
            {
                return false;
            }
        }

        return true;
    }

    /* Iterative Function to calculate (x^n)%p in
    O(logy) */
    static int power(int x, int y, int p)
    {
        int res = 1;     // Initialize result

        x = x % p; // Update x if it is more than or
        // equal to p

        while (y > 0)
        {
            // If y is odd, multiply x with result
            if (y % 2 == 1)
            {
                res = (res * x) % p;
            }

            // y must be even now
            y = y >> 1; // y = y/2
            x = (x * x) % p;
        }
        return res;
    }

    // Utility function to store prime factors of a number
    static void findPrimefactors(HashSet<Integer> s, Long n)
    {
        // Print the number of 2s that divide n
        while (n % 2 == 0)
        {
            s.add(2);
            n = n / 2;
        }

        // n must be odd at this point. So we can skip
        // one element (Note i = i +2)
        for (int i = 3; i <= Math.sqrt(n); i = i + 2)
        {
            // While i divides n, print i and divide n
            while (n % i == 0)
            {
                s.add(i);
                n = n / i;
            }
        }

        // This condition is to handle the case when
        // n is a prime number greater than 2
        if (n > 2)
        {
            s.add(n.intValue());
        }
    }

    // Function to find smallest primitive root of n
    static Long findPrimitive(Long n)
    {
        HashSet<Integer> s = new HashSet<Integer>();

        // Check if n is prime or not
        if (isPrime(n) == false)
        {
            return Long.valueOf("-1");
        }

        // Find value of Euler Totient function of n
        // Since n is a prime number, the value of Euler
        // Totient function is n-1 as there are n-1
        // relatively prime numbers.
        Long phi = n - 1;

        // Find prime factors of phi and store in a set
        findPrimefactors(s, phi);

        // Check for every number from 2 to phi
        for (int r = 2; r <= phi; r++)
        {
            // Iterate through all prime factors of phi.
            // and check if we found a power with value 1
            boolean flag = false;
            for (Integer a : s)
            {

                // Check if r^((phi)/primefactors) mod n
                // is 1 or not
                if (power(r, phi.intValue() / (a), n.intValue()) == 1)
                {
                    flag = true;
                    break;
                }
            }

            // If there was no power with value 1.
            if (flag == false)
            {
                return Long.valueOf(r);
            }
        }

        // If no primitive root found
        return Long.valueOf("-1");
    }

    //https://stackoverflow.com/questions/66112449/find-the-largest-prime-factor-of-a-given-number
    public static Long getLargestPrime(Long number) {

        if (isPrime(number)){
            return number;
        }

        if (number < 0) {
            return Long.valueOf(-1);
        }

        for (Long i = number - 1; i > 1; i--) {
            if (number % i == 0) {
                boolean flag = true;
                for (int j = 2; j < i; j++) {
                    if (i % j == 0) {
                        flag = false;
                        break;
                    }
                }
                if (flag)
                    return i;
            }
        }
        return Long.valueOf("-1");
    }
}