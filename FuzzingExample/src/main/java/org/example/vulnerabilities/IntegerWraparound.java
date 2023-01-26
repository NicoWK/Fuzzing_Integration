package org.example.vulnerabilities;

public class IntegerWraparound {

    /**
     * This method calculates the final capital using the initial capital, monthly savings rate,
     * investment period in years, and annual interest rate. It returns the interest and deposit amounts.
     *
     * @param initialCapital The initial amount of money invested.
     * @param monthlySavings The monthly amount added to the investment.
     * @param investmentPeriod The number of years the investment is held.
     * @param annualInterestRate The annual interest rate in percent.
     * @return An int array containing the interest amount in the first index and deposit amount in the second index and the final capital in the third index.
     */
    public int[] calculateFinalCapital(int initialCapital, int monthlySavings, int investmentPeriod, int annualInterestRate) {
        // verify all parameters are positive and not 0
        if (initialCapital > 0 && monthlySavings > 0 && investmentPeriod > 0 && annualInterestRate > 0){
            int deposit = 0;
            int finalCapital=0;
            double interestRate = annualInterestRate / 100;
            //
            for (int i = 1; i <= investmentPeriod * 12; i++) {
                deposit += monthlySavings; // add the monthly savings to the deposit
                finalCapital += (int)(initialCapital * (interestRate / 12)) + monthlySavings; // add the interest and the deposit to the initial capital to calculate the final capital
            }
            int interest = finalCapital - deposit - initialCapital; //calculate the interest by subtracting the deposit and initial capital from the final capital.
            return new int[] {interest, deposit, finalCapital};
        } else {
            return new int[] {0,0,0};
        }

    }
}
