package com.example.multipleaccountsproject;

import android.content.Intent;
import android.os.Bundle;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.core.content.IntentCompat;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.util.concurrent.Executor;

public class ActivityA extends AppCompatActivity {

    private static final String KEY_ALIAS = "myKeyAlias";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private EditText editTextA;
    private Button encryptButtonA, decryptButtonA,nextActivity;
    TextView encryptedData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_a);

        editTextA = findViewById(R.id.editTextA);
        encryptButtonA = findViewById(R.id.encryptButtonA);
        decryptButtonA = findViewById(R.id.decryptButtonA);
        nextActivity = findViewById(R.id.nextActivity);
        encryptedData = findViewById(R.id.encryptedData);

        encryptButtonA.setOnClickListener(v -> encryptAndStoreData());
        decryptButtonA.setOnClickListener(v -> authenticateAndDecrypt());

        nextActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ActivityA.this,ActivityB.class);
                startActivity(intent);
            }
        });


        String masterKeyAlias = null;
        try {
            masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);

        // Create EncryptedSharedPreferences instance
        EncryptedSharedPreferences sharedPreferences = (EncryptedSharedPreferences) EncryptedSharedPreferences.create(
                "biometric_data_activity_A",            // File name to store the encrypted data
                masterKeyAlias,              // Master key alias to encrypt the data
                ActivityA.this,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM);

        String encryptedStringBase64 = sharedPreferences.getString("encrypted_string_A", null);

        if (encryptedStringBase64 == null) {
            Toast.makeText(this, "No encrypted data found", Toast.LENGTH_SHORT).show();
            return;
        }

        byte[] encryptedString = Base64.decode(encryptedStringBase64, Base64.DEFAULT);
        encryptedData.setText(encryptedStringBase64);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }

    // Method to generate or retrieve the secret key from Keystore
    private SecretKey getKey() throws Exception {
        KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);

        SecretKey key = (SecretKey) keyStore.getKey(KEY_ALIAS, null);
        if (key == null) {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES", "AndroidKeyStore");
            keyGenerator.init(
                    new KeyGenParameterSpec.Builder(KEY_ALIAS, KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                            .build());
            key = keyGenerator.generateKey();
        }
        return key;
    }

    // Encrypt the string with AES/GCM
    private byte[] encryptString(String input) throws Exception {
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, getKey());

        byte[] iv = cipher.getIV();
        byte[] encryptedData = cipher.doFinal(input.getBytes(StandardCharsets.UTF_8));

        byte[] combined = new byte[iv.length + encryptedData.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(encryptedData, 0, combined, iv.length, encryptedData.length);

        return combined;
    }

    // Convert byte array to Base64
    private String byteArrayToBase64(byte[] input) {
        return Base64.encodeToString(input, Base64.DEFAULT);
    }

    // Encrypt and store the data
    private void encryptAndStoreData() {
        String input = editTextA.getText().toString();
        try {
            byte[] encryptedInput = encryptString(input);
            String base64EncryptedInput = byteArrayToBase64(encryptedInput);

            // Create master key for encryption
            String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);

            // Correctly create EncryptedSharedPreferences instance
            EncryptedSharedPreferences sharedPreferences = (EncryptedSharedPreferences) EncryptedSharedPreferences.create(
                    "biometric_data_activity_A",            // File name to store the encrypted data
                    masterKeyAlias,              // Master key alias to encrypt the data
                    ActivityA.this,              // Context
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM // You don't need these flags unless your app needs compatibility issues
            );

            // Store the encrypted string
            sharedPreferences.edit()
                    .putString("encrypted_string_A", base64EncryptedInput)
                    .apply();

            Toast.makeText(this, "Data encrypted and saved", Toast.LENGTH_SHORT).show();

            encryptedData.setText(base64EncryptedInput);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Encryption failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // Authenticate and decrypt the data
    private void authenticateAndDecrypt() {
        Executor executor = ContextCompat.getMainExecutor(this);
        BiometricPrompt biometricPrompt = new BiometricPrompt(this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                decryptData();
            }

            @Override
            public void onAuthenticationError(int errorCode, CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Toast.makeText(ActivityA.this, "Authentication Error: " + errString, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Toast.makeText(ActivityA.this, "Authentication Failed", Toast.LENGTH_SHORT).show();
            }
        });

        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Biometric Authentication")
                .setSubtitle("Please authenticate to decrypt the data")
                .setNegativeButtonText("Cancel")
                .build();

        biometricPrompt.authenticate(promptInfo);
    }

    // Decrypt the data
    private void decryptData() {
        try {
            String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);

            // Create EncryptedSharedPreferences instance
            EncryptedSharedPreferences sharedPreferences = (EncryptedSharedPreferences) EncryptedSharedPreferences.create(
                    "biometric_data_activity_A",            // File name to store the encrypted data
                    masterKeyAlias,              // Master key alias to encrypt the data
                    ActivityA.this,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM);

            String encryptedStringBase64 = sharedPreferences.getString("encrypted_string_A", null);

            if (encryptedStringBase64 == null) {
                Toast.makeText(this, "No encrypted data found", Toast.LENGTH_SHORT).show();
                return;
            }

            byte[] encryptedString = Base64.decode(encryptedStringBase64, Base64.DEFAULT);

            byte[] iv = new byte[12];
            byte[] ciphertext = new byte[encryptedString.length - iv.length];

            System.arraycopy(encryptedString, 0, iv, 0, iv.length);
            System.arraycopy(encryptedString, iv.length, ciphertext, 0, ciphertext.length);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, getKey(), new GCMParameterSpec(128, iv));

            byte[] decryptedData = cipher.doFinal(ciphertext);
            String decryptedString = new String(decryptedData, StandardCharsets.UTF_8);

            editTextA.setText(decryptedString);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Decryption failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
