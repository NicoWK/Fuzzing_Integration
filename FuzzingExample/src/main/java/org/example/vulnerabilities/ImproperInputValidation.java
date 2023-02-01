package org.example.vulnerabilities;

public class ImproperInputValidation {

    /**
     *  This method generates a random sequence of dice rolls, based on a provided number of rounds.
     *  The number of rounds is not validated.
     * @param rounds The number of dice rolls to generate
     * @return An array of bytes representing the results of the dice rolls
     */
    public byte[] rollDice(int rounds){
        byte[] results = new byte[rounds];
        for(int i = 0; i < rounds; i++){
            results[i] = (byte) (Math.random() * 6 +1);
        }
        return results;
    }
}
