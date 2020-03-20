package com.finalyear.busconductor.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.finalyear.busconductor.R;
import com.finalyear.busconductor.services.UserClient;
import com.finalyear.busconductor.model.Bus;
import com.finalyear.busconductor.model.Conductor;
import com.finalyear.busconductor.model.ConductorLocationBus;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.finalyear.busconductor.ui.StartActivity.exitConstant;


public class BusInfo extends AppCompatActivity {

    private static final String TAG = "BusInfo";
    private ProgressBar progressBar;
    private AutoCompleteTextView mBusNumber,mBusSource,mBusDestination;
    private ImageView mNumberDropdown,mSourceDropdown,mDestDropdown;
    private TextView mCountID;
    private FirebaseAuth fAuth;
    private FirebaseFirestore fStore;
    private Button mSelectedBus;
    private ConductorLocationBus mConductorLocationBus;

    private Bus bus;
    private String countID;
    private FusedLocationProviderClient mFusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bus_info);

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        mSelectedBus = findViewById(R.id.bt_selectBus);
        mCountID = findViewById(R.id.tv_countID);
        progressBar = findViewById(R.id.pgb_BusInfo);
        mBusNumber = findViewById(R.id.actv_busNumber);
        mNumberDropdown = findViewById(R.id.imgV_Numberdropdown);
        mBusSource = findViewById(R.id.actv_busSource);
        mSourceDropdown = findViewById(R.id.imgV_Sourcedropdown);
        mBusDestination = findViewById(R.id.actv_busDest);
        mDestDropdown = findViewById(R.id.imgV_Destdropdown);

        bus = new Bus();

        Intent intent = getIntent();
        countID = intent.getStringExtra("countID");

        mCountID.setText(countID);

        BusNumberList();

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

    }

    @Override
    protected void onResume() {
        super.onResume();

        if(exitConstant !=100){
            startActivity(new Intent(getApplicationContext(),StartActivity.class));
        }
        mSelectedBus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getConductor();
                getLastKnownLocation();
                boolean status = getBusInformation();
                if(status) {
                    saveConductorBusInfo();
                    Toast.makeText(BusInfo.this, "Bus Details Saved", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(getApplicationContext(), LocationTracker.class));
                }
                else{
                    Toast.makeText(BusInfo.this, "Source and Destination cannot be same", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void getConductor(){
        if(mConductorLocationBus == null){
            mConductorLocationBus = new ConductorLocationBus();
            Conductor conductor = ((UserClient)getApplicationContext()).getConductor();
            mConductorLocationBus.setConductor(conductor);

        }
    }

    private void saveConductorBusInfo(){
        if(mConductorLocationBus != null){
            Conductor conductor = ((UserClient)getApplicationContext()).getConductor();
            DocumentReference conductorBusRef = fStore.collection("Conductor Bus Location").document(conductor.getRef_no());
            conductorBusRef.set(mConductorLocationBus).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        Log.d(TAG,"onComplete: Conductor Bus Location saved in database.\n Latitude - "+mConductorLocationBus.getGeoPoint().getLatitude()
                                +" Longitude - "+mConductorLocationBus.getGeoPoint().getLongitude());
                    }
                }
            });
        }
    }

    private boolean getBusInformation(){

        boolean status = false;
        Log.d(TAG,"Bus : "+bus);
        String bus_number = bus.getBusNumber();
        String bus_source = bus.getBusSource();
        String bus_desti = bus.getBusDestination();
        if(!(bus_source.equals(bus_desti))){
            status = true;
            mConductorLocationBus.setBusCount(countID);
            mConductorLocationBus.setBusNumber(bus_number);
            mConductorLocationBus.setBusDestination(bus_desti);
            mConductorLocationBus.setBusSource(bus_source);
        }

        return status;
    }


    private void getLastKnownLocation(){
        Log.d(TAG,"getLastKnownLocation: called.");
        mFusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if(task.isSuccessful()){
                    Location location = task.getResult();
                    if(location != null) {
                        GeoPoint geopoint = new GeoPoint(location.getLatitude(), location.getLongitude());
                        Log.d(TAG, "onComplete: Latitude - " + geopoint.getLatitude());
                        Log.d(TAG, "onComplete: Longitude - " + geopoint.getLongitude());
                        mConductorLocationBus.setGeoPoint(geopoint);
                        mConductorLocationBus.setTimestamp(null);
                    }
                }
            }
        });
    }

    private void BusStopList(final String busNo){

        progressBar.setVisibility(View.VISIBLE);

        final List<String> busdestiations= new ArrayList<>();
        final ArrayAdapter<String> adapter1 = new ArrayAdapter<>(getApplicationContext(),android.R.layout.simple_list_item_1,busdestiations);
        adapter1.setDropDownViewResource(android.R.layout.simple_list_item_1);
        mBusSource.setAdapter(adapter1);
        mBusSource.setThreshold(1);

        final List<String> bussources= new ArrayList<>();
        final ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(getApplicationContext(),android.R.layout.simple_list_item_1,bussources);
        adapter2.setDropDownViewResource(android.R.layout.simple_list_item_1);
        mBusDestination.setAdapter(adapter2);
        mBusDestination.setThreshold(1);

        fStore.collection("BusRoute").document(busNo).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    if(task.getResult() != null) {
                        Map<String,Object> data = task.getResult().getData();

                        if(data != null) {
                            for (int i = 1; i <= data.size(); i++) {
                                String stop = data.get("busStop" + i).toString();
                                busdestiations.add(stop);
                                bussources.add(stop);
                            }
                        }
                        else{
                            Toast.makeText(getApplicationContext(),"Route information not available for "+busNo,Toast.LENGTH_LONG).show();
                        }
                    }
                    adapter1.notifyDataSetChanged();
                    adapter2.notifyDataSetChanged();
                    progressBar.setVisibility(View.INVISIBLE);
                }
            }
        });


        mSourceDropdown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBusSource.showDropDown();
            }
        });


        mDestDropdown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBusDestination.showDropDown();
            }
        });

        mBusDestination.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String busDestination = mBusDestination.getText().toString();
                bus.setBusDestination(busDestination);
            }
        });

        mBusSource.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String busSource = mBusSource.getText().toString();
                bus.setBusSource(busSource);
            }
        });

    }

    private void BusNumberList(){

        final List<String> busnumbers = new ArrayList<>();
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(),android.R.layout.simple_list_item_1,busnumbers);
        adapter.setDropDownViewResource(android.R.layout.simple_list_item_1);
        mBusNumber.setAdapter(adapter);
        mBusNumber.setThreshold(1);

        fStore.collection("BusNumber").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    for(QueryDocumentSnapshot documentSnapshot : task.getResult()){
                        String busnumber = documentSnapshot.getString("busNumber");
                        busnumbers.add(busnumber);
                    }
                    adapter.notifyDataSetChanged();
                }
            }
        });

        mNumberDropdown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBusNumber.showDropDown();
            }
        });

        mBusNumber.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String busNo = mBusNumber.getText().toString();
                bus.setBusNumber(busNo);
                BusStopList(busNo);
            }
        });
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
