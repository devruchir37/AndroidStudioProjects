package com.example.multipleaccountsproject.AutoZoomOld;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.Image;
import android.os.Bundle;
import android.util.Size;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.multipleaccountsproject.R;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.Barcode;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.common.InputImage;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AutoZoomOld extends AppCompatActivity implements SensorEventListener {

    private PreviewView previewView;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private ExecutorService cameraExecutor;
    private Camera camera;
    private SensorManager sensorManager;
    private Sensor lightSensor;
    private boolean isTorchOn = false;
    private boolean isZoomedIn = false;

    private static final int CAMERA_PERMISSION_REQUEST_CODE = 1001;
    private static final float LIGHT_THRESHOLD = 10.0f; // Example threshold for low light
    private static final float ZOOM_FACTOR = 0.5f; // Example zoom factor

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auto_zoom_old);

        previewView = findViewById(R.id.previewView);
        Button torchButton = findViewById(R.id.torchButton);
        cameraExecutor = Executors.newSingleThreadExecutor();
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

        if (lightSensor == null) {
            Toast.makeText(this, "No light sensor found!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_REQUEST_CODE);
        }

        torchButton.setOnClickListener(v -> {
            if (camera != null) {
                /*isTorchOn = !camera.getCameraInfo().isTorchOn();
                camera.getCameraControl().enableTorch(isTorchOn);*/
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    private void startCamera() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder().build();
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setTargetResolution(new Size(1280, 720))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();

        imageAnalysis.setAnalyzer(cameraExecutor, new ImageAnalysis.Analyzer() {
            @Override
            public void analyze(@NonNull ImageProxy imageProxy) {
                processImageProxy(imageProxy);
            }
        });

        camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);
    }

    private void setZoom(float zoomFactor) {
        if (camera != null) {
            camera.getCameraControl().setLinearZoom(zoomFactor);
        }
    }

    private void processImageProxy(ImageProxy imageProxy) {
        @androidx.camera.core.ExperimentalGetImage
        Image mediaImage = imageProxy.getImage();
        if (mediaImage != null) {
            InputImage image = InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());
            BarcodeScanning.getClient().process(image)
                    .addOnSuccessListener(barcodes -> {
                        if (barcodes.size() > 0) {
                            // QR code detected, zoom in
                            if (!isZoomedIn) {
                                setZoom(ZOOM_FACTOR);
                                isZoomedIn = true;

                                for (Barcode barcode : barcodes) {
                                    // Get the raw value of the QR code
                                    String rawValue = barcode.getRawValue();
                                    // Do something with the QR code value
                                    Toast.makeText(AutoZoomOld.this, "QR Code Detected: " + rawValue, Toast.LENGTH_SHORT).show();
                                }


                            }
                        } else {
                            // No QR code detected, zoom out
                            if (isZoomedIn) {
                                setZoom(0.0f);
                                isZoomedIn = false;
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        // Handle the error
                    })
                    .addOnCompleteListener(task -> imageProxy.close());
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
            float ambientLight = event.values[0];
            if (ambientLight < LIGHT_THRESHOLD && !isTorchOn) {
                if (camera != null) {
                    camera.getCameraControl().enableTorch(true);
                    isTorchOn = true;
                }
            } else if (ambientLight >= LIGHT_THRESHOLD && isTorchOn) {
                if (camera != null) {
                    camera.getCameraControl().enableTorch(false);
                    isTorchOn = false;
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do nothing for now
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                // Permission denied. Handle the error
                Toast.makeText(this, "Camera permission is required", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
    }
}