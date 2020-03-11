package com.finalyear.busconductor.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.finalyear.busconductor.R;
import com.finalyear.busconductor.services.UserClient;
import com.finalyear.busconductor.model.Conductor;
import com.finalyear.busconductor.model.ConductorLocationBus;
import com.finalyear.busconductor.services.LocationService;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import static com.finalyear.busconductor.ui.StartActivity.exitConstant;

public class LocationTracker extends AppCompatActivity {


    private static final String TAG = "LocationTracker";
    private TextView mCountNumber,mLatitude,mLongitude,mBusNumber, mBusSource,mBusDest;
    private ProgressBar progressBar_Count, progressBar_Lat , progressBar_Long, progressBar_busNum,progressBar_busSource, progressBar_busDest;
    private Button mStopBus;
    private DatabaseReference countdatabase;
    private FirebaseAuth fAuth;
    private FirebaseFirestore fStore;
    private Conductor conductor;
    public static int stopServiceFlag;
    private Handler mHandler = new Handler();
    private Runnable mRunnable;
    private static final int LOCATION_UPDATE_INTERVAL = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_tracker);

        stopServiceFlag = 900;
        mCountNumber = findViewById(R.id.tv_DisplayCount);
        mLatitude = findViewById(R.id.tv_LatitudeCoordinates);
        mLongitude = findViewById(R.id.tv_LongitudeCoordinates);
        mBusNumber = findViewById(R.id.tv_DisplayBusNum);
        mBusDest = findViewById(R.id.tv_DisplayBusDest);
        mBusSource = findViewById(R.id.tv_DisplayBusSource);
        mStopBus = findViewById(R.id.bt_stopBus);
        progressBar_Count = findViewById(R.id.pgB_count);
        progressBar_Lat = findViewById(R.id.pgB_latitude);
        progressBar_Long = findViewById(R.id.pgB_longitude);
        progressBar_busNum = findViewById(R.id.pgB_BusNumber);
        progressBar_busSource = findViewById(R.id.pgB_BusSource);
        progressBar_busDest = findViewById(R.id.pgB_BusDestination);

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        countdatabase = FirebaseDatabase.getInstance().getReference();


        conductor = ((UserClient)getApplicationContext()).getConductor();

        startLocationService();
        getConductorDetails();

        mStopBus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopLocationUpdates();
                fStore.collection("Conductor Bus Location")
                        .document(conductor.getRef_no())
                        .delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        stopServiceFlag=999;
                        Toast.makeText(getApplicationContext(), "Bus Stopped", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getApplicationContext(),MainActivity.class));
                        finish();
                    }
                });

            }
        });
    }

    private void getConductorDetails() {
        //final Conductor conductor = ((UserClient)getApplicationContext()).getConductor();
        fStore.collection("Conductor Bus Location")
                .document(conductor.getRef_no())
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                ConductorLocationBus conductorLocationBus = task.getResult().toObject(ConductorLocationBus.class);
                String busCountID = conductorLocationBus.getBusCount();
                Log.d(TAG,"Conductor Location Bus "+conductorLocationBus);
                Log.d(TAG,"Bus Count ID : "+busCountID);
                getCount(busCountID);
            }
        });
    }

    private void getCount(String busCountID){
        Log.d(TAG,"Bus Count id : "+busCountID);
        countdatabase = countdatabase.child(busCountID);
        countdatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG,""+dataSnapshot.getValue().toString());
                for(DataSnapshot data:dataSnapshot.getChildren()){
                    String countCurrent = data.getValue().toString();
                    mCountNumber.setText(countCurrent);
                    progressBar_Count.setVisibility(View.INVISIBLE);
                    Log.d(TAG,"Count = "+countCurrent);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(exitConstant !=100){
            startActivity(new Intent(getApplicationContext(),StartActivity.class));
        }
        startUserLocationsRunnable();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }


    private void startLocationService(){
        if(!isLocationServiceRunning()){
            Intent serviceIntent = new Intent(this, LocationService.class);
            //        this.startService(serviceIntent);
            if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.O){

                LocationTracker.this.startForegroundService(serviceIntent);
            }else{
                startService(serviceIntent);
            }
        }
    }

    private boolean isLocationServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        //noinspection deprecation
        for (ActivityManager.RunningServiceInfo service :manager.getRunningServices(Integer.MAX_VALUE)) {
            if ("com.finalyear.busconductor.services.LocationService".equals(service.service.getClassName())) {
                Log.d(TAG, "isLocationServiceRunning: location service is already running.");
                return true;
            }
        }
        Log.d(TAG, "isLocationServiceRunning: location service is not running.");
        return false;
    }

    private void startUserLocationsRunnable(){
        Log.d(TAG, "startUserLocationsRunnable: starting runnable for retrieving updated locations.");
        mHandler.postDelayed(mRunnable = new Runnable() {
            @Override
            public void run() {
                retrieveUserLocation();
                mHandler.postDelayed(mRunnable, LOCATION_UPDATE_INTERVAL);
            }
        }, LOCATION_UPDATE_INTERVAL);
    }

    private void stopLocationUpdates(){
        mHandler.removeCallbacks(mRunnable);
    }

    private void retrieveUserLocation(){
        Log.d(TAG, "retrieveUserLocations: called");
        DocumentReference userLocationRef= fStore.collection("Conductor Bus Location").document(conductor.getRef_no());

            if (stopServiceFlag != 999) {
                userLocationRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        try{
                            if (task.isSuccessful()) {
                                Log.d(TAG, "onComplete : sucessfully got the bus location details");
                                ConductorLocationBus conductorLocationBus = task.getResult().toObject(ConductorLocationBus.class);
                                double latitude = conductorLocationBus.getGeoPoint().getLatitude();
                                double longitude = conductorLocationBus.getGeoPoint().getLongitude();
                                String busNumber = conductorLocationBus.getBusNumber();
                                String busSource = conductorLocationBus.getBusSource();
                                String busDestination = conductorLocationBus.getBusDestination();
                                Conductor con = conductorLocationBus.getConductor();
                                String conductorName = con.getName();
                                String conductorRefNum = con.getRef_no();
                                mBusNumber.setText(busNumber);
                                progressBar_busNum.setVisibility(View.INVISIBLE);
                                mBusSource.setText(busSource);
                                progressBar_busSource.setVisibility(View.INVISIBLE);
                                mBusDest.setText(busDestination);
                                progressBar_busDest.setVisibility(View.INVISIBLE);
                                mLatitude.setText("" + latitude);
                                progressBar_Lat.setVisibility(View.INVISIBLE);
                                mLongitude.setText("" + longitude);
                                progressBar_Long.setVisibility(View.INVISIBLE);
                                setTitle("Welcome "+conductorName.toUpperCase()+" ("+conductorRefNum+")");
                            }
                        }
                        catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                });
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
            stopLocationUpdates();
            fStore.collection("Conductor Bus Location")
                    .document(conductor.getRef_no())
                    .delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    stopServiceFlag = 999;
                    Toast.makeText(getApplicationContext(), "Bus Stopped", Toast.LENGTH_SHORT).show();
                    FirebaseAuth.getInstance().signOut();
                    startActivity(new Intent(getApplicationContext(), Login.class));
                    finish();
                }
            });
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        stopLocationUpdates();
        fStore.collection("Conductor Bus Location")
                .document(conductor.getRef_no())
                .delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                stopServiceFlag = 999;
                Toast.makeText(getApplicationContext(), "Bus Stopped", Toast.LENGTH_SHORT).show();
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(getApplicationContext(), Login.class));
                finish();
            }
        });
        startActivity(new Intent(getApplicationContext(),ScanQRCode.class));
        finish();
    }
}
