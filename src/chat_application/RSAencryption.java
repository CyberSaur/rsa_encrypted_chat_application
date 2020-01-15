/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chat_application;

import java.io.InputStream;
import static java.nio.charset.StandardCharsets.UTF_8;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.util.Base64;
import javax.crypto.Cipher;
/**
 *
 * @author Suneth
 */
public class RSAencryption{
    
    public static KeyPair generateKeyPair() throws Exception{
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048, new SecureRandom());
        KeyPair pair = generator.generateKeyPair();
        return pair;
    }

    public static KeyPair getKeyPairFromKeyStore() throws Exception{
	// Generated with:
	// keytool -genkeypair -alias mykey -storepass s3cr3t -keypass s3cr3t -keyalg
	// RSA -keystore keystore.jks
	InputStream ins = RSAencryption.class.getResourceAsStream("/keystore.jks");
	KeyStore keyStore = KeyStore.getInstance("JCEKS");
	keyStore.load(ins, "s3cr3t".toCharArray()); // Keystore password
	KeyStore.PasswordProtection keyPassword = // Key password
	new KeyStore.PasswordProtection("s3cr3t".toCharArray());
	KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) keyStore.getEntry("mykey", keyPassword);
	java.security.cert.Certificate cert = keyStore.getCertificate("mykey");
	PublicKey publicKey = cert.getPublicKey();
	PrivateKey privateKey = privateKeyEntry.getPrivateKey();
	return new KeyPair(publicKey, privateKey);
    }

    public static String encrypt(String plainText, PrivateKey privateKey) throws Exception{
	Cipher encryptCipher = Cipher.getInstance("RSA");
	encryptCipher.init(Cipher.ENCRYPT_MODE, privateKey);
	byte[] cipherText = encryptCipher.doFinal(plainText.getBytes(UTF_8));
	return Base64.getEncoder().encodeToString(cipherText);
    }

    public static String decrypt(String cipherText, PublicKey publicKey) throws Exception{
	byte[] bytes = Base64.getDecoder().decode(cipherText);
	Cipher decriptCipher = Cipher.getInstance("RSA");
	decriptCipher.init(Cipher.DECRYPT_MODE, publicKey);
	return new String(decriptCipher.doFinal(bytes), UTF_8);
    }

    public static String sign(String plainText, PrivateKey privateKey) throws Exception{
        Signature privateSignature = Signature.getInstance("SHA256withRSA");
	privateSignature.initSign(privateKey);
	privateSignature.update(plainText.getBytes(UTF_8));
	byte[] signature = privateSignature.sign();
	return Base64.getEncoder().encodeToString(signature);
    }

    public static boolean verify(String plainText, String signature, PublicKey publicKey) throws Exception{
	Signature publicSignature = Signature.getInstance("SHA256withRSA");
	publicSignature.initVerify(publicKey);
	publicSignature.update(plainText.getBytes(UTF_8));
	byte[] signatureBytes = Base64.getDecoder().decode(signature);
	return publicSignature.verify(signatureBytes);
    }
}
