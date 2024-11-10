package com.example.multipleaccountsproject;

import android.os.Bundle;
import android.util.Base64;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;

import java.util.concurrent.Executor;

public class ActivityB extends AppCompatActivity {


    private EditText editTextB;
    private Button encryptButtonB, decryptButtonB;
    TextView encryptedDataB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_b);

        editTextB = findViewById(R.id.editTextB);
        encryptButtonB = findViewById(R.id.encryptButtonB);
        decryptButtonB = findViewById(R.id.decryptButtonB);
        encryptedDataB = findViewById(R.id.encryptedDataB);

        encryptButtonB.setOnClickListener(v -> encryptAndStoreData());
        decryptButtonB.setOnClickListener(v -> authenticateAndDecrypt());


        String masterKeyAlias = null;
        try {
            masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);

            EncryptedSharedPreferences sharedPreferences = (EncryptedSharedPreferences) EncryptedSharedPreferences.create(
                    "biometric_data_activity_B",            // File name to store the encrypted data
                    masterKeyAlias,              // Master key alias to encrypt the data
                    ActivityB.this,              // Context
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM // You don't need these flags unless your app needs compatibility issues
            );

            String encryptedStringBase64 = sharedPreferences.getString("encrypted_string_B", null);
            encryptedDataB.setText(encryptedStringBase64);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // Method to generate or retrieve the secret key from Keystore

    // Encrypt and store the data
    private void encryptAndStoreData() {
        String input = editTextB.getText().toString();
        try {
            byte[] encryptedInput = BiometricUtil.encryptString(input);
            String base64EncryptedInput = BiometricUtil.byteArrayToBase64(encryptedInput);

            String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
            EncryptedSharedPreferences sharedPreferences = (EncryptedSharedPreferences) EncryptedSharedPreferences.create(
                    "biometric_data_activity_B",
                    masterKeyAlias,
                    ActivityB.this,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM // You don't need these flags unless your app needs compatibility issues
            );

            sharedPreferences.edit()
                    .putString("encrypted_string_B", base64EncryptedInput)
                    .apply();

            Toast.makeText(this, "Data encrypted and saved", Toast.LENGTH_SHORT).show();

            encryptedDataB.setText(base64EncryptedInput);

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
                Toast.makeText(ActivityB.this, "Authentication Error: " + errString, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Toast.makeText(ActivityB.this, "Authentication Failed", Toast.LENGTH_SHORT).show();
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
            EncryptedSharedPreferences sharedPreferences = (EncryptedSharedPreferences) EncryptedSharedPreferences.create(
                    "biometric_data_activity_B",            // File name to store the encrypted data
                    masterKeyAlias,              // Master key alias to encrypt the data
                    ActivityB.this,              // Context
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM // You don't need these flags unless your app needs compatibility issues
            );

            String encryptedStringBase64 = sharedPreferences.getString("encrypted_string_B", null);

            if (encryptedStringBase64 == null) {
                Toast.makeText(this, "No encrypted data found", Toast.LENGTH_SHORT).show();
                return;
            }

            byte[] encryptedString = Base64.decode(encryptedStringBase64, Base64.DEFAULT);

            byte[] iv = new byte[12];
            byte[] ciphertext = new byte[encryptedString.length - iv.length];

            System.arraycopy(encryptedString, 0, iv, 0, iv.length);
            System.arraycopy(encryptedString, iv.length, ciphertext, 0, ciphertext.length);

            Cipher cipher = Cipher.getInstance(BiometricUtil.TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, BiometricUtil.getKey(), new GCMParameterSpec(128, iv));

            byte[] decryptedData = cipher.doFinal(ciphertext);
            String decryptedString = new String(decryptedData, StandardCharsets.UTF_8);

            editTextB.setText(decryptedString);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Decryption failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
