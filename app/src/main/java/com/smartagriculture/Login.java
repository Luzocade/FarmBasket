package com.smartagriculture;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.TimeUnit;

import de.hdodenhof.circleimageview.CircleImageView;

public class Login extends AppCompatActivity {
    private static final String TAG = "AppTest>>>>";
    UiEnhancement uiEnhancement;

    TextInputEditText txtPhno, txtCode, txtName, txtArea;
    TextInputLayout tilCode;
    Button btnSubmitVerify, btnProceed, btnResend;
    ImageButton btnFarmer, btnCustomer;
    LinearLayout typeForm, loginForm, detailsForm, grpCode;
    CircleImageView imgProfile;
    boolean sent;
    String type;
    String name, phno, area, code;
    SharedPreferences userDetails;
    FirebaseDatabase database;
    private FirebaseAuth mAuth;
    private String mVerificationId;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private boolean mVerificationInProgress;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private FirebaseStorage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //view initialisation
        typeForm = findViewById(R.id.typeForm);
        loginForm = findViewById(R.id.loginForm);
        detailsForm = findViewById(R.id.detailsForm);
        grpCode = findViewById(R.id.grpCode);
        txtCode = findViewById(R.id.txtCode);
        txtPhno = findViewById(R.id.txtPhno);
        txtName = findViewById(R.id.txtName);
        txtArea = findViewById(R.id.txtArea);
        tilCode = findViewById(R.id.tilCode);
        btnSubmitVerify = findViewById(R.id.btnSubmitVerify);
        btnFarmer = findViewById(R.id.btnFarmer);
        btnCustomer = findViewById(R.id.btnCustomer);
        btnProceed = findViewById(R.id.btnProceed);
        btnResend = findViewById(R.id.btnResend);
        imgProfile = findViewById(R.id.imgProfile);
        //end view initialisation

        //var initialisations
        sent = false;
        type = null;
        name = null;
        phno = null;
        area = null;
        code = null;
        mAuth = FirebaseAuth.getInstance();
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.
                Log.i(TAG, "onVerificationCompleted:" + credential);
                signInWithPhoneAuthCredential(credential);


                // [START_EXCLUDE silent]
                mVerificationInProgress = false;
                // [END_EXCLUDE]
                //signInWithPhoneAuthCredential(credential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                Log.i(TAG, "onVerificationFailed", e);
                btnSubmitVerify.setText(R.string.login_btn_submitverify_txt2);
                uiEnhancement.removeAllDrawables(btnSubmitVerify, btnResend);
                // [START_EXCLUDE silent]
                mVerificationInProgress = false;
                // [END_EXCLUDE]

                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                    // [START_EXCLUDE]
                    Toast.makeText(Login.this, "FirebaseAuthInvalidCredentialsException", Toast.LENGTH_SHORT).show();
                    // [END_EXCLUDE]
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                    // [START_EXCLUDE]

                    Snackbar.make(findViewById(android.R.id.content), "Quota exceeded.",
                            Snackbar.LENGTH_SHORT).show();
                    // [END_EXCLUDE]
                }
            }

            @Override
            public void onCodeSent(String verificationId,
                                   PhoneAuthProvider.ForceResendingToken token) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                Log.i(TAG, "onCodeSent:" + verificationId);

                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = token;

                // [START_EXCLUDE]
                // Update UI
                Toast.makeText(Login.this, "SMS Sent", Toast.LENGTH_SHORT).show();
                uiEnhancement.hide(false, 0, 0, grpCode);
                uiEnhancement.visible(grpCode);
                uiEnhancement.show(true, 0, 200, grpCode);
                uiEnhancement.removeAllDrawables(btnSubmitVerify, btnResend);
                btnResend.setText(R.string.login_btn_resend_txt);
                btnSubmitVerify.setText(R.string.login_btn_submitverify_txt2);
                sent = true;
                //updateUI(STATE_CODE_SENT);
                // [END_EXCLUDE]
            }
        };
        userDetails = getSharedPreferences("userDetails", MODE_PRIVATE);
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
        //end var initialisations

        //anims and other ui initialisations
        uiEnhancement = new UiEnhancement();
        uiEnhancement.blur(this, R.drawable.crops, 10, true, findViewById(R.id.bg));
        uiEnhancement.gone(grpCode, loginForm, detailsForm);
        uiEnhancement.visible(typeForm);
        imgProfile.setImageResource(R.drawable.user_default_profile_pic);
        //end anims and other ui initialisations

        btnResend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnSubmitVerify.setText(R.string.login_btn_resend_intermediate);
                uiEnhancement.setDrawable('s', getDrawable(R.drawable.ic_spinner200px), btnResend);
                resendVerificationCode(phno, mResendToken);
            }
        });

        btnProceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (txtName.getText().toString().trim().isEmpty() || txtArea.getText().toString().trim().isEmpty()) {
                    Toast.makeText(Login.this, getString(R.string.login_empty_error), Toast.LENGTH_SHORT).show();
                    return;
                }

                name = txtName.getText().toString().trim();
                area = txtArea.getText().toString().trim();

                if (type.equalsIgnoreCase("farmer")) {
                    addNewUserNode(new Farmer(name, txtPhno.getText().toString().trim(), area));
                    uploadImage();
                    SharedPreferences.Editor userEditor = userDetails.edit();
                    userEditor.putString("type", type);
                    userEditor.putString("phno", txtPhno.getText().toString().trim());
                    userEditor.putString("name", name);
                    userEditor.putString("area", area);
                    userEditor.apply();
                    startActivity(new Intent(Login.this, FarmerMain.class));
                    Login.this.finish();
                } else if (type.equalsIgnoreCase("customer")) {
                    addNewUserNode(new Customer(name, txtPhno.getText().toString().trim(), area));
                    SharedPreferences.Editor userEditor = userDetails.edit();
                    userEditor.putString("type", type);
                    userEditor.putString("phno", txtPhno.getText().toString().trim());
                    userEditor.putString("name", name);
                    userEditor.putString("area", area);
                    userEditor.apply();
                    startActivity(new Intent(Login.this, CustomerMain.class));
                    Login.this.finish();
                }
                Log.i(TAG, "saved user: " + userDetails.getAll());
            }
        });

        btnSubmitVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!sent) {
                    // these lines will execute before sending code
                    if (txtPhno.getText().toString().length() != 10) {
                        txtPhno.setError(getString(R.string.login_txtPhno_invalid_error));
                        txtPhno.requestFocus();
                        return;
                    }
                    phno = "+91" + txtPhno.getText().toString().trim();
                    hideKeyboard();
                    btnSubmitVerify.setText(R.string.login_btn_submit_intermediate);
                    uiEnhancement.setDrawable('s', getDrawable(R.drawable.ic_spinner200px), btnSubmitVerify);
                    startPhoneNumberVerification(phno);
                } else {
                    // these lines will execute after sending code
                    if (txtCode.getText().toString().length() != 6) {
                        txtCode.setError(getString(R.string.login_txtCode_short_error));
                        txtCode.requestFocus();
                        return;
                    }
                    code = txtCode.getText().toString().trim();
                    hideKeyboard();
                    btnSubmitVerify.setText(R.string.login_btn_verify_intermediate);
                    uiEnhancement.setDrawable('s', getDrawable(R.drawable.ic_spinner200px), btnSubmitVerify);
                    verifyPhoneNumberWithCode(mVerificationId, txtCode.getText().toString());
                }
            }

        });

        btnFarmer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                type = "farmer";
                uiEnhancement.fadeSwitch(typeForm, loginForm, 200);
            }
        });

        btnCustomer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                type = "customer";
                uiEnhancement.fadeSwitch(typeForm, loginForm, 200);
            }
        });

        imgProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imgProfileClick();
            }
        });

    }

    private void addNewUserNode(Farmer farmer) {
        DatabaseReference fGroup = database.getReference("farmers");
        DatabaseReference fUser = fGroup.child(farmer.getPhno());
        fUser.setValue(farmer);
    }

    private void addNewUserNode(Customer customer) {
        DatabaseReference cGroup = database.getReference("customers");
        DatabaseReference cUser = cGroup.child(txtPhno.getText().toString());
        cUser.setValue(customer);
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (loginForm.getVisibility() == View.VISIBLE) {
            uiEnhancement.fadeSwitch(loginForm, typeForm, 200);
            uiEnhancement.gone(grpCode);
            btnSubmitVerify.setText(R.string.login_btn_submitverify_txt1);
            sent = false;
        } else {

            //Test
            FirebaseAuth.getInstance().signOut();
            //EndTest

            super.onBackPressed();
        }
    }

    @Override
    protected void onStart() {

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        super.onStart();
    }

    private void startPhoneNumberVerification(String phoneNumber) {
        // [START start_phone_auth]
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks);        // OnVerificationStateChangedCallbacks
        // [END start_phone_auth]

        mVerificationInProgress = true;
    }

    private void verifyPhoneNumberWithCode(String verificationId, String code) {
        // [START verify_with_code]
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        // [END verify_with_code]
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.i(TAG, "signInWithCredential:success");
                    Log.i(TAG, "onComplete: " + mAuth.getCurrentUser().getPhoneNumber());

                    Toast.makeText(Login.this, "Successfully signed in", Toast.LENGTH_SHORT).show();
                    uiEnhancement.gone(grpCode);
                    btnSubmitVerify.setText(R.string.login_btn_verify_success_intermediate);

                    if (type.equalsIgnoreCase("farmer")) {
                        DatabaseReference fGroup = database.getReference("farmers");
                        DatabaseReference fMember = fGroup.child(txtPhno.getText().toString().trim());
                        fMember.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.hasChildren()){
                                    SharedPreferences.Editor userEditor = userDetails.edit();
                                    userEditor.putString("type", "farmer");
                                    userEditor.putString("phno", txtPhno.getText().toString().trim());
                                    userEditor.putString("name", dataSnapshot.getValue(Farmer.class).getName());
                                    userEditor.putString("area", dataSnapshot.getValue(Farmer.class).getArea());
                                    userEditor.putString("idUrl",dataSnapshot.getValue(Farmer.class).getIdUrl());
                                    userEditor.putString("certUrl",dataSnapshot.getValue(Farmer.class).getCertUrl());
                                    userEditor.apply();
                                    startActivity(new Intent(Login.this, FarmerMain.class));
                                    uiEnhancement.removeAllDrawables(btnSubmitVerify, btnResend);
                                    Login.this.finish();
                                }else{
                                    uiEnhancement.fadeSwitch(loginForm, detailsForm, 200);
                                    uiEnhancement.removeAllDrawables(btnSubmitVerify, btnResend);
                                }

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }else if(type.equalsIgnoreCase("customer")){
                        DatabaseReference fGroup = database.getReference("customers");
                        DatabaseReference fMember = fGroup.child(txtPhno.getText().toString().trim());
                        fMember.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.hasChildren()){
                                    SharedPreferences.Editor userEditor = userDetails.edit();
                                    userEditor.putString("type", "customer");
                                    userEditor.putString("phno", txtPhno.getText().toString().trim());
                                    userEditor.putString("name", dataSnapshot.getValue(Customer.class).getName());
                                    userEditor.putString("area", dataSnapshot.getValue(Customer.class).getArea());
                                    userEditor.putString("email",dataSnapshot.getValue(Customer.class).getEmail());
                                    userEditor.putString("dob",dataSnapshot.getValue(Customer.class).getDob());
                                    if(dataSnapshot.getValue(Customer.class).getAddress()!=null) {
                                        userEditor.putString("addr", dataSnapshot.getValue(Customer.class).getAddress().getAddressString());
                                        userEditor.putString("addrPt", dataSnapshot.getValue(Customer.class).getAddress().getLatitude()
                                                + "_" + dataSnapshot.getValue(Customer.class).getAddress().getLongitude());
                                    }
                                    userEditor.apply();
                                    startActivity(new Intent(Login.this, CustomerMain.class));
                                    uiEnhancement.removeAllDrawables(btnSubmitVerify, btnResend);
                                    Login.this.finish();
                                }else{
                                    uiEnhancement.fadeSwitch(loginForm, detailsForm, 200);
                                    uiEnhancement.removeAllDrawables(btnSubmitVerify, btnResend);
                                }

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }

                } else {
                    // Sign in failed, display a message and update the UI
                    Log.i(TAG, "signInWithCredential:failure", task.getException());
                    if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                        // [START_EXCLUDE silent]
                        txtCode.setError(getString(R.string.login_txtCode_invalid_error));
                        // [END_EXCLUDE]
                    }
                    // [START_EXCLUDE silent]
                    // Update UI
                    Toast.makeText(Login.this, "SignIn Failed", Toast.LENGTH_SHORT).show();
                    //updateUI(STATE_SIGNIN_FAILED);
                    // [END_EXCLUDE]
                }
            }
        });
    }

    private void resendVerificationCode(String phoneNumber, PhoneAuthProvider.ForceResendingToken token) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks,         // OnVerificationStateChangedCallbacks
                token);             // ForceResendingToken from callbacks
    }

    private void imgProfileClick() {
        CropImage.activity().start(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i(TAG, "onActivityResult: Compare: " + requestCode + " " + CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            Log.i(TAG, "onActivityResult: CropImgReqestCode: " + resultCode);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                //Log.i(TAG, "onActivityResult: ImageUri: " + resultUri);
                imgProfile.setImageURI(resultUri);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Log.i(TAG, "onActivityResult: " + error.getMessage());
            }
        }
    }

    private void uploadImage() {
        final StorageReference pPic = storage.getReference("images/" + txtPhno.getText().toString().trim());
        imgProfile.setDrawingCacheEnabled(true);
        imgProfile.buildDrawingCache();
        Bitmap bitmap = imgProfile.getDrawingCache();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = pPic.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.i(TAG, "onFailure: "+exception.getMessage());
                //Toast.makeText(getActivity(), "Profile picture update failed. Please check your internet connection and try again.", Toast.LENGTH_SHORT).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                pPic.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Uri downloadUrl = uri;
                        DatabaseReference pPicUri = database.getReference("farmers/" + txtPhno.getText().toString().trim() + "/profilePic");
                        pPicUri.setValue(downloadUrl.toString());
                        btnProceed.setText("Proceed");
                        btnProceed.setEnabled(true);
                    }
                });

                //setProfilePic(downloadUrl.toString());
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                btnProceed.setEnabled(true);
                btnProceed.setText(String.format("Uploading Image... %d ", (taskSnapshot.getBytesTransferred() * 100) / taskSnapshot.getTotalByteCount()));
                btnProceed.setEnabled(false);
            }
        });
    }

    private void setProfilePic(String profilePic) {
        if (profilePic.isEmpty()||profilePic==null)
            Glide.with(this).load(R.drawable.user_default_profile_pic).into(imgProfile);
        else
            Glide.with(this).load(profilePic).into(imgProfile);
    }
}
