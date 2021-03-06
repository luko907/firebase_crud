package com.lucaskoch.firebasecrud.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;

import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.lucaskoch.firebasecrud.R;
import com.lucaskoch.firebasecrud.model.ItemRVModel;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;


public class AddItemFragment extends Fragment {

    private TextInputEditText idEDT_title, idEDT_price, idEDT_description;
    private TextView max_price;
    private ImageView idIMG_preview;
    private MaterialButton idBTN_upload_image,idBTN_cancel_img_preview;
    private DatabaseReference databaseReference;
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private StorageReference storageReference;
    private String itemID;
    private SwipeRefreshLayout add_item_swipe_container;
    private AutoCompleteTextView idACT_typeDropdown, idACT_genderDropdown, idACT_sizeDropdown;
    private ActivityResultLauncher<Intent> someActivityResultLauncher;
    private String[] types;
    private String[] gender;
    private String[] sizes;
    private String randomUUID;
    private Uri imageUri = null;
    private String imageLinkFireBase;
    private Bitmap bitmap;
    private byte[] dataToSend;
    private final ObservableBoolean uploadImageToFirebase = new ObservableBoolean();
    private final ObservableBoolean checkImageLinkFirebase = new ObservableBoolean();
    String userId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_item, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        randomUUID = UUID.randomUUID().toString();
        types = getResources().getStringArray(R.array.clothe_type);
        gender = getResources().getStringArray(R.array.gender);
        sizes = getResources().getStringArray(R.array.sizes);
        FirebaseStorage storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        idEDT_title = view.findViewById(R.id.idEDT_title);
        idEDT_price = view.findViewById(R.id.idEDT_price);
        idBTN_upload_image = view.findViewById(R.id.idBTN_upload_image);
        idBTN_cancel_img_preview = view.findViewById(R.id.idBTN_cancel_img_preview);
        idIMG_preview = view.findViewById(R.id.idIMG_preview);
        idEDT_description = view.findViewById(R.id.idEDT_description);
        idACT_typeDropdown = view.findViewById(R.id.idACT_typeDropdown);
        idACT_genderDropdown = view.findViewById(R.id.idACT_genderDropdown);
        idACT_sizeDropdown = view.findViewById(R.id.idACT_sizeDropdown);
        max_price = view.findViewById(R.id.max_price);
        Button idBTN_add_clothe = view.findViewById(R.id.idBTN_accept);
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Users");
        add_item_swipe_container = view.findViewById(R.id.edit_item_swipe_container);



        uploadImageToFirebase.setOnBooleanChangeListener(new OnBooleanChangeListener() {
            @Override
            public void onBooleanChanged(boolean newState) {
                if (newState) {
                    FirebaseStorage storageIn = FirebaseStorage.getInstance();
                    StorageReference stor = storageIn.getReference();
                    stor.child("images/" + userId+"/"+randomUUID).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            /*   Log.v("Tag", "adas  " + uri);*/
                            imageLinkFireBase = uri.toString();
                            checkImageLinkFirebase.set(true);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                        }
                    });
                }
            }
        });
        idACT_typeDropdown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                idACT_typeDropdown.setTextColor(getResources().getColor(R.color.white));
            }
        });
        idACT_genderDropdown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                idACT_genderDropdown.setTextColor(getResources().getColor(R.color.white));
            }
        });
        idACT_sizeDropdown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                idACT_sizeDropdown.setTextColor(getResources().getColor(R.color.white));
            }
        });

        ArrayAdapter<String> typeAdapter =
                new ArrayAdapter<>(getContext(), R.layout.drowdown_template, types);
        idACT_typeDropdown.setAdapter(typeAdapter);
        ArrayAdapter<String> genderAdapter =
                new ArrayAdapter<>(getContext(), R.layout.drowdown_template, gender);
        idACT_genderDropdown.setAdapter(genderAdapter);
        ArrayAdapter<String> sizeAdapter =
                new ArrayAdapter<>(getContext(), R.layout.drowdown_template, sizes);
        idACT_sizeDropdown.setAdapter(sizeAdapter);

        someActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            final ContentResolver cr = requireActivity().getContentResolver();
                            imageUri = Objects.requireNonNull(result.getData()).getData();
            /*                Log.v("Tag","imageUri :"+ imageUri.toString());
                            Log.v("Tag","result.getData() :"+result.getData());
                            Log.v("Tag","result.getData()).getData() :"+result.getData().getData());*/
                            bitmap = null;
                            try {
                                bitmap = MediaStore
                                        .Images
                                        .Media
                                        .getBitmap(
                                                cr,
                                                imageUri);
                                ByteArrayOutputStream temp = new ByteArrayOutputStream();
                                bitmap = resizeImage(bitmap,600,true);
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, temp);
                                dataToSend = temp.toByteArray();
                                idIMG_preview.setImageBitmap(bitmap);
                                idBTN_cancel_img_preview.setVisibility(View.VISIBLE);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });

        idBTN_cancel_img_preview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                idIMG_preview.setImageBitmap(null);
                idBTN_cancel_img_preview.setVisibility(View.GONE);
                dataToSend = null;
            }
        });
        idBTN_upload_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();

            }
        });
        idEDT_price.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
/*                Log.v("Tag", "Start " + start);
                Log.v("Tag", "Before " + before);*/
                if ((start - before) < 4) {
                    max_price.setVisibility(View.GONE);
                } else {
                    max_price.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        add_item_swipe_container.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

            @SuppressLint("SetTextI18n")
            @Override
            public void onRefresh() {
                //Do your task
                ArrayAdapter<String> typeAdapter =
                        new ArrayAdapter<>(getContext(), R.layout.drowdown_template, types);
                ArrayAdapter<String> genderAdapter =
                        new ArrayAdapter<>(getContext(), R.layout.drowdown_template, gender);
                ArrayAdapter<String> sizeAdapter =
                        new ArrayAdapter<>(getContext(), R.layout.drowdown_template, sizes);
                idACT_typeDropdown.setText("Choose Clothe");
                idACT_typeDropdown.setAdapter(typeAdapter);
                idACT_genderDropdown.setText("Choose Gender");
                idACT_genderDropdown.setAdapter(genderAdapter);
                idACT_sizeDropdown.setText("Choose Size");
                idACT_sizeDropdown.setAdapter(sizeAdapter);
                idEDT_title.setText("");
                idEDT_price.setText("");
                idIMG_preview.setImageBitmap(null);
                idEDT_description.setText("");
                idEDT_title.requestFocus();
                add_item_swipe_container.setRefreshing(false);
            }
        });

        idBTN_add_clothe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = Objects.requireNonNull(idEDT_title.getText()).toString();
                String price = Objects.requireNonNull(idEDT_price.getText()).toString();
                String description = Objects.requireNonNull(idEDT_description.getText()).toString();
                String gender = Objects.requireNonNull(idACT_genderDropdown.getText()).toString();
                String type = Objects.requireNonNull(idACT_typeDropdown.getText()).toString();
                String img = Objects.requireNonNull(idBTN_upload_image.getText()).toString();
                String size = Objects.requireNonNull(idACT_sizeDropdown.getText()).toString();
                itemID = title.toLowerCase(Locale.ROOT);
                ItemRVModel itemRVModel = new ItemRVModel(title, img, type, gender, size, price, description, itemID,randomUUID);
                if (isNetworkConnected()) {
                    if (TextUtils.isEmpty(title) || TextUtils.isEmpty(price) || TextUtils.isEmpty(description) || dataToSend == null) {
                        Toast.makeText(getContext(), "Please fill empty values...", Toast.LENGTH_SHORT).show();
                    } else {
                        databaseReference.child(userId).child(itemID).get().addOnSuccessListener(snapshot -> {
                            /*Log.v("Tag", "snapshot : "+ snapshot.getValue());*/
                            if(snapshot.getValue() != null){
                                Toast.makeText(getContext(), "Data exist", Toast.LENGTH_SHORT).show();
                                idEDT_title.setText("");
                                idEDT_title.requestFocus();
                            }else{
                                uploadImage(dataToSend);
                                databaseReference.child(userId).child(itemID).setValue(itemRVModel);
                            }
                        });
                    }
                    checkImageLinkFirebase.setOnBooleanChangeListener(new OnBooleanChangeListener() {
                        @Override
                        public void onBooleanChanged(boolean newState) {
                            if (newState) {
                                DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("Users").child(userId).child(itemRVModel.getItemID());
                                mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        /*      Log.v("Tag","imagelink" + imageLinkFireBase);*/
                                        mDatabase.child("img").setValue(imageLinkFireBase);
                                        HomeFragment homeFragment = new HomeFragment();
                                        FragmentTransaction fragmentTransaction = getParentFragmentManager().beginTransaction();
                                        fragmentTransaction.replace(R.id.frameLayout_fragment_container, homeFragment).addToBackStack(null);
                                        fragmentTransaction.commit();
                                        Toast.makeText(getContext(), "Uploaded", Toast.LENGTH_SHORT).show();
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                            }

                        }
                    });
                } else {
                    Toast.makeText(getContext(), "No internet connexion", Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) requireContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }

    public void selectImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        someActivityResultLauncher.launch(intent);
    }

    private void uploadImage(byte[] file) {
        if (file != null) {
            final ProgressDialog progressDialog = new ProgressDialog(getContext());
            progressDialog.setTitle("Uploading...");
            progressDialog.show();
            StorageReference putFileUriToFirebase = storageReference.child("images/" + userId+"/"+randomUUID);
            putFileUriToFirebase.putBytes(file)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            uploadImageToFirebase.set(true);
                            progressDialog.dismiss();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(getContext(), "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot
                                    .getTotalByteCount());
                            progressDialog.setMessage("Uploaded " + (int) progress + "%");
                        }
                    });
        }
    }


    public interface OnBooleanChangeListener {
        void onBooleanChanged(boolean newState);
    }

    public static class ObservableBoolean {
        private OnBooleanChangeListener listener;

        private boolean value = false;

        public void setOnBooleanChangeListener(OnBooleanChangeListener listener) {
            this.listener = listener;
        }

        public boolean get() {
            return value;
        }

        public void set(boolean value) {
            this.value = value;

            if (listener != null) {
                listener.onBooleanChanged(value);
            }
        }
    }
    public static Bitmap resizeImage(Bitmap realImage, float maxImageSize,
                                     boolean filter) {
        float ratio = Math.min(
                (float) maxImageSize / realImage.getWidth(),
                (float) maxImageSize / realImage.getHeight());
        int width = Math.round((float) ratio * realImage.getWidth());
        int height = Math.round((float) ratio * realImage.getHeight());

        Bitmap newBitmap = Bitmap.createScaledBitmap(realImage, width,
                height, filter);
        return newBitmap;
    }
}

