package com.finalyear.busconductor.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Process;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.finalyear.busconductor.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import static com.finalyear.busconductor.ui.StartActivity.exitConstant;
import java.util.ArrayList;

public class Login extends AppCompatActivity {

    private static final String TAG = "Login";
    private EditText mUsername,mPassword;
    private Button loginbtn;
    private TextView registerhere,forgotpass;
    private ProgressBar progressBar;
    private FirebaseAuth fAuth;
    private FirebaseFirestore fStore;
    private ArrayList<String> email_list=new ArrayList<>();
    private Toast backPressedToast;
    private long backButtonPressedTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mUsername = findViewById(R.id.et_username);
        mPassword = findViewById(R.id.et_password);
        loginbtn = findViewById(R.id.bt_login);
        progressBar = findViewById(R.id.progressBar2);
        registerhere = findViewById(R.id.tv_register);
        forgotpass = findViewById(R.id.tv_forgotpass);

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        loginbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String emailid = mUsername.getText().toString();
                String pass = mPassword.getText().toString();

                if(TextUtils.isEmpty(emailid)){
                    mUsername.setError("Email is required");
                    return;
                }

                if(TextUtils.isEmpty(pass)){
                    mPassword.setError("Password is required");
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);
                //Authenticate User
                fAuth.signInWithEmailAndPassword(emailid,pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            fStore.collection("Conductors").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if (task.isSuccessful()) {
                                        if (task.getResult() != null ) {
                                            for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                                                String email=documentSnapshot.getString("email");
                                                email_list.add(email);
                                                Log.d(TAG,"EMAIL : "+email_list.size()+", TASK :"+task.getResult().size());
                                                if(emailid.equals(email)){
                                                    Toast.makeText(Login.this,"Login Successful",Toast.LENGTH_SHORT).show();
                                                    startActivity(new Intent(getApplicationContext(),MainActivity.class));
                                                }
                                                else if(email_list.size()==task.getResult().size()){
                                                    Toast.makeText(Login.this,"Conductor is not registered",Toast.LENGTH_SHORT).show();
                                                    progressBar.setVisibility(View.INVISIBLE);
                                                }
                                            }
                                        }

                                    }

                                }
                            });
                        }
                        else{
                            Toast.makeText(Login.this,"Error.. ! "+task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.INVISIBLE);
                        }
                    }
                });
            }
        });

        registerhere.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),Register.class));
            }
        });

        forgotpass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String emailid = mUsername.getText().toString();
                Intent intent = new Intent(getApplicationContext(),ForgotPass.class);
                if(!(TextUtils.isEmpty(emailid))){
                    intent.putExtra("emailid",emailid);
                }
                startActivity(intent);
            }
        });
    }

    @Override
    public void onBackPressed() {
        if(backButtonPressedTime+2000 >= System.currentTimeMillis()){
            super.onBackPressed();
            backPressedToast.cancel();
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            exitConstant = 200;
            finish();
        }
        else{
            backPressedToast = Toast.makeText(this, "Press Back Again to Exit", Toast.LENGTH_SHORT);
            backPressedToast.show();
        }
        backButtonPressedTime = System.currentTimeMillis();
    }


    @Override
    protected void onResume() {
        super.onResume();
        if(exitConstant !=100){
            startActivity(new Intent(getApplicationContext(),StartActivity.class));
        }
    }
}
