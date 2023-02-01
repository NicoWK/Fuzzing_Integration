package org.example.vulnerabilities;

import java.util.ArrayList;

public class ResourceConsumption {
    /** Recursive function to calculate the total cost via cost per day and days
     * or one week a discount of 5% is added, for one month 10½ and for more than three months 15½
     * @param days days to be multiplied
     * @param costPerDay cost per day
     * @return The total cost.
     *
     **/
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
}
