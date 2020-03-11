package com.finalyear.busconductor.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.finalyear.busconductor.R;
import com.finalyear.busconductor.model.Conductor;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import static com.finalyear.busconductor.ui.StartActivity.exitConstant;

public class Register extends AppCompatActivity {

    private static final String TAG = "Register";
    private EditText mFullname,mEmail,mRefno,mPassword,mConfirmpassword;
    private TextView loginhere;
    private Button registerbtn;
    private ProgressBar progressBar;
    private FirebaseAuth fAuth;
    private FirebaseFirestore fStore;
    private String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mFullname = findViewById(R.id.et_fullname);
        mEmail = findViewById(R.id.et_email);
        mRefno = findViewById(R.id.et_refno);
        mPassword = findViewById(R.id.et_pass);
        mConfirmpassword = findViewById(R.id.et_confirmpass);
        loginhere = findViewById(R.id.tv_login);
        registerbtn = findViewById(R.id.bt_register);
        progressBar = findViewById(R.id.progressBar);

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        registerbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String pass = mPassword.getText().toString();
                final String conpass = mConfirmpassword.getText().toString();

                final Conductor conductor = new Conductor();
                conductor.setName(mFullname.getText().toString());
                conductor.setEmail(mEmail.getText().toString());
                conductor.setRef_no(mRefno.getText().toString());

                if(TextUtils.isEmpty(conductor.getName())){
                    mFullname.setError("Full Name is required");
                    return;
                }

                if(TextUtils.isEmpty(conductor.getEmail())){
                    mEmail.setError("Email is required");
                    return;
                }

                if(TextUtils.isEmpty(pass)){
                    mPassword.setError("Password is required");
                    return;
                }

                if(TextUtils.isEmpty(conpass)){
                    mConfirmpassword.setError("Please confirm password");
                    return;
                }

                if(pass.length()<6){
                    mPassword.setError("Password must be more than 6 characters");
                    return;
                }
                if(!pass.equals(conpass)){
                    mConfirmpassword.setError("Incorrect password");
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);

                fAuth.createUserWithEmailAndPassword(conductor.getEmail(),pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(Register.this,"Conductor Profile Created",Toast.LENGTH_SHORT).show();
                            userID = fAuth.getCurrentUser().getUid();
                            DocumentReference documentReference = fStore.collection("Conductors").document(userID);
                            Map<String,Object> data = new HashMap<>();
                            data.put("name",conductor.getName());
                            data.put("email",conductor.getEmail());
                            data.put("ref_no",conductor.getRef_no());

                            documentReference.set(data).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d(TAG,"Conductor profile created for "+userID);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d(TAG,"onFailure: "+e.toString());
                                }
                            });
                            startActivity(new Intent(getApplicationContext(),Login.class));
                        }
                        else{
                            Toast.makeText(Register.this,"Error.. ! "+task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.INVISIBLE);

                        }
                    }
                });
            }
        });

        loginhere.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),Login.class));
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(exitConstant !=100){
            startActivity(new Intent(getApplicationContext(),StartActivity.class));
        }
    }
}
