package it.unimore.wldt.test.coap.smartobject;

import java.util.Random;


public class StringGenerator {

    public static String generateRandomAlphanumericString() {

        try{

            int leftLimit = 48; // numeral '0'
            int rightLimit = 122; // letter 'z'
            int targetStringLength = 10;

            Random random = new Random();

            String generatedString = random.ints(leftLimit, rightLimit + 1)
                    .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                    .limit(targetStringLength)
                    .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                    .toString();

            return generatedString;

        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

}
