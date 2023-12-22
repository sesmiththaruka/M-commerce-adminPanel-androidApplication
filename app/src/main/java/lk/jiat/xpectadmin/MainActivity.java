package lk.jiat.xpectadmin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import lk.jiat.xpectadmin.activity.AdminHome;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getName();
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;
    private EditText editTextUsername;
    private EditText editTextPassword;
    private Button loginButton;
    private boolean Admin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);
        loginButton = findViewById(R.id.btnLogin);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!checkField(editTextUsername)) {
                    Log.e(TAG, "ujh");
                    Toast.makeText(MainActivity.this, "Add Username", Toast.LENGTH_SHORT).show();
                } else if (!checkField(editTextPassword)) {
                    Toast.makeText(MainActivity.this, "Add Password", Toast.LENGTH_SHORT).show();
                } else {
                    Log.e(TAG, "ujhyyyyyyy");
                    firebaseAuth.signInWithEmailAndPassword(editTextUsername.getText().toString(), editTextPassword.getText().toString())
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()){
                                        Log.e(TAG, "getuser");
                                        firestore.collection("userRoles").whereEqualTo("userId", firebaseAuth.getCurrentUser().getUid())
                                                .get()
                                                .addOnSuccessListener(queryDocumentSnapshots -> {
                                                    if (!queryDocumentSnapshots.isEmpty()) {
                                                        Log.e(TAG, "getuserIn roles");
                                                        DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
                                                        String admin = documentSnapshot.getString("isAdmin");
                                                        Log.e(TAG, admin);
                                                        if (admin.equals("1")){
                                                            Log.e(TAG, "getuserIn roles is admin");
                                                            startActivity(new Intent(getApplicationContext(), AdminHome.class));
                                                            finish();
                                                        }
                                                    }
                                                });


                                    }else {
                                        Toast.makeText(MainActivity.this, "Invalid username or password", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                }
            }
        });


    }

    private void checkIfAdmin(String userId) {

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            startActivity(new Intent(getApplicationContext(), AdminHome.class));
            finish();
        }
    }

    public boolean checkField(EditText editText) {
        if (editText.getText().toString().isEmpty()) {
            editText.setError("Error");
            return false;
        } else {
            return true;
        }
    }

}