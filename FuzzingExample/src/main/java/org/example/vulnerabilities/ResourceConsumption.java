package org.example.vulnerabilities;

import java.util.ArrayList;

public class ResourceConsumption {
    //Recursive function to calculate the total cost via cost per day and days
    // For one week a discount of 5% is added, for one month 10½ and for more than three months 15½
    public int calculateCostRekursive(int days, int costPerDay) {
        //validate that the inputs are positive numbers
        if (days > 0 && costPerDay > 0){
            if (days == 1) {
                return costPerDay;
            }
            int totalCost = costPerDay + calculateCostRekursive(days - 1, costPerDay);
            if (days >= 7) {
                totalCost = (int) (totalCost * 0.95);
            }
            if (days >= 30) {
                totalCost = (int) (totalCost * 0.90);
            }
            if (days >= 90) {
                totalCost = (int) (totalCost * 0.85);
            }
            return totalCost;
        } else {
            // Convert negative inputs to positive inputs
            days = Math.abs(days);
            costPerDay = Math.abs(costPerDay);
            return calculateCostRekursive(days, costPerDay);
        }

    }
    /**
     * This method takes an integer as input and returns a list of its prime factors.
     *
     * @param num the integer to be prime factorized
     * @return an ArrayList of prime factors of the input integer
     */
    public static ArrayList<Integer> primeFactorize(int num) {
        ArrayList<Integer> factors = new ArrayList<Integer>();
        for (int i = 2; i <= num / i; i++) {
            // Divide the input number by i as long as it is divisible by i.
            while (num % i == 0) {
                factors.add(i);
                num /= i;
            }
        }
        // If the input number is greater than 1 and no further factors are found, it is added as the last factor.
        if (num > 1) {
            factors.add(num);
        }
        return factors;
    }
}
