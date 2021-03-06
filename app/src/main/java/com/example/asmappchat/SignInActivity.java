package com.example.asmappchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.asmappchat.Model.Users;
import com.example.asmappchat.databinding.ActivitySignInBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Date;

public class SignInActivity extends AppCompatActivity {

    ActivitySignInBinding binding;
    ProgressDialog progressDialog;
    FirebaseAuth mAuth;
    FirebaseDatabase firebaseDatabase;

    GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getSupportActionBar().hide();

        mAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance("https://asmappchat-default-rtdb.asia-southeast1.firebasedatabase.app/");

        progressDialog = new ProgressDialog(SignInActivity.this);
        progressDialog.setTitle("????ng Nh???p");
        progressDialog.setMessage("??ang ????ng Nh???p\nVui L??ng ch??? trong gi??y l??t.");

        //Kh???i t???o google
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        binding.btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //N???u ch??a nh???p ????? th??ng tin ????ng nh???p ( Email , Password ) th?? s??? b??o " B???n ch??a nh???p ????? th??ng tin ????ng nh???p "
                if (!binding.txtEmail.getText().toString().isEmpty() && !binding.txtPassword.getText().toString().isEmpty()){
                    progressDialog.show();
                    // N???u nh???p ????ng Email v?? password th?? s??? ????ng Nh???p th??nh c??ng . N???u kh??ng ????ng nh???p th??nh c??ng th?? s??? t???m d???ng ????ng nh???p v?? ph???i ????ng nh???p l???i
                    mAuth.signInWithEmailAndPassword(binding.txtEmail.getText().toString(),binding.txtPassword.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            progressDialog.dismiss();
                            if (task.isSuccessful()){
                                Intent intent = new Intent(SignInActivity.this,MainActivity.class);
                                startActivity(intent);
                                sendNotification();
                            }else {
                             //   Toast.makeText(SignInActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                Toast.makeText(SignInActivity.this, "T??i Kho???n ho???c m???t kh???u kh??ng ch??nh x??c", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }else {
                    Toast.makeText(SignInActivity.this, "B???n ch??a nh???p ????? th??ng tin ????ng nh???p", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //Khi ???? th???c hi???n ????ng nh???p => Tho??t app , v?? l???i s??? ??? trang ch??nh .
        if (mAuth.getCurrentUser()!=null){
            Intent intent = new Intent(SignInActivity.this,MainActivity.class);
            startActivity(intent);
        }

        //Chuy???n t??? ????ng nh???p sang ????ng k??
        binding.txtClickSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignInActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });

        //btn google
        binding.btnGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });
    }


    //code Noti ( th??ng b??o )
    private void sendNotification() {
        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        android.app.Notification notification = new NotificationCompat.Builder(this, com.example.asmappchat.Notification.CHANNEL_ID)
                .setContentTitle("Th??ng b??o t??? h??? th???ng")
                .setContentText("B???n ???? ????ng nh???p th??nh c??ng ! Ch??c b???n vui v???")
                .setSmallIcon(R.drawable.notification)
                .setSound(uri)
                .build();

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null){
            notificationManager.notify(getNotificationId(),notification);
        }
    }

    //H??m th??ng b??o nhi???u l???n
    private int getNotificationId(){
        return (int) new Date().getTime();
    }

    // code Google

    int RC_SIGN_IN = 65;
    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // ????ng nh???p b???ng Google th??nh c??ng, x??c th???c b???ng Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d("TAG", "firebaseAuthWithGoogle:" + account.getId());
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // ????ng nh???p Google kh??ng th??nh c??ng, h??y c???p nh???t giao di???n ng?????i d??ng th??ch h???p
                Log.w("TAG", "????ng nh???p t??i kho???n Google th???t b???i", e);
            }
        }
    }

    //??p s??? ki??n gg l??n firebase
    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("TAG", "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();

                            //??p User google v??o realtime
                            Users users = new Users();
                            users.setUserId(user.getUid());
                            users.setUserName(user.getDisplayName());
                            users.setProfilePic(user.getPhotoUrl().toString());
                            firebaseDatabase.getReference().child("Users").child(user.getUid()).setValue(users);

                            Intent intent = new Intent(SignInActivity.this,MainActivity.class);
                            startActivity(intent);

                            Toast.makeText(SignInActivity.this,"????ng nh???p t??i kho???n Google th??nh c??ng.", Toast.LENGTH_SHORT).show();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("TAG", "signInWithCredential:failure", task.getException());
                        }
                    }
                });
    }

}