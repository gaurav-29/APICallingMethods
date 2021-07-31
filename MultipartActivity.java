package com.example.getwithparam.PostWithMultipart;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.getwithparam.Model.MultipartModel;
import com.example.getwithparam.R;
import com.example.getwithparam.Utils.GeneralAPIClient;
import com.example.getwithparam.Utils.GeneralAPIInterface;
import com.example.getwithparam.databinding.ActivityMultipartBinding;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import droidninja.filepicker.FilePickerBuilder;
import droidninja.filepicker.FilePickerConst;
import droidninja.filepicker.utils.ContentUriUtils;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.os.Build.VERSION.SDK_INT;

public class MultipartActivity extends AppCompatActivity {

    ActivityMultipartBinding mBinding;
    MultipartActivity ctx = MultipartActivity.this;
    GeneralAPIInterface loginApiInterface;
    String BASE_URL = "http://api.cssolution.in/";
    File file;
    ArrayList<Uri> photoPaths = new ArrayList<>();

    private final int IMAGE_PERMISSION_CODE = 1;
    private final int VIDEO_PERMISSION_CODE = 2;
    private final int AUDIO_PERMISSION_CODE = 3;
    private final int FILE_PERMISSION_CODE = 4;
    private final int PERMISSION_GRANTED_CODE = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(ctx, R.layout.activity_multipart);

    }

    private boolean isInternetConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo wifiConn = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mobileConn = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        if ((wifiConn != null && wifiConn.isConnected()) || (mobileConn != null && mobileConn.isConnected())) {
            return true;
        } else {
            return false;
        }
    }

    public void onClickPickImage(View view) {

        Dexter.withContext(ctx)
                .withPermissions(CAMERA, READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                        if (multiplePermissionsReport.areAllPermissionsGranted()) {
                            if (SDK_INT >= Build.VERSION_CODES.R)
                                if (Environment.isExternalStorageManager()) {
                                    imagePicker();
                                } else {
                                    try {
                                        Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                                        intent.addCategory("android.intent.category.DEFAULT");
                                        intent.setData(Uri.parse(String.format("package:%s", new Object[]{getApplicationContext().getPackageName()})));
                                        startActivityForResult(intent, 2000);
                                    } catch (Exception e) {
                                        Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                                        startActivityForResult(intent, 2000);
                                    }
                                }
                            else {
                                imagePicker();
                            }
                        }
                        if (multiplePermissionsReport.isAnyPermissionPermanentlyDenied()) {
                            showRationaleDialog();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                }).check();
    }

    private void imagePicker() {
        FilePickerBuilder.getInstance()
                .setMaxCount(5) //optional
//                .setSelectedFiles(filePaths) //optional
                .setActivityTheme(R.style.LibAppTheme) //optional
                .pickPhoto(this, 1);
    }

    private void showRationaleDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        builder.setTitle("Grant Permission");
        builder.setMessage("Permission is required to access images, files, audios & videos from this device");
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                openSettings();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    private void openSettings() {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", ctx.getPackageName(), null);
        intent.setData(uri);
        ctx.startActivityForResult(intent, PERMISSION_GRANTED_CODE);
    }

    public void onClickUploadFile(View view) {

        if (photoPaths.size() > 0) {
            GetResponse();
        } else {
            Toast.makeText(ctx, "Please select any Image before upload", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {

            photoPaths = new ArrayList<>();

            photoPaths.addAll(data.getParcelableArrayListExtra(FilePickerConst.KEY_SELECTED_MEDIA));

            if (photoPaths.size() > 0) {

                Uri uri = photoPaths.get(0);

                Log.d("PHOTOPATHS", photoPaths.toString());

                String path = ContentUriUtils.INSTANCE.getFilePath(ctx, uri);

                mBinding.tvPath.setText(path);

                Glide.with(ctx).load(path).placeholder(R.drawable.placeholder).centerCrop().into(mBinding.ivImage);
            }
        }
    }

    private void GetResponse() {

        mBinding.progressBar.getIndeterminateDrawable();
        mBinding.progressBar.setVisibility(View.VISIBLE);

        if (isInternetConnected()) {

            String path1 = mBinding.tvPath.getText().toString();

            file = new File(path1);

            RequestBody requestBody = RequestBody.create(MediaType.parse("image/*"), file);

            // First parameter in below line ("avatar") must be same as parameter name given with API.
            MultipartBody.Part parts = MultipartBody.Part.createFormData("avatar", file.getName(), requestBody);

            loginApiInterface = GeneralAPIClient.getClient(BASE_URL).create(GeneralAPIInterface.class);

            Call<MultipartModel> call2 = loginApiInterface.getStatus3(parts);

            call2.enqueue(new Callback<MultipartModel>() {
                @Override
                public void onResponse(Call<MultipartModel> call, Response<MultipartModel> response) {

                    Log.d("SHIV", response.body().toString());

                    if (response.code() == 200) {
                        Toast.makeText(ctx, response.body().message, Toast.LENGTH_SHORT).show();

                        MultipartModel res = response.body();

                    } else {
                        Toast.makeText(ctx, "Response not received successfully", Toast.LENGTH_LONG).show();
                    }
                    mBinding.progressBar.setVisibility(View.GONE);
                }

                @Override
                public void onFailure(Call<MultipartModel> call, Throwable t) {

                    Toast.makeText(ctx, "Something went wrong", Toast.LENGTH_LONG).show();
                    Log.d("ERROR", t.getMessage());
                    mBinding.progressBar.setVisibility(View.GONE);
                }
            });
        } else {
            Toast.makeText(ctx, "No Internet Connection", Toast.LENGTH_LONG).show();
            mBinding.progressBar.setVisibility(View.GONE);
        }
    }
}
