package com.example.multipleaccountsproject;

import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;

import androidx.biometric.BiometricPrompt;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.Signature;

public class BiometricCryptoExample {

    private static final String KEY_ALIAS = "myKeyAlias";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private Cipher cipher;  // This will hold the Cipher used for encryption

    // Method to generate or get the secret key from Android Keystore
    private SecretKey getKey() throws Exception {
        KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);

        SecretKey key = (SecretKey) keyStore.getKey(KEY_ALIAS, null);
        if (key == null) {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
            keyGenerator.init(
                    new KeyGenParameterSpec.Builder(KEY_ALIAS, KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                            .build());
            key = keyGenerator.generateKey();
        }
        return key;
    }

    // Method to initialize or get the Cipher instance
    public Cipher getCipher() throws Exception {
        if (cipher == null) {
            cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, getKey());  // Initialize cipher in ENCRYPT_MODE
        }
        return cipher;
    }

    // Encrypt a string using AES
    public byte[] encryptString(String input) throws Exception {
        // Get the Cipher for encryption
        Cipher cipher = getCipher();

        // Generate a random IV (Initialization Vector)
        byte[] iv = cipher.getIV();

        // Encrypt the string
        byte[] encryptionResult = cipher.doFinal(input.getBytes(StandardCharsets.UTF_8));

        // Combine IV and ciphertext in one array (to send them together)
        byte[] combined = new byte[iv.length + encryptionResult.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(encryptionResult, 0, combined, iv.length, encryptionResult.length);

        return combined;
    }

    // Convert byte array to Base64 (to store or pass around in a string format)
    public String byteArrayToBase64(byte[] input) {
        return Base64.encodeToString(input, Base64.DEFAULT);
    }

    // Example usage of encrypting two strings and using them in a BiometricPrompt.CryptoObject
    public void performBiometricAuthentication(BiometricPrompt biometricPrompt) {
        try {
            String string1 = "First string";
            String string2 = "Second string";

            // Encrypt both strings
            byte[] encryptedString1 = encryptString(string1);
            byte[] encryptedString2 = encryptString(string2);

            // Convert to Base64 (if you want to store or pass as a string)
            String base64String1 = byteArrayToBase64(encryptedString1);
            String base64String2 = byteArrayToBase64(encryptedString2);

            // Log or pass these Base64 strings as needed
            System.out.println("Encrypted String 1: " + base64String1);
            System.out.println("Encrypted String 2: " + base64String2);

            // Create a BiometricPrompt.CryptoObject with a Cipher object
            Cipher cipher = getCipher();  // Get the Cipher initialized for encryption

            // Create the CryptoObject
            BiometricPrompt.CryptoObject cryptoObject = new BiometricPrompt.CryptoObject(cipher);

            // Create the BiometricPrompt.PromptInfo for UI
            BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                    .setTitle("Biometric Authentication")
                    .setSubtitle("Use your fingerprint to authenticate")
                    .setNegativeButtonText("Cancel")
                    .build();

            // Now use the cryptoObject and promptInfo with BiometricPrompt to authenticate
            biometricPrompt.authenticate(promptInfo, cryptoObject);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Decrypt the string after successful authentication
    public String decryptString(BiometricPrompt.CryptoObject cryptoObject, byte[] encryptedData) throws Exception {
        // Get the Cipher from the CryptoObject
        Cipher cipher = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            cipher = cryptoObject.getCipher();
        }

        if (cipher == null) {
            throw new Exception("Cipher is null");
        }

        // Separate the IV and encrypted data (the first 12 bytes are the IV for AES-GCM)
        byte[] iv = new byte[12];  // GCM requires 12-byte IVs
        byte[] ciphertext = new byte[encryptedData.length - iv.length];

        System.arraycopy(encryptedData, 0, iv, 0, iv.length);
        System.arraycopy(encryptedData, iv.length, ciphertext, 0, ciphertext.length);

        // Initialize the cipher for decryption with the IV
        GCMParameterSpec spec = new GCMParameterSpec(128, iv);  // 128-bit tag size
        cipher.init(Cipher.DECRYPT_MODE, getKey(), spec);

        // Decrypt the data
        byte[] decryptedData = cipher.doFinal(ciphertext);

        // Convert to string
        return new String(decryptedData, StandardCharsets.UTF_8);
    }

    // Example method to get encrypted data (replace with your actual data retrieval logic)
    private byte[] getEncryptedData(String stringName) {
        // Retrieve the encrypted string for 'stringName' (could be from storage, database, etc.)
        // In this case, we're just returning a sample encrypted byte array.
        return new byte[0];  // Replace with actual encrypted byte array
    }
}
