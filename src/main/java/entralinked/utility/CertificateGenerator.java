package entralinked.utility;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Calendar;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.AuthorityKeyIdentifier;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

/**
 * Utility class for generating SSL certificates that are trusted by the DS.
 */
public class CertificateGenerator {
    
    private static final Logger logger = LogManager.getLogger();
    private static final String issuerCertificateString = 
            "eNp9lNuyqjgQhu95irm3dokoHi4TEk4aNBBAuAPRIOBZCPL0G5e1Zq+ZqZpcdqr/Tvf/dX796g/EhuX8pWGXWbqlAYbfwV8SsSw0ZZoGRksOhAUBt6BRAAfy8paXR2"
            + "MhZAiorwOkwQLhFQGlAUY+hjnRgoC0Eu6AC7kT9JlMq7J8p+TX9JTJqTJpLQT2n7sL02X1mp7dKj25jWUsThbWn/HWvkqRgrkXqgWhQmg8QgGlKyycNZNxqyPgfQ"
            + "QI034KYAcSzxIWiGxpeYmtvNk5gJZQz50i8mGdKrhOT1WdGUEdG/M6UhZPAidbxIBC0K4lBVZIR7t1cNlKiJF3UDjo76DgMW21Dtif6hEDVcCISwSmXy+0sLhuo6"
            + "1zSUL1LGXGoiYuEOjzfBOLzItD95kqakk8LEzxFV/iNl/3CXIcqmX6gk18hDANg5eUhE5u4aqOXpNW70Dw3TOqsmviwXUgE+6bdhNroP3u+2fb0v/2bRkHAmRD82"
            + "6GZ6VjRPHbUgAmhvO29UiXUOIUxaq8cRfEcrHijeaFOyPquizn6UQE7fi1TXYvmBeHyPM2bmovzVXhogl7ni+zk51OJPNg7LEiKy5bdA8gBlNTni6aod9MydZdDT"
            + "S+3AB9tqvjKfNJ4pbyKps14fO4qZMiDqJaKumJE81SqqOjWc0Qbs8g2bvuphuMcwzY4Kjfh+tNFWwYXy9ojxYF8DKxIOt6ev2HkLS3Na7MILAEQOABecXzksO8yT"
            + "VAZWy2UcbGsEorhzENmr0L1e6k925QTpXehcyoTm8nMsMX5kdsDWGE9dWRdObZEVSFNwXe6krfkNnwah6TrB+u+nbL9AiWeIoFT6ZAN59bv5u3dzNr54fRUuUL9H"
            + "CCgMZD7xIZ3tUyNn0iIfQhtA9QBhZ2IPkdZqTf1PeGaZAsA0XvEqOq47HbpAXmPcKfu5xs2R+beapEnIajStqdqiLyoJ2eSIsQWH5AejAgqxvKsE+g9SXQY7T6Kd"
            + "CPp7UKwKWfe69RHGLFyamhNpGn1n/QVYtUUfqhAuF85nSA2kSsELhJF+3I7UtUJqYr79ClWSnOK9X+nSw3/6B0bD++CZW+EYUfRCEwwmJiGDf3tDvYLpqfj+6Tne"
            + "Fw2ewOxmIyw3d4HFGrLx9yvk6Q5HpTlSgvbgO22k/vo0N43dO7utgfn/e4Xo9alIAktnewGqj5nRm75H7iaLEJBo983jJdugeXdp6y5X6sDmaDmyw/p91TKPrKmD"
            + "4ebbTY38D5WT67fONjW/X1OLNHg46GYYZa6etbxQ7671f7GypgwnI=";
    
    private static final String issuerPrivateKeyString = 
            "eNod0Em2a0AAANAFGRBNYVgK0QalfWaIPproZfX/nH+XcG1dR4mnSxCaUu3JKUe5WLR1rND+Q+gwb3NO3ws5e0YXcydZcUtNV/35votzw9SsDstssI0TPxg5q1XPUq"
            + "EpGgfib4UnATQKiAcZHsBOsEWg2nShyhd7CoLQznBPWW/+iLfW3bMujf723htqG+n0p30h/SClZIRZibH7I5hGgQHRqgvpuJ/IDWpH9HQZelCC0xPK8uXZ87jcwv"
            + "Al64ufS7EqDw4HZfb6fi81sZvq02Xx84aBxLRvYg+ASqvaLsqPb9CrjrxW0koV02lUS9bWCSuQctd+t4w/3BwIjy0ZUdeOqEKzl95s4Sgcw66TQ55RWT5f78KalH"
            + "ncOiUv2Gk9VoV8tHw7sYC9RsH3m2xH6QeAFirI8+Qff3Lmmgrp3rqZbymH8skaH7Cku2uPPCe0xn2oP3uM7pOqtbvUKFyUyZ9tKqj7StW5zJ2UgvpYMeuc7INHAo"
            + "hV2CuQM0H0TU3f/wzd42bWlkx50Fc5eWTmBGCsMe95I05TreXUNFNLiisySELsXkZC8JNhR3KaLsmrWI4fa6iNpzrP1TOg/6StsTjERretEDEB7gZ28+mhQAs7T6"
            + "MzgI1W5a+hUvdFVGB43qZr5eOJ07g/FVIZ7xCYsGlcooISBawqUerOMyT4ivKwCC8XMn0wDNXSVb/+FtVXuzmmoQKx6C2OiW921ctlpI7+L7QPxl9RWjM8QsdL54"
            + "4sSHNmIW+3mVEWW8H9+3/s59kaKT97p4oFm+9Jc1hKrMu5/lhDrSe0QqBnwRnZx0jKRqdxHG7Z6lflnu0SfxCFKIwe2laBvNJzVHPlTayy/g/JGQy0";
    
    private static X509Certificate issuerCertificate;
    private static PrivateKey issuerPrivateKey;
    private static boolean initialized;
    
    public static void initialize() {
        if(initialized) {
            logger.warn("CertificateGenerator is already initialized!");
            return;
        }
        
        System.setProperty("jdk.tls.useExtendedMasterSecret", "false"); // Will straight up not reuse SSL sessions if this is enabled
        Security.setProperty("jdk.tls.disabledAlgorithms", "");
        Security.setProperty("jdk.tls.legacyAlgorithms", "");
        Security.insertProviderAt(new BouncyCastleProvider(), 1);
        
        try {
            issuerCertificate = (X509Certificate)CertificateFactory.getInstance("X.509").generateCertificate(
                    new ByteArrayInputStream(decodeAndDeflate(issuerCertificateString)));
        } catch (CertificateException | DataFormatException e) {
            logger.error("Could not create issuer certificate", e);
        }
        
        try {
            byte[] keyBytes = Base64.getDecoder().decode(decodeAndDeflate(issuerPrivateKeyString));
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            issuerPrivateKey = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(keyBytes));
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | DataFormatException e) {
            logger.error("Could not create issuer private key", e);
        }
        
        initialized = true;
    }
    
    public static KeyStore generateCertificateKeyStore(String type, String password) throws GeneralSecurityException, IOException {
        // Generate subject keys
        KeyPairGenerator keyGenerator = KeyPairGenerator.getInstance("RSA");
        keyGenerator.initialize(1024);
        KeyPair keyPair = keyGenerator.generateKeyPair();
        SubjectPublicKeyInfo publicKeyInfo = SubjectPublicKeyInfo.getInstance(keyPair.getPublic().getEncoded());
        
        // Generate start/end dates
        Calendar notBefore = Calendar.getInstance();
        Calendar notAfter = Calendar.getInstance();
        notAfter.add(Calendar.YEAR, 50);
        
        // Voodoo because PKCS12 will cry that the DN doesn't match if the attributes aren't in the same order
        X500Name issuerName = new JcaX509CertificateHolder(issuerCertificate).getSubject();
        
        // Configure certificate builder
        X509v3CertificateBuilder builder = new X509v3CertificateBuilder(
                issuerName,
                BigInteger.ONE,
                notBefore.getTime(),
                notAfter.getTime(),
                new X500Name("CN=*.*.*"),
                publicKeyInfo)
        .addExtension(Extension.authorityKeyIdentifier, false, AuthorityKeyIdentifier.getInstance(
                JcaX509ExtensionUtils.parseExtensionValue(issuerCertificate.getExtensionValue(Extension.authorityKeyIdentifier.getId()))));
        
        // Sign certificate and create chain
        ContentSigner signer = null;
        
        try {
            signer = new JcaContentSignerBuilder("SHA1withRSA").build(issuerPrivateKey);
        } catch(OperatorCreationException e) {
            // Delegate exception
            throw new GeneralSecurityException(e);
        }
        
        X509Certificate subjectCertificate = new JcaX509CertificateConverter().getCertificate(builder.build(signer));
        X509Certificate[] certificateChain = { subjectCertificate, issuerCertificate };
        
        // And finally, create the keystore
        KeyStore keyStore = KeyStore.getInstance(type);
        keyStore.load(null, null);
        keyStore.setCertificateEntry("server", subjectCertificate);
        keyStore.setKeyEntry("server", keyPair.getPrivate(), password == null ? null : password.toCharArray(), certificateChain);
        return keyStore;
    }
    
    private static byte[] decodeAndDeflate(String input) throws DataFormatException {
        byte[] bytes = Base64.getDecoder().decode(input.getBytes());
        Inflater inflater = new Inflater();
        inflater.setInput(bytes);
        byte[] buffer = new byte[2048];
        byte[] output = new byte[inflater.inflate(buffer)];
        System.arraycopy(buffer, 0, output, 0, output.length);
        return output;
    }
}
