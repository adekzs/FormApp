package com.adeks.formapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.adeks.formapp.model.ResponseResult;
import com.adeks.formapp.model.User;
import com.adeks.formapp.retrofit.FormService;
import com.adeks.formapp.retrofit.RetrofitClientClass;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class MainActivity extends AppCompatActivity implements  ProcessResult{
    public static final int REQUEST_CODE_STORAGE_PERMISSION = 1;
    public static final int REQUEST_CODE_SELECT_IMAGE = 2;
    private static final String TAG = "MainActivity";
    public static final int CAMERA_PERM_CODE = 101;
    public static final int CAMERA_REQ_CODE = 102;
    public static final int WRITE_REQUEST_CODE = 104;

    private ImageView mImageView;
    private File mSelectedFile;
    private User user;

    private ProgressBar mProgressBar;
    TextInputLayout firstName, lastName, email, phone, pass, confPass, address, service, state, lga, description, price, avail, location, duration;
    private String currentPhotoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mImageView = findViewById(R.id.imagev);
        getEachFieldId();
        mProgressBar = findViewById(R.id.progressBar);

        mImageView.setOnClickListener(v -> {
//            askStoragePermission();
            askCameraPermission();
        });
    }

    private void askStoragePermission() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    MainActivity.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_CODE_STORAGE_PERMISSION
            );
        } else {
            selectImage();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE_STORAGE_PERMISSION && grantResults.length >= 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selectImage();
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_LONG).show();
            }
            return;
        }
        if (requestCode == CAMERA_PERM_CODE && grantResults.length >= 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                dispatchTakePictureIntent();
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_LONG).show();
            }
            return;
        }
        if (requestCode == WRITE_REQUEST_CODE && grantResults.length >= 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mSelectedFile = saveBitmapToFile(mSelectedFile);
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SELECT_IMAGE && resultCode == RESULT_OK) {
            if (data != null) {
                Uri selectedImageUri = data.getData();
                if (selectedImageUri != null) {
                    try {
                        InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        bitmap = getResizedBitmap(bitmap, 200, 200);
                        mImageView.setImageBitmap(bitmap);
                        mSelectedFile = new File(getPathFromUri(selectedImageUri));
                        Log.d(TAG, "onActivityResult: FILE SIZE " + mSelectedFile.length());
                        mSelectedFile = saveBitmapToFile(mSelectedFile);
                        Log.d(TAG, "onActivityResult: FILE SIZE  2 " + mSelectedFile.length());

                    } catch (Exception ex) {
                        Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
                        Log.e(TAG, "onActivityResult: Error",ex.fillInStackTrace() );
                    }
                }
            }
            return;
        }

        if (requestCode == CAMERA_REQ_CODE && resultCode == RESULT_OK) {
            File f = new File(currentPhotoPath);
            mSelectedFile = saveBitmapToFile(f);
            try {
                InputStream inputStream = getContentResolver().openInputStream(Uri.fromFile(mSelectedFile));
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
//                bitmap = getResizedBitmap(bitmap, 200, 200);
                mImageView.setImageBitmap(bitmap);
            } catch (Exception ex) {
                Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
            }
//            mImageView.setImageURI(Uri.fromFile(f));
//            mSelectedFile = f;
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, CAMERA_REQ_CODE);
            }
        }
    }

    private void askCameraPermission() {
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERM_CODE);
        } else {
            dispatchTakePictureIntent();
        }
    }


    public static Bitmap getResizedBitmap(Bitmap bm, int newHeight, int newWidth) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);
        // RECREATE THE NEW BITMAP
        return Bitmap.createBitmap(bm, 0, 0, width, height,
                matrix, false);

    }

    private String getPathFromUri(Uri contentUri) {
        String filePath;
        Cursor cursor = getContentResolver().
                query(contentUri, null, null, null, null);
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
            Toast.makeText(this, "You need to select a file", Toast.LENGTH_LONG).show();
        } else if (!validateAllFields()) {
            Toast.makeText(this, "Wrong input", Toast.LENGTH_SHORT).show();
        } else {
            getEachTextVal();
            user.setFile(mSelectedFile);
            performSendRequest(user,this);
        }

    }

    private void performSendRequest(User user, ProcessResult processResult) {
        Retrofit retrofit = RetrofitClientClass.getRetrofitInstance();
        FormService form = retrofit.create(FormService.class);

        MultipartBody.Builder builder = new MultipartBody.Builder();
        builder.setType(MultipartBody.FORM);
        builder.addFormDataPart("address", user.getAddress());
        builder.addFormDataPart("availability", user.getAvailability());
        builder.addFormDataPart("description", user.getDescription());
        builder.addFormDataPart("duration", user.getDuration());
        builder.addFormDataPart("email", user.getEmail());
        builder.addFormDataPart("first_name", user.getFirst_name());
        RequestBody requestBody = RequestBody.create(mSelectedFile, MediaType.parse("multipart/form-data"));
        builder.addFormDataPart("image", mSelectedFile.getName(), requestBody);
        builder.addFormDataPart("last_name", user.getLast_Name());
        builder.addFormDataPart("lga", user.getLga());
        builder.addFormDataPart("location", user.getLocation());
        builder.addFormDataPart("password", user.getPassword());
        builder.addFormDataPart("password_confirmation", user.getPassword_confirmation());
        builder.addFormDataPart("phone", user.getPhone());
        builder.addFormDataPart("price", user.getPrice());
        builder.addFormDataPart("service", user.getService());
        builder.addFormDataPart("state", user.getState());
        RequestBody finalRb = builder.build();


        Call<ResponseResult> upload = form.createUser(finalRb);
        upload.enqueue(new Callback<ResponseResult>() {
            @Override
            public void onResponse(Call<ResponseResult> call, Response<ResponseResult> response) {
                if (response.isSuccessful()) {
                    if (response.code() == 200) {
                        ResponseResult resp = response.body();
                        String token = resp.getToken();
                        String workerId = resp.getWorker().getWorkerId();
                        Log.d(TAG, "onResponse: TOKEN:" + token +"WORKER_ID: "+ workerId);
                        processResult.onProcessCompleted();
                    }
                }else  {
                        if (response.errorBody() != null) {
                            try {
                                String errorMessage = response.errorBody().string();
                                JsonParser parser = new JsonParser();
                                Object obj = parser.parse(errorMessage);
                                JsonObject message = (JsonObject) obj;
                                JsonObject error = message.getAsJsonObject("errors");
                                Log.d(TAG, "onResponse: Errors av" + error.toString());
                                outputErrorOnEachField(error);
                                Log.d(TAG, "onResponse: Message" + errorMessage);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        processResult.onProcessFailed("Error");
                    }
                    Log.d(TAG, "onResponse: SUCCESSFUL" + response.body());
                }


            @Override
            public void onFailure(Call<ResponseResult> call, Throwable t) {
                processResult.onProcessFailed(t.toString());
                Log.d(TAG, "onFailure: FAILED" + t.toString());
            }
        });
        showLoadingIcon();
    }
    private  void checkEachField(JsonObject error, String fieldName, TextInputLayout layout){
        if (error.has(fieldName)) {
            StringBuilder error_m = new StringBuilder();
            JsonArray array = error.getAsJsonArray(fieldName);
            for (int i = 0; i < array.size(); i++) {
                error_m.append(array.get(i).toString()).append(" ");
            }
            layout.setError(error_m.toString());
        }
    }
    private void outputErrorOnEachField(JsonObject error) {
       checkEachField(error,"first_name",firstName);
        checkEachField(error,"last_name",lastName);
        checkEachField(error,"phone",phone);
        checkEachField(error,"email",email);
        checkEachField(error,"password",pass);
        checkEachField(error,"password_confirmation",confPass);
        checkEachField(error,"address",address);
        checkEachField(error,"service",service);
        checkEachField(error,"state",state);
        checkEachField(error,"lga",lga);
        checkEachField(error,"description",description);
        checkEachField(error,"price",price);
        checkEachField(error,"availability",avail);
        checkEachField(error,"location",location);
        checkEachField(error,"duration",duration);

        if (error.has("image")) {
            StringBuilder error_m = new StringBuilder();
            JsonArray array = error.getAsJsonArray("image");
            for (int i = 0; i < array.size(); i++) {
                error_m.append(array.get(i).toString()).append(" ");
            }
            Toast.makeText(this, error_m.toString(), Toast.LENGTH_LONG).show();
        }
    }

    public File saveBitmapToFile(File file){
        try {
            // BitmapFactory options to downsize the image
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            o.inSampleSize = 6;
            // factor of downsizing the image

            FileInputStream inputStream = new FileInputStream(file);
            //Bitmap selectedBitmap = null;
            BitmapFactory.decodeStream(inputStream, null, o);
            inputStream.close();

            // The new size we want to scale to
            final int REQUIRED_SIZE=75;

            // Find the correct scale value. It should be the power of 2.
            int scale = 1;
            while(o.outWidth / scale / 2 >= REQUIRED_SIZE &&
                    o.outHeight / scale / 2 >= REQUIRED_SIZE) {
                scale *= 2;
            }

            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            inputStream = new FileInputStream(file);

            Bitmap selectedBitmap = BitmapFactory.decodeStream(inputStream, null, o2);
            inputStream.close();

            // here i override the original image file
            file.createNewFile();
            FileOutputStream outputStream = new FileOutputStream(file);
            if ( ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                selectedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            } else {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_REQUEST_CODE);

            }
            return file;
        } catch (Exception e) {
            return null;
        }
    }

    private void showLoadingIcon() {
        mProgressBar.setVisibility(View.VISIBLE);
    }

    private boolean validateAllFields() {
        ArrayList<TextInputLayout> list = new ArrayList<>();
        list.add(firstName);
        list.add(lastName);
        list.add(address);
        list.add(service);
        list.add(state);
        list.add(lga);
        list.add(description);
        list.add(price);
        list.add(avail);
        list.add(duration);
        list.add(email);
        list.add(phone);
        list.add(pass);
        list.add(confPass);
        list.add(location);
        return checkRemainingFields(list) && validateEmail() && validatePass()
                && validateConfPass() && validatePhone();
    }

    private Boolean validatePhone() {
        String val = getString(phone);
        //remove leading 234 || +234
        if (val.startsWith("234")) {
            if (val.length() != 13) {
                phone.setError("Incorrect number input");
                return false;
            }
        } else if (val.startsWith("0")) {
            if (val.length() != 11) {
                phone.setError("Incorrect number input");
                return false;
            }
        }
        phone.setError(null);
        phone.setErrorEnabled(false);
        return true;
    }

    private Boolean validatePass() {
        String val = getString(pass);

        if (val.isEmpty()) {
            pass.setError("Field Cannot be Empty");
            return false;
        } else {
            pass.setError(null);
            pass.setErrorEnabled(false);
            return true;
        }
    }

    private Boolean validateConfPass() {
        String val = getString(confPass);

        if (val.isEmpty()) {
            confPass.setError("Field Cannot be Empty");
            return false;
        } else if (!val.equals(getString(pass))) {
            confPass.setError("Password and Confirmation password must be equal");
            return false;
        } else {
            confPass.setError(null);
            confPass.setErrorEnabled(false);
            return true;
        }
    }

    private Boolean validateEmail() {
        String val = getString(email);
        String regx = "(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"" +
                "(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*" +
                "\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|" +
                "[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\" +
                "x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])";

        if (val.isEmpty()) {
            email.setError("Field Cannot be Empty");
            return false;
        } else if (!val.matches(regx)) {
            email.setError("Invalid email Address");
            return false;
        } else {
            email.setError(null);
            email.setErrorEnabled(false);
            return true;
        }
    }

    private boolean checkRemainingFields(@NotNull ArrayList<TextInputLayout> layouts) {
        boolean isError = false;
        for (TextInputLayout layout : layouts) {
            String val = getString(layout);
            if (val.isEmpty()) {
                layout.setError("Field Cannot be Empty");
                isError = true;
            } else {
                layout.setError(null);
                layout.setErrorEnabled(false);
            }
        }
        return !isError;
    }

    private void getEachTextVal() {
        user = new User(getString(firstName), getString(lastName), getString(phone), getString(pass), getString(confPass), getString(address),
                getString(service), getString(state), null, getString(lga), getString(email), getString(description), getString(price),
                getString(avail), getString(location), getString(duration));
    }

    private String getString(@NotNull TextInputLayout input) {
        return input.getEditText().getText().toString();
    }

    private void getEachFieldId() {
        firstName = findViewById(R.id.first_name);
        lastName = findViewById(R.id.last_name);
        phone = findViewById(R.id.phone);
        email = findViewById(R.id.email);
        pass = findViewById(R.id.password);
        confPass = findViewById(R.id.confirm_password);
        address = findViewById(R.id.address);
        service = findViewById(R.id.service);
        state = findViewById(R.id.state);
        lga = findViewById(R.id.lga);
        description = findViewById(R.id.description);
        price = findViewById(R.id.price);
        avail = findViewById(R.id.availablity);
        location = findViewById(R.id.location);
        duration = findViewById(R.id.duration);
    }


    @Override
    public void onProcessCompleted() {
        removeLoadingIcon();
    }

    private void removeLoadingIcon() {
        mProgressBar.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onProcessFailed(String t) {
        removeLoadingIcon();
        Toast.makeText(this, "Connection Timed out", Toast.LENGTH_SHORT).show();
    }
}