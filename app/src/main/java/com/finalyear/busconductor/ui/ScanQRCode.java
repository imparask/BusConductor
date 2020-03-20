package com.finalyear.busconductor.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.finalyear.busconductor.R;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.firebase.auth.FirebaseAuth;

import java.io.IOException;

import static com.finalyear.busconductor.ui.StartActivity.exitConstant;

public class ScanQRCode extends AppCompatActivity {


    private static final String TAG = "ScanQRCode";
    private SurfaceView mQRscanner;
    private CameraSource mCameraSource;
    private BarcodeDetector mBarcodeDetector;
    private static final int MY_PERMISSIONS_REQUEST_CAMERA = 9004 ;
    private boolean mCameraPermission = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_qrcode);

        mQRscanner = findViewById(R.id.sv_QRscanner);

        /*
        String data = "bus2";
        Intent intent = new Intent(getApplicationContext(),BusInfo.class);

        intent.putExtra("countID",data);
        startActivity(intent);

         */

        mBarcodeDetector = new BarcodeDetector.Builder(getApplicationContext()).setBarcodeFormats(Barcode.QR_CODE).build();

        mCameraSource = new CameraSource.Builder(this, mBarcodeDetector).setRequestedPreviewSize(640, 480).build();

        mQRscanner.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {

                try {
                    mCameraSource.start(holder);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                mCameraSource.stop();
            }
        });

        mBarcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {

            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                SparseArray<Barcode> qrCode = detections.getDetectedItems();

                if (qrCode.size() != 0) {
                    int vibratorDuration = 1000;
                    Vibrator vib = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
                    vib.vibrate(vibratorDuration);

                    String data = qrCode.valueAt(0).displayValue;
                    Intent intent = new Intent(getApplicationContext(), BusInfo.class);

                    intent.putExtra("countID", data);
                    startActivity(intent);
                }
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        if(exitConstant !=100){
            startActivity(new Intent(getApplicationContext(),StartActivity.class));
        }
        else if (!mCameraPermission) {
            getCameraPermission();
        }
    }


    private void getCameraPermission(){

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), android.Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            mCameraPermission = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    MY_PERMISSIONS_REQUEST_CAMERA);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mCameraPermission = false;
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CAMERA: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mCameraPermission = true;
                    recreate();
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: called.");
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CAMERA: {
                if (!mCameraPermission) {
                    getCameraPermission();
                }
                else {
                    recreate();
                }
            }
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if(item.getItemId() == R.id.logout){
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(getApplicationContext(), Login.class));
            finish();
        }
        return true;
    }
}
