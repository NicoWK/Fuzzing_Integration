package org.example.vulnerabilities;

public class ImproperInputValidation {

    public byte[] rollDice(int rounds){
        byte[] results = new byte[rounds];
        for(int i = 0; i < rounds; i++){
            results[i] = (byte) (Math.random() * 6 +1);
        }
        return results;
    }
}
