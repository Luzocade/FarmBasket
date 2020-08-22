package com.smartagriculture;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

import static android.app.Activity.RESULT_OK;

public class FarmerProfile extends Fragment {

    private static final String TAG = "AppTest>>>>";
    private int mode = -1;
    private Button btnEditUpdate, btnSignOut, btnId, btnCert, btnSelPic, btnChangePic, btnCallHq;
    private TextInputEditText txtName, txtArea;
    private FirebaseDatabase database;
    private SharedPreferences userDetails, modeS;
    private boolean editMode;
    private String name, area;
    private ViewGroup viewContainer;
    private CircleImageView imgProfile;
    private View v;
    private FirebaseStorage storage;
    private Farmer farmer;
    private TextView txtStatus;
    private ImageView formPic;
    private UiEnhancement uiEnhancement;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        viewContainer = container;
        v = inflater.inflate(R.layout.fragment_farmer_profile, container, false);
        btnEditUpdate = v.findViewById(R.id.btnEditUpdate);
        btnSignOut = v.findViewById(R.id.btnSignOut);
        txtName = v.findViewById(R.id.txtName);
        txtArea = v.findViewById(R.id.txtArea);
        imgProfile = v.findViewById(R.id.imgProfile);
        btnId = v.findViewById(R.id.btnId);
        btnCert = v.findViewById(R.id.btnCert);
        formPic = v.findViewById(R.id.formPic);
        btnSelPic = v.findViewById(R.id.btnSelPic);
        txtStatus = v.findViewById(R.id.txtStatus);
        btnChangePic = v.findViewById(R.id.btnChangePic);
        btnCallHq = v.findViewById(R.id.btnCallHq);

        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
//        updateValuesFromBundle(savedInstanceState);

        uiEnhancement = new UiEnhancement();

        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
        userDetails = Objects.requireNonNull(getActivity()).getSharedPreferences("userDetails", Context.MODE_PRIVATE);
        modeS = Objects.requireNonNull(getActivity()).getSharedPreferences("mode", Context.MODE_PRIVATE);
        //Log.i(TAG, "onViewCreated: userDetails: "+userDetails.getAll().toString());
        txtName.setText(userDetails.getString("name", ""));
        txtArea.setText(userDetails.getString("area", ""));

        txtName.setEnabled(false);
        txtArea.setEnabled(false);
        editMode = false;

        Glide.with(v).load(R.drawable.ic_spinner200px).into(imgProfile);
        uiEnhancement.gone(btnSelPic, btnChangePic);
        getProfilePic();


        btnId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mode = 1;
                modeS.edit().putInt("m",mode).apply();
                if (userDetails.getString("idUrl", "").equalsIgnoreCase("")) {
                    txtStatus.setText(R.string.farmer_profile_not_set_status_txt);
                    uiEnhancement.fadeSwitch(formPic, btnSelPic, 200);
                } else {
                    if (btnSelPic.getVisibility() == View.VISIBLE)
                        uiEnhancement.fadeSwitch(btnSelPic, formPic, 200);
                    uiEnhancement.visible(btnChangePic);
                    String imgUrl = userDetails.getString("idUrl", "");
                    Glide.with(v).load(imgUrl).into(formPic);
                    getApprovals();
                }
            }
        });

        btnCert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mode = 2;
                modeS.edit().putInt("m",mode).apply();
                if (userDetails.getString("certUrl", "").equalsIgnoreCase("")) {
                    txtStatus.setText(R.string.farmer_profile_not_set_status_txt);
                    uiEnhancement.fadeSwitch(formPic, btnSelPic, 200);
                } else {
                    if (btnSelPic.getVisibility() == View.VISIBLE)
                        uiEnhancement.fadeSwitch(btnSelPic, formPic, 200);
                    uiEnhancement.visible(btnChangePic);
                    String imgUrl = userDetails.getString("certUrl", "");
                    Glide.with(v).load(imgUrl).into(formPic);
                    getApprovals();
                }
            }
        });

        btnSelPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openPhotoChooser();
            }
        });

        btnSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor userEditor = userDetails.edit();
                userEditor.clear().apply();
                FirebaseAuth.getInstance().signOut();
                Toast.makeText(getActivity(), "Successfully Signed out", Toast.LENGTH_SHORT).show();
                getActivity().startActivity(new Intent(getActivity(), Login.class));
                getActivity().finish();

            }
        });

        btnEditUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (txtName.getText().toString().isEmpty() || txtArea.getText().toString().isEmpty()) {
                    Toast.makeText(getActivity(), "All fields must be filled", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!editMode) {
                    editMode = true;
                    txtName.setEnabled(true);
                    txtArea.setEnabled(true);
                    txtName.requestFocus();
                    name = txtName.getText().toString().trim();
                    area = txtArea.getText().toString().trim();
                    btnEditUpdate.setText(R.string.farmer_profile_btn_update_txt);
                } else {
                    hideKeyboard();
                    editMode = false;
                    txtName.setEnabled(false);
                    txtArea.setEnabled(false);
                    btnEditUpdate.setText(R.string.farmer_profile_btn_edit_txt);
                    if (name.equalsIgnoreCase(txtName.getText().toString().trim()) && area.equalsIgnoreCase(txtArea.getText().toString())) {
                        Toast.makeText(getActivity(), "No changes made", Toast.LENGTH_SHORT).show();
                    } else {
                        DatabaseReference fGroup = database.getReference("farmers");
                        final DatabaseReference fMember = fGroup.child(userDetails.getString("phno", ""));
                        fMember.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                farmer = dataSnapshot.getValue(Farmer.class);
                                farmer.setName(txtName.getText().toString().trim());
                                farmer.setPhno(userDetails.getString("phno", ""));
                                farmer.setArea(txtArea.getText().toString().trim());
                                fMember.setValue(farmer);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                        SharedPreferences.Editor userEditor = userDetails.edit();
                        userEditor.putString("name", txtName.getText().toString().trim());
                        userEditor.putString("area", txtArea.getText().toString().trim());
                        userEditor.apply();
                        Toast.makeText(getActivity(), "Details updated successfully", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        imgProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mode = 0;
                modeS.edit().putInt("m",mode).apply();
                openPhotoChooser();
            }
        });

        btnChangePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //if (mode == 0) mode = 1;
                mode = modeS.getInt("m",-1);
                Log.i(TAG, "onClick: mode: " + mode);
                openPhotoChooser();
            }
        });

        btnCallHq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String PHNO = getActivity().getSharedPreferences("cc", 0).getString("ccphno", "8437860605");

                if (ContextCompat.checkSelfPermission(getActivity(),
                        Manifest.permission.CALL_PHONE)
                        == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(Intent.ACTION_CALL);

                    intent.setData(Uri.parse("tel:" + PHNO));
                    getActivity().startActivity(intent);
                }
            }
        });

        mode = modeS.getInt("m",-1);
        Log.i(TAG, "onViewCreated: mode: " + mode);
        if (mode == -1 || mode == 1)
            btnId.performClick();
        else if (mode == 2)
            btnCert.performClick();

    }

    private void openPhotoChooser() {
        CropImage.activity().start(Objects.requireNonNull(getContext()), this);
    }

    private void getProfilePic() {

        DatabaseReference fGroup = database.getReference("farmers");
        DatabaseReference fMember = fGroup.child(userDetails.getString("phno", ""));
        fMember.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Farmer f = dataSnapshot.getValue(Farmer.class);
                if (f != null) {
                    if (f.getProfilePic() != null) {
                        mode = 0;
                        modeS.edit().putInt("m",mode).apply();
                        setProfilePic(f.getProfilePic());
                    } else {
                        mode = 0;
                        modeS.edit().putInt("m",mode).apply();
                        setProfilePic("");
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void setProfilePic(String pic) {
        if (pic.isEmpty())
            Glide.with(v).load(R.drawable.user_default_profile_pic).into(imgProfile);
        else {
            Glide.with(v).load(pic).into(imgProfile);
        }
    }

    private void hideKeyboard() {
        if (viewContainer != null) {
            InputMethodManager imm = (InputMethodManager) Objects.requireNonNull(getActivity()).getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(viewContainer.getWindowToken(), 0);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mode = modeS.getInt("m",-1);
        Log.i(TAG, "onActivityResult: mode: " + mode);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && data != null) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                switch (mode) {
                    case 0:
                        //mode = 0;
                        imgProfile.setImageURI(resultUri);
                        uploadProfileImage(resultUri);
                        break;
                    case 1:
                        //mode = 1;
                        formPic.setImageURI(resultUri);
                        uiEnhancement.fadeSwitch(btnSelPic, formPic, 200);
                        //uiEnhancement.visible(formPic);
                        uploadIdImage(resultUri);
                        break;
                    case 2:
                        //mode = 2;
                        //uiEnhancement.visible(formPic);
                        uiEnhancement.fadeSwitch(btnSelPic, formPic, 200);
                        formPic.setImageURI(resultUri);
                        uploadCertImage(resultUri);
                        break;
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Log.i(TAG, "onActivityResult: " + error.getMessage());
            }
        }
    }

    private void uploadCertImage(Uri uri) {
        File compressedImageFile = null;
        try {
            compressedImageFile = new Compressor(getActivity()).compressToFile(new File(uri.getPath()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        UploadTask uploadTask;
        final StorageReference cPic = storage.getReference("images/" + userDetails.getString("phno", "") + "cert");
        uploadTask = cPic.putFile(Uri.fromFile(compressedImageFile));

        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.i(TAG, "onFailure: " + exception.getMessage());
                //Toast.makeText(getActivity(), "Profile picture update failed. Please check your internet connection and try again.", Toast.LENGTH_SHORT).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                mode = 2;
                modeS.edit().putInt("m",mode).apply();
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.

                cPic.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Uri downloadUrl = uri;
                        DatabaseReference cPicUri = database.getReference("farmers/" + userDetails.getString("phno", "") + "/certUrl");
                        cPicUri.setValue(Objects.requireNonNull(downloadUrl).toString());
                        SharedPreferences.Editor userEditor = userDetails.edit();
                        userEditor.putString("certUrl", downloadUrl.toString()).apply();
                        setCertPic(downloadUrl.toString());
                        DatabaseReference certRef = database.getReference("approvals/" + userDetails.getString("phno", "") + "/cert");
                        DatabaseReference certApproveRef = database.getReference("approvals/" + userDetails.getString("phno", "") + "/certApproved");
                        certRef.setValue(downloadUrl.toString());
                        certApproveRef.setValue(false).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                txtStatus.setText(R.string.farmer_profile_pending_status_txt);
                            }
                        });
                    }
                });

            }
        });
    }

    private void setCertPic(String pic) {
        if (pic.isEmpty()) {
            uiEnhancement.gone(formPic);
            txtStatus.setText(R.string.farmer_profile_not_set_status_txt);
        } else {
            uiEnhancement.visible(formPic);
            Glide.with(v).load(pic).into(formPic);
        }
    }

    private void uploadIdImage(Uri uri) {

        File compressedImageFile = null;
        try {
            compressedImageFile = new Compressor(getActivity()).compressToFile(new File(uri.getPath()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        UploadTask uploadTask;
        final StorageReference iPic = storage.getReference("images/" + userDetails.getString("phno", "") + "id");
        uploadTask = iPic.putFile(Uri.fromFile(compressedImageFile));

        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.i(TAG, "onFailure: " + exception.getMessage());
                //Toast.makeText(getActivity(), "Profile picture update failed. Please check your internet connection and try again.", Toast.LENGTH_SHORT).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                mode = 1;
                modeS.edit().putInt("m",mode).apply();
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.

                iPic.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Uri downloadUrl = uri;
                        DatabaseReference iPicUri = database.getReference("farmers/" + userDetails.getString("phno", "") + "/idUrl");
                        iPicUri.setValue(Objects.requireNonNull(downloadUrl).toString());
                        SharedPreferences.Editor userEditor = userDetails.edit();
                        userEditor.putString("idUrl", downloadUrl.toString()).apply();
                        setIdPic(downloadUrl.toString());
                        DatabaseReference idRef = database.getReference("approvals/" + userDetails.getString("phno", "") + "/id");
                        DatabaseReference idApproveRef = database.getReference("approvals/" + userDetails.getString("phno", "") + "/idApproved");
                        idRef.setValue(downloadUrl.toString());
                        idApproveRef.setValue(false).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                txtStatus.setText(R.string.farmer_profile_pending_status_txt);
                            }
                        });
                    }
                });


            }
        });
    }

    private void setIdPic(String pic) {
        if (pic.isEmpty()) {
            uiEnhancement.gone(formPic);
            txtStatus.setText(R.string.farmer_profile_not_set_status_txt);
        } else {
            uiEnhancement.visible(formPic);
            Glide.with(v).load(pic).into(formPic);
        }
    }


    private void uploadProfileImage(Uri uri) {
        File compressedImageFile = null;
        try {
            compressedImageFile = new Compressor(getActivity()).compressToFile(new File(uri.getPath()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        UploadTask uploadTask;
        final StorageReference pPic = storage.getReference("images/" + userDetails.getString("phno", ""));
        uploadTask = pPic.putFile(Uri.fromFile(compressedImageFile));

        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.i(TAG, "onFailure: " + exception.getMessage());
                //Toast.makeText(getActivity(), "Profile picture update failed. Please check your internet connection and try again.", Toast.LENGTH_SHORT).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                mode = 0;
                modeS.edit().putInt("m",mode).apply();
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.

                pPic.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Uri downloadUrl = uri;
                        DatabaseReference pPicUri = database.getReference("farmers/" + userDetails.getString("phno", "") + "/profilePic");
                        pPicUri.setValue(Objects.requireNonNull(downloadUrl).toString());
                        setProfilePic(downloadUrl.toString());
                    }
                });

            }
        });


    }

    private void getApprovals() {

        mode = modeS.getInt("m",-1);

        Log.i(TAG, "getApprovals: mode: " + mode);
        switch (mode) {

            case 1:
                DatabaseReference idApproveRef = database.getReference("approvals/" + userDetails.getString("phno", "") + "/idApproved");
                idApproveRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            if (dataSnapshot.getValue(Boolean.class))
                                txtStatus.setText(R.string.farmer_profile_approved_status_txt);
                            else
                                txtStatus.setText(R.string.farmer_profile_pending_status_txt);
                        } else if (userDetails.getString("idUrl", "").equalsIgnoreCase(""))
                            txtStatus.setText(R.string.farmer_profile_not_set_status_txt);
                        else {
                            txtStatus.setText(R.string.farmer_profile_denied_status_txt);
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                break;

            case 2:
                DatabaseReference certApproveRef = database.getReference("approvals/" + userDetails.getString("phno", "") + "/certApproved");
                certApproveRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            if (dataSnapshot.getValue(Boolean.class))
                                txtStatus.setText(R.string.farmer_profile_approved_status_txt);
                            else
                                txtStatus.setText(R.string.farmer_profile_pending_status_txt);
                        } else if (userDetails.getString("certUrl", "").equalsIgnoreCase(""))
                            txtStatus.setText(R.string.farmer_profile_not_set_status_txt);
                        else {
                            txtStatus.setText(R.string.farmer_profile_denied_status_txt);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
        }
    }
}
