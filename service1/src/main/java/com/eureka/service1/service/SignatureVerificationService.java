package com.eureka.service1.service;

import com.eureka.service1.model.SignedTokenData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.util.Base64;

@Slf4j
@Service
@RequiredArgsConstructor
public class SignatureVerificationService {
    private final PublicKeyService publicKeyService;

    /**
     * تأیید امضای توکن با استفاده از کلید عمومی Gateway
     */
    public boolean verifySignature(SignedTokenData signedTokenData) {
        try {
            PublicKey publicKey = publicKeyService.getGatewayPublicKey();
            if (publicKey == null) {
                log.error("Service1: کلید عمومی Gateway در دسترس نیست");
                return false;
            }

            String dataToVerify = signedTokenData.getSignedData();
            String signature = signedTokenData.getSignature();

            boolean isValid = verifySignatureWithPublicKey(dataToVerify, signature, publicKey);
            
            if (isValid) {
                log.info("Service1: امضای توکن با کلید عمومی Gateway تأیید شد");
            } else {
                log.warn("Service1: امضای توکن با کلید عمومی Gateway تأیید نشد");
            }
            
            return isValid;
            
        } catch (Exception e) {
            log.error("Service1: خطا در تأیید امضای توکن: {}", e.getMessage());
            return false;
        }
    }

    /**
     * تأیید امضا با استفاده از کلید عمومی RSA
     */
    private boolean verifySignatureWithPublicKey(String data, String signature, PublicKey publicKey) {
        try {
            Signature sig = Signature.getInstance("SHA256withRSA");
            sig.initVerify(publicKey);
            sig.update(data.getBytes());
            
            byte[] signatureBytes = Base64.getDecoder().decode(signature);
            boolean isValid = sig.verify(signatureBytes);
            
            log.debug("Service1: تأیید امضای RSA: {}", isValid);
            return isValid;
            
        } catch (SignatureException e) {
            log.error("Service1: خطا در تأیید امضای RSA: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("Service1: خطا در تأیید امضای RSA: {}", e.getMessage());
            return false;
        }
    }

    /**
     * تأیید امضا با استفاده از hash (روش جایگزین)
     */
    public boolean verifySignatureWithHash(SignedTokenData signedTokenData) {
        try {
            String expectedSignature = generateHash(signedTokenData.getSignedData());
            String actualSignature = signedTokenData.getSignature();
            
            boolean isValid = expectedSignature.equals(actualSignature);
            
            if (isValid) {
                log.info("Service1: امضای توکن با hash تأیید شد");
            } else {
                log.warn("Service1: امضای توکن با hash تأیید نشد");
            }
            
            return isValid;
            
        } catch (Exception e) {
            log.error("Service1: خطا در تأیید امضای hash: {}", e.getMessage());
            return false;
        }
    }

    /**
     * تولید hash از داده‌ها
     */
    private String generateHash(String data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.getBytes());
            return Base64.getEncoder().encodeToString(hash);
            
        } catch (NoSuchAlgorithmException e) {
            log.error("Service1: خطا در تولید hash: {}", e.getMessage());
            return null;
        }
    }

    /**
     * تأیید جامع امضا (ابتدا RSA، سپس hash)
     */
    public boolean verifySignatureComprehensive(SignedTokenData signedTokenData) {
        // ابتدا با کلید عمومی RSA تأیید کن
        boolean rsaValid = verifySignature(signedTokenData);
        if (rsaValid) {
            return true;
        }

        // اگر RSA موفق نبود، با hash تأیید کن
        log.warn("Service1: تأیید RSA ناموفق، تلاش با hash");
        return verifySignatureWithHash(signedTokenData);
    }

    /**
     * بررسی دسترسی کلید عمومی Gateway
     */
    public boolean isGatewayPublicKeyAvailable() {
        return publicKeyService.hasCachedPublicKey() || 
               publicKeyService.getGatewayPublicKey() != null;
    }

    /**
     * به‌روزرسانی کلید عمومی Gateway
     */
    public void refreshGatewayPublicKey() {
        log.info("Service1: به‌روزرسانی کلید عمومی Gateway");
        publicKeyService.refreshPublicKey();
    }
} 