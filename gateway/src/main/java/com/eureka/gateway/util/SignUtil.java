package com.eureka.gateway.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.xml.bind.DatatypeConverter;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.codec.binary.Base64;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

@Service
@Log4j2
public class SignUtil {


    private static String publicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAzwYeMHB6XDoHpizJ785KcabXAAGsvaQ/ACnZtHNEoO/fSmRsBKERUMWt1GRUbMuX7ZOimA/sxGtZptV/wI+P3LuVpCKoVLeR0UIPrUUkXlEQ9UFPYHZHg2eWftTssI1EQOEcq/HcDo3/6lCtnepzpJ2AKMuXtWNQNx7gd4m/L7mG5jiLSjL3NJ+bMU+iNoeF6frqxewJMBx3ugeQRqL+m9VZZeDkOxwIo1SxYLdJgtXlqNY5PUDO/6CEKmGRx3eQRVEq1E5gkpET7QvBY/U5avdYuRptlr5SuMM+iOHrqptPsRY053ddpw3JkXKXKtnIlADjuUBe6LkVI5YwE8bXWwIDAQAB";

    private static String privateKey = "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQDPBh4wcHpcOgem\n" +
            "LMnvzkpxptcAAay9pD8AKdm0c0Sg799KZGwEoRFQxa3UZFRsy5ftk6KYD+zEa1mm\n" +
            "1X/Aj4/cu5WkIqhUt5HRQg+tRSReURD1QU9gdkeDZ5Z+1OywjURA4Ryr8dwOjf/q\n" +
            "UK2d6nOknYAoy5e1Y1A3HuB3ib8vuYbmOItKMvc0n5sxT6I2h4Xp+urF7AkwHHe6\n" +
            "B5BGov6b1Vll4OQ7HAijVLFgt0mC1eWo1jk9QM7/oIQqYZHHd5BFUSrUTmCSkRPt\n" +
            "C8Fj9Tlq91i5Gm2WvlK4wz6I4euqm0+xFjTnd12nDcmRcpcq2ciUAOO5QF7ouRUj\n" +
            "ljATxtdbAgMBAAECggEAUKZGfoJi+KjWsAMEzDomQC5J1cPRQrPIo0yqdiTtmHC6\n" +
            "ISYL+qWwtDG+bV6EkTmjPzdjgS+7Ai376AWGVkLXPZuKST4DK7WzxbyhlNO5vlCA\n" +
            "dbryrFaHt4ZUV6alaoYuD8Riwg1ft//TsbmqWTmrwXZmJf5iZJSC/GY39fmglHtT\n" +
            "u1MHSveU/mq0YqurzUYKhHBiXYEww8c4cLArAtpMmpPPJNL8bMiK4UmfG6Fz4jju\n" +
            "NsQXkxpqlqNO7+BfF7je7pnLj3i2LgWHtosF6RG3ZiQn6DXs0/DjvOi+S+uQ6xxn\n" +
            "gjBWZtjwShINgGKyg9Oy4H9wMkEyLrD335tpaNMR4QKBgQDuTSf6ueDMi/RqZUvR\n" +
            "4o1CEBW2UCvzyLBw4f3m/P/UPV8mR2Kq2af81tuKK8MMJ6Ajg1rkuqAcin08fEr/\n" +
            "KGLpERME0hV8N3BUAjgoEt7EMmeeWEkzV6IVu31gKEUX5Y7lAXaSiszD8g+DdU9J\n" +
            "HNE75f24QbjxDCraQysTn3XwkQKBgQDeZke8u5vgLZma7eK4SKYM1owbUsU+rPCC\n" +
            "+rmje1aVo6yUPAqZmjJSgtHZJ2l14cO3dezu0wEj32pQ0qTELI1m7RVtVpjQZRPg\n" +
            "rMMMvKbAi56oZWQsVyNM7J+MXqYQUO6rnXdW94fJA7Ej50DdliDJrqQTmHviN15s\n" +
            "7P/cpmf/KwKBgQCKarClNyC3TzfaMRp4QELSs6sY6bqN8O1jtDEZ4azr5/YDswVB\n" +
            "vgmQmHCO8lpqDf47goniP/DOgza5UmzxhtDlFfDZJPor27vYYC2kQUm55pk/ZYKn\n" +
            "Wuif/PaXSuzPM5zrsgzgk9TaoBiYwCQckKuMQkw8oZg+E1Y0zz0POdl0sQKBgHxq\n" +
            "baFotvf/qpngkOAVTEeMUs8KS+wXJWmwx99sJXELIoW+3RM3DwXXx3ubL8VRqoLc\n" +
            "xtKIWm/uEXTrkl9oqSY2ZbFIK68RjMA5Bdj/RK4crYJ5TkP773SeP1dwr6gbDpoV\n" +
            "Omj9iwnqNdPnEAOmc+s/9uw7drqDzSBUqYKP2UgHAoGBAKBk9mAQdChbrH7S+qYc\n" +
            "GjxcM579ORgTkdq+sqDQrqp81MnKdxgYo4r1u6gSq9ollgTiVXxYj1tZdiafBBk5\n" +
            "P347Ur2T4Prb4Zaq5vpox4iOjaFh2jRGScz0fEhwhngHHXjXH6SnbNh/AcAISkxN\n" +
            "uhuq9Y6R4Otj3H3nj03+rot3";

    public static String signValue(String input) {

        try {
            PrivateKey privateKey = readPrivateKey();
            byte[] sign = new byte[0];
            sign = sign(privateKey, input);
            return org.apache.commons.codec.binary.Base64.encodeBase64String(sign);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static byte[] sign(PrivateKey prvKey, String message) throws UnsupportedEncodingException {
        try {
            Signature signer = Signature.getInstance("SHA256withRSA");
            signer.initSign(prvKey);
            signer.update(message.getBytes("UTF-8"));
            return signer.sign();
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException ex) {
            // log error
        }
        return null;
    }

    private static PrivateKey readPrivateKey() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        StringBuilder pkcs8Lines = new StringBuilder();
        BufferedReader rdr = new BufferedReader(new StringReader(privateKey));
        String line;
        while ((line = rdr.readLine()) != null) {
            // Remove the "BEGIN" and "END" lines
            if (line.contains("-BEGIN") || line.contains("-END"))
                continue;
            pkcs8Lines.append(line);
        }

        String pkcs8Pem = pkcs8Lines.toString();
        pkcs8Pem = pkcs8Pem.replaceAll("\\s+", "");

        byte[] pkcs8EncodedBytes = Base64.decodeBase64(pkcs8Pem);

        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(pkcs8EncodedBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(keySpec);
    }

    public static String mapToJsonOrNull(Object object) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.error("mapToJson JsonProcessingException !", e);
            return "";
        }
    }

    public static boolean verifySignature(byte[] data, byte[] signature) throws Exception {
        Signature sig = Signature.getInstance("SHA256withRSA");
        sig.initVerify(getPublic(publicKey));
        sig.update(data);
        return sig.verify(org.bouncycastle.util.encoders.Base64.decode(signature));
    }

    private static PublicKey getPublic(String key) throws Exception {

        byte[] keyBytes = DatatypeConverter.parseBase64Binary(key);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePublic(spec);
    }
}
