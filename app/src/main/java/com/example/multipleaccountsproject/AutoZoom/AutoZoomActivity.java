package com.example.multipleaccountsproject.AutoZoom;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraControl;
import androidx.camera.core.CameraInfo;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import com.example.multipleaccountsproject.R;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.Barcode;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.common.InputImage;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AutoZoomActivity extends AppCompatActivity {
    private ExecutorService cameraExecutor;
    private BarcodeScanner barcodeScanner;
    private Camera camera;
    private PreviewView previewView;
    private ImageAnalysis imageAnalysis;

    private final ActivityResultLauncher<String[]> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                Boolean cameraGranted = result.getOrDefault(Manifest.permission.CAMERA, false);
                if (cameraGranted != null && cameraGranted) {
                    startCamera();
                } else {
                    // Handle permission denial
                    Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auto_zoom);

        previewView = findViewById(R.id.previewView);
        cameraExecutor = Executors.newSingleThreadExecutor();
        barcodeScanner = BarcodeScanning.getClient();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions();
        } else {
            startCamera();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requestPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(new String[]{
                    Manifest.permission.CAMERA
            });
        } else {
            startCamera();
        }
    }

    private void startCamera() {
        final ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                // Handle any errors (including cancellation) here.
                Log.e("AutoZoomActivity", "Error starting camera", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder().build();
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        imageAnalysis = new ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();

        imageAnalysis.setAnalyzer(cameraExecutor, imageProxy -> processImageProxy(imageProxy));

        camera = cameraProvider.bindToLifecycle(this, cameraSelector, imageAnalysis, preview);
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        CameraControl cameraControl = camera.getCameraControl();
        CameraInfo cameraInfo = camera.getCameraInfo();

        // Log supported zoom ratios
        float minZoom = cameraInfo.getZoomState().getValue().getMinZoomRatio();
        float maxZoom = cameraInfo.getZoomState().getValue().getMaxZoomRatio();
        Log.d("AutoZoomActivity", "Supported zoom ratios: min=" + minZoom + ", max=" + maxZoom);

        // Monitor ambient light conditions and toggle flashlight
        monitorAmbientLight(cameraControl);
    }

    private void processImageProxy(ImageProxy imageProxy) {
        @SuppressLint("UnsafeExperimentalUsageError")
        Image mediaImage = imageProxy.getImage();
        if (mediaImage != null) {
            InputImage image = InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());
            barcodeScanner.process(image)
                    .addOnSuccessListener(barcodes -> {
                        for (Barcode barcode : barcodes) {
                            // Handle successful barcode detection here
                            String rawValue = barcode.getRawValue();
                            if (rawValue != null) {
                                showDialog(rawValue);
                                // Stop analyzing further images
                                stopImageAnalysis();
                            }

                            Rect boundingBox = barcode.getBoundingBox();
                            if (boundingBox != null) {
                                int width = boundingBox.width();
                                int height = boundingBox.height();
                                int size = Math.max(width, height);
                                Log.d("AutoZoomActivity", "Barcode size: " + size);
                                adjustZoom(size);
                            }
                        }
                    })
                    .addOnCompleteListener(task -> imageProxy.close());
        }
    }

    private void showDialog(String qrValue) {
        runOnUiThread(() -> new AlertDialog.Builder(AutoZoomActivity.this)
                .setTitle("QR Code Detected")
                .setMessage(qrValue)
                .setPositiveButton("OK", (dialog, which) -> {
                    dialog.dismiss();
                    // Resume analyzing images after closing the dialog
                    resumeImageAnalysis();
                })
                .show());
    }

    private void stopImageAnalysis() {
        if (imageAnalysis != null) {
            imageAnalysis.clearAnalyzer();
        }
    }

    private void resumeImageAnalysis() {
        if (imageAnalysis != null) {
            imageAnalysis.setAnalyzer(cameraExecutor, imageProxy -> processImageProxy(imageProxy));
        }
    }

    private void adjustZoom(int barcodeSize) {
        float zoomLevel = calculateZoomLevel(barcodeSize);
        Log.d("AutoZoomActivity", "Setting zoom level: " + zoomLevel);
        CameraControl cameraControl = camera.getCameraControl();
       /* cameraControl.setZoomRatio(zoomLevel)
                .addOnSuccessListener(() -> Log.d("AutoZoomActivity", "Zoom level set: " + zoomLevel))
                .addOnFailureListener(e -> Log.e("AutoZoomActivity", "Failed to set zoom level", e));*/
    }

    private float calculateZoomLevel(int barcodeSize) {
        // Define your zoom level calculation logic
        if (barcodeSize > 500) {
            return 1.0f; // No zoom
        } else if (barcodeSize > 200) {
            return 2.0f; // Moderate zoom
        } else {
            return 4.0f; // Maximum zoom
        }
    }

    private void monitorAmbientLight(CameraControl cameraControl) {
        final float[] lightLevel = new float[1];
        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        Sensor lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        SensorEventListener lightEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                lightLevel[0] = event.values[0];
                Log.d("AutoZoomActivity", "Ambient light level: " + lightLevel[0]);
                if (lightLevel[0] < 40) {  // Adjusted threshold value
                    // Turn on flashlight
                    cameraControl.enableTorch(true);
                } else {
                    // Turn off flashlight
                    cameraControl.enableTorch(false);
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }
        };

        sensorManager.registerListener(lightEventListener, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
    }
}
