package com.adeks.formapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.adeks.formapp.model.User;
import com.adeks.formapp.retrofit.FormService;
import com.adeks.formapp.retrofit.RetrofitClientClass;

import java.io.File;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class MainActivity extends AppCompatActivity {
    public static final int REQUEST_CODE_STORAGE_PERMISSION = 1;
    public static final int REQUEST_CODE_SELECT_IMAGE = 2;
    private static final String TAG = "MainActivity";
    private ImageView mImageView;
    private File mSelectedFile;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mImageView = findViewById(R.id.imagev);
        user = new User("Adeks","kays","09035016818","12345678","12345678","address","Mobile",
                "Oyo", null, "lagelu", "john@gmail.com", "text","5000","1","Oyo state Nigeria","6");
        findViewById(R.id.button).setOnClickListener(v -> {
            if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        MainActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_CODE_STORAGE_PERMISSION
                );
            } else {
                selectImage();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE_STORAGE_PERMISSION && grantResults.length > 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selectImage();
            } else {
                Toast.makeText(this,"Permission denied",Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SELECT_IMAGE && resultCode == RESULT_OK) {
            if (data != null) {
                Uri selectedImageUri = data.getData();
                if (selectedImageUri != null){
                    try {
                        InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        mImageView.setImageBitmap(bitmap);
                        mSelectedFile = new File(getPathFromUri(selectedImageUri));
                    }catch (Exception ex) {
                        Toast.makeText(this,ex.getMessage(),Toast.LENGTH_LONG).show();
                    }
                }
            }
        }
    }
    private String getPathFromUri(Uri contentUri) {
        String filePath;
        Cursor cursor = getContentResolver().
                query(contentUri,null,null,null,null);
        if (cursor == null) {
            filePath = contentUri.getPath();
        } else {
            cursor.moveToFirst();
            int index = cursor.getColumnIndex("_data");
            filePath = cursor.getString(index);
        }
        cursor.close();
        return filePath;
    }

    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_CODE_SELECT_IMAGE);
        }
    }


    public void sendFormRequest(View view) {
        if (mSelectedFile == null) {
            Toast.makeText(this,"You need to select a file",Toast.LENGTH_LONG).show();
        }else {
            user.setFile(mSelectedFile);
            Retrofit retrofit = RetrofitClientClass.getRetrofitInstance();
            FormService form = retrofit.create(FormService.class);

            MultipartBody.Builder builder = new MultipartBody.Builder();
            builder.setType(MultipartBody.FORM);
            builder.addFormDataPart("address",user.getAddress());
            builder.addFormDataPart("availability",user.getAvailability());
            builder.addFormDataPart("description",user.getDescription());
            builder.addFormDataPart("duration",user.getDuration());
            builder.addFormDataPart("email",user.getEmail());
            builder.addFormDataPart("first_name",user.getFirst_name());
            RequestBody requestBody = RequestBody.create(mSelectedFile,MediaType.parse("multipart/form-data"));
            builder.addFormDataPart("image",mSelectedFile.getName(),requestBody);
            builder.addFormDataPart("last_name",user.getLast_Name());
            builder.addFormDataPart("lga",user.getLga());
            builder.addFormDataPart("location",user.getLocation());
            builder.addFormDataPart("password",user.getPassword());
            builder.addFormDataPart("password_confirmation",user.getPassword_confirmation());
            builder.addFormDataPart("phone",user.getPhone());
            builder.addFormDataPart("price",user.getPrice());
            builder.addFormDataPart("service",user.getService());
            builder.addFormDataPart("state",user.getState());
            RequestBody finalRb = builder.build();

//        Call<ResponseBody> upload = form.createUser(RequestBody.create(String.valueOf(user),MediaType.get("application/json")),MultipartBody.Part.createFormData("image",file.getName(), RequestBody.create(file,MediaType.parse("image"))));
            Call<ResponseBody> upload = form.createUser(finalRb);
            upload.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    Log.d(TAG, "onResponse: SUCCESSFULL"+response.body());
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Log.d(TAG, "onFailure: FAILED"+t.toString());
                }
            });
        }

    }
}