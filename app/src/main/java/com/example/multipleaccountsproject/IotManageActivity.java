/*
package com.example.multipleaccountsproject;

import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import java.util.concurrent.Executor;

import javax.crypto.Cipher;

public class IotManageActivity extends AppCompatActivity {

    private BiometricCryptoExample biometricCryptoExample;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_iot_manage);

        EditText editText1 = findViewById(R.id.editText1);
        EditText editText2 = findViewById(R.id.editText2);
        Button encryptButton = findViewById(R.id.encryptButton);
        Button decryptButton = findViewById(R.id.decryptButton);

        // Set up listeners for encryption and decryption buttons
        encryptButton.setOnClickListener(v -> encryptAndSaveData());
        decryptButton.setOnClickListener(v -> decryptData());

        biometricCryptoExample = new BiometricCryptoExample();
    }

    private void startBiometricAuthentication() {
        // Create the executor for the BiometricPrompt
        Executor executor = ContextCompat.getMainExecutor(this);

        // Create a BiometricPrompt instance
        BiometricPrompt biometricPrompt = new BiometricPrompt(this,
                executor, new BiometricPrompt.AuthenticationCallback() {

            @Override
            public void onAuthenticationError(int errorCode, CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Toast.makeText(IotManageActivity.this, "Authentication error: " + errString, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);

                // You can now use the `BiometricPrompt.CryptoObject` to decrypt the data
                BiometricPrompt.CryptoObject cryptoObject = result.getCryptoObject();
                if (cryptoObject != null) {
                    // Successfully authenticated, use the `CryptoObject` for decryption
                    // Decrypt your data here
                    decryptData(cryptoObject);
                }
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Toast.makeText(IotManageActivity.this, "Authentication failed", Toast.LENGTH_SHORT).show();
            }
        });

        // Perform biometric authentication
        biometricCryptoExample.performBiometricAuthentication(biometricPrompt);
    }

    private void decryptData(BiometricPrompt.CryptoObject cryptoObject) {
        try {
            // Assuming you have saved encrypted data somewhere
            byte[] encryptedData = getEncryptedData();  // Retrieve the encrypted data

            // Decrypt using the cipher from the CryptoObject
            String decryptedString = biometricCryptoExample.decryptString(cryptoObject, encryptedData);
            Toast.makeText(this, "Decrypted Data: " + decryptedString, Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Decryption failed", Toast.LENGTH_SHORT).show();
        }
    }

    private byte[] getEncryptedData() {
        // For simplicity, just returning an example encrypted byte array
        // In real use, you would fetch the actual encrypted data
        return new byte[0];
    }
}
*/
