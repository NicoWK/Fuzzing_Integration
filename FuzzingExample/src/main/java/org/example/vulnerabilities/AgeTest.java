package org.example.vulnerabilities;

public class AgeTest {
    public String verifyAge(int age) {
        //input validation
        String result = null;
        if (age > 120 || age < 0) {
            result = "Age is not valid";

        } else{
            if (age < 14) {
                result = "You are according to law a child";
            } else if (age > 14 && age < 18){ // the age 14 is not evaluated (age >=14)
                result = "You are a juvenile according to the law";
            } else if (age > 18){
                result = "You are of legal age according to law";
            }
        }
        //unhandled NullPoniterException
        result = result.toLowerCase();
        return result;
    }
}
