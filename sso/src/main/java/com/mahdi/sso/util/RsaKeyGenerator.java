package com.mahdi.sso.util;

import jakarta.xml.bind.DatatypeConverter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * Utility class for generating RSA key pairs
 */
public class RsaKeyGenerator {
    
    public static void main(String[] args) {
        try {
            // Generate SSO keys
            KeyPair ssoKeyPair = generateKeyPair();
            System.out.println("=== SSO Keys ===");
            System.out.println("SSO Private Key:");
//            System.out.println(formatPrivateKey(ssoKeyPair.getPrivate()));
            System.out.println("\nSSO Public Key:");
//            System.out.println(formatPublicKey(ssoKeyPair.getPublic()));
            
            System.out.println("\n" + "=".repeat(50) + "\n");
            
            // Generate Gateway keys
            KeyPair gatewayKeyPair = generateKeyPair();
            System.out.println("=== Gateway Keys ===");
            System.out.println("Gateway Private Key:");
//            System.out.println(formatPrivateKey(gatewayKeyPair.getPrivate()));
            System.out.println("\nGateway Public Key:");
//            System.out.println(formatPublicKey(gatewayKeyPair.getPublic()));
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private static KeyPair generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        return keyPairGenerator.generateKeyPair();
    }

    public static PublicKey getPublic() throws Exception {

        byte[] keyBytes = DatatypeConverter.parseBase64Binary(formatPublicKey());
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePublic(spec);
    }
    
    public static String formatPrivateKey() {
        return """
                -----BEGIN PRIVATE KEY-----
                MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQDIbZFwzxF3tPZB
                zwKXgF7MoWAoPmTDpoO5vU4l50ytkVGT8kPD6sbNf2ZAFYbq4F2UjwJp2ELcFGmR
                NDtf5354LM6LWZdNKZMgZQfD5VPOmIjkLNI/BBvVs6CG60u2Xu8ccWHWRxDlIn1K
                RH4ZKZ2jkjEVkMNfWishbQzG7TWC4Y3IoSHwwC4llX6F/BqJDjjV0w8wLKI74VT/
                3sE1AKggDUzrE8x7Yydyy8cMpUFyPh8GoDDWkK1C1ZUXmdZkphD/XPuKCKtTD/vb
                +dMOkIfrlJf1rTbK3H2tu6uvpRCrtaacj+VwmbT4R+ixBJmP/YM/+VxXu2eRm4HS
                tgQrD3GhAgMBAAECggEAPXlFw6jvPZ0WbubUb61i5jaU7KL+evfmrSCACKYh4ZaR
                w+PpnvyyjzMFJKC1qfK5ISif3+EB2Mi1/GBd2bOCGc/8ZcdL3dHjm2sBb7c19kbY
                rcuFjOhwtSIMGXWv2jbR9hJcpMFVUEI81XuehY87F4FuZqPpGKTqV60/3v/Aiuyh
                84HZKbjpWcStuMO/szN2qoAYnOo7Ipvj2B+I2AMNqPIi0TvnXKNCT0JBb+Un4Q1w
                5TStf+gcfKnMcrbDkwDfNNGFDCxfgMnLO3DDMNC0Hw35b8di0Yyb5/J/fy15Vjck
                nKfxsi97nxYLaPeglC7d723myorlsYKGhCsnMBft5QKBgQD3qX7pdeSLLPDNDY2A
                QTS6vpbta2EvzNTrx6QX2BhtJL0pgCSJLMun7a0SV2Y1C9qQjhROj00qJX5uNj0n
                KoKdnrYXLcfTSS5EqaTF7xVxswF2W49iXg5iRSMe0c2c5gNzzyGxW/+7QrHOACLF
                NNLndK8WeDqTySTwp7mT7O73OwKBgQDPLPrb29bY2OfXnvOVL+ECHz+ogxslctfo
                o9dCjbLpBw7VML+zRbHCgMucAvbxHhJUOOykAZQ51hsqVoCOpvWPtKWyU2WKHp/V
                algF+887dpVkVxQAwj5NemuzxzkxKye5vVaWRV03R3PQQC8ANR6InEJjAbPX6r6t
                GeYlfmpE0wKBgQDvj1t8thXw7fKrVl22XtyDetIBK+ohU4/t1foLOnM+N9hmKVat
                /c+tS0ErSbFKxIGRbG2GJCVvDD+EmiCDHyKjFp9qQlVVMid4MWR6Na7XgWB7vUsv
                ym09b0TujeDG0NMsFYKwzZ3L5FKbQhRgD52mV375TCjVRS7fPk5LIVdxewKBgC3t
                2dtd/F5sj9FkgrnDOI3g4Zr5Hc6KjEUd9X3irhQtJWOsAAO9YrAtH3aBzb8cnJGh
                YQzm8LZ1ueYD8VIuv2fRc556EJuzOFn2znbdIU8cdfgdueiFJ4zDpBbiBtLYr2Dl
                EcoUrJqxs4IqiIDyRcehZLJQUU9/0i/L+xK62NJTAoGAHen9w20HuYo1x2Mcm2p2
                SlT4HXkMsmTGemAP6GCRFz5A+xNo8cVFRXz5qLybf5UeBQpThL+1GnMTn4aGKm/j
                yaNZzNv6+je7n070DntTBdlkJ5k0kqgZ7MknpRJ82srh4+Vf6DiVZiX+B9MSccdQ
                Dymvqdl82KavLIJE/2Jgetk=
                -----END PRIVATE KEY-----
                """;
    }

    public static PrivateKey readPrivateKey() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        StringBuilder pkcs8Lines = new StringBuilder();
        BufferedReader rdr = new BufferedReader(new StringReader(formatPrivateKey()));
        String line;
        while ((line = rdr.readLine()) != null) {
            // Remove the "BEGIN" and "END" lines
            if (line.contains("-BEGIN") || line.contains("-END"))
                continue;
            pkcs8Lines.append(line);
        }

        String pkcs8Pem = pkcs8Lines.toString();
        pkcs8Pem = pkcs8Pem.replaceAll("\\s+", "");

        byte[] pkcs8EncodedBytes = org.apache.commons.codec.binary.Base64.decodeBase64(pkcs8Pem);

        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(pkcs8EncodedBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(keySpec);
    }
    
    public static String formatPublicKey() {
       return """
               -----BEGIN PUBLIC KEY-----
               MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAyG2RcM8Rd7T2Qc8Cl4Be
               zKFgKD5kw6aDub1OJedMrZFRk/JDw+rGzX9mQBWG6uBdlI8CadhC3BRpkTQ7X+d+
               eCzOi1mXTSmTIGUHw+VTzpiI5CzSPwQb1bOghutLtl7vHHFh1kcQ5SJ9SkR+GSmd
               o5IxFZDDX1orIW0Mxu01guGNyKEh8MAuJZV+hfwaiQ441dMPMCyiO+FU/97BNQCo
               IA1M6xPMe2MncsvHDKVBcj4fBqAw1pCtQtWVF5nWZKYQ/1z7igirUw/72/nTDpCH
               65SX9a02ytx9rburr6UQq7WmnI/lcJm0+EfosQSZj/2DP/lcV7tnkZuB0rYEKw9x
               oQIDAQAB
               -----END PUBLIC KEY-----
                              
               """;
    }
    
    private static String insertLineBreaks(String key) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < key.length(); i += 64) {
            result.append(key, i, Math.min(i + 64, key.length()));
            if (i + 64 < key.length()) {
                result.append("\n");
            }
        }
        return result.toString();
    }
}