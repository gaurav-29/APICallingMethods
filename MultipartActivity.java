package com.example.getwithparam.PostWithMultipart;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.getwithparam.GetWithDirectUrl.LoginActivity;
import com.example.getwithparam.GetWithParam.RegisterActivity;
import com.example.getwithparam.Model.GeneralModel;
import com.example.getwithparam.Model.MultipartModel;
import com.example.getwithparam.R;
import com.example.getwithparam.Utils.GeneralAPIClient;
import com.example.getwithparam.Utils.GeneralAPIInterface;
import com.example.getwithparam.databinding.ActivityMultipartBinding;

import java.io.File;
import java.util.ArrayList;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MultipartActivity extends AppCompatActivity {

    ActivityMultipartBinding mBinding;
    MultipartActivity ctx = MultipartActivity.this;
    GeneralAPIInterface loginApiInterface;
    String BASE_URL = "http://api.cssolution.in/";
    File file;

    private final int STORAGE_PERMISSION_CODE = 1;
    private final int PERMISSION_GRANTED_CODE = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(ctx, R.layout.activity_multipart);

        isInternetConnected();
        onClickListner();
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

    private void onClickListner() {
        mBinding.tvSelectFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if ((ContextCompat.checkSelfPermission(ctx, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
                        && (ContextCompat.checkSelfPermission(ctx, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)) {
                    Toast.makeText(ctx, "You have already granted this permission!", Toast.LENGTH_SHORT).show();

                    openFileFolder();
                } else {
                    requestStoragePermission();
                }
            }
        });

        mBinding.tvUploadFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                GetResponse();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 15 && resultCode == RESULT_OK) {
            String path = data.getData().getPath();
            mBinding.tvPath.setText(path);
        }

        if (requestCode == PERMISSION_GRANTED_CODE && resultCode == RESULT_OK) {
            Intent intent2 = new Intent(ctx, MultipartActivity.class);
            startActivity(intent2);
        }
    }

    private void GetResponse() {

        mBinding.progressBar.getIndeterminateDrawable();
        mBinding.progressBar.setVisibility(View.VISIBLE);

        if (isInternetConnected()) {

            String path1 = mBinding.tvPath.getText().toString();
            if (file == null) {
                file = new File(path1);
            }

            RequestBody requestBody = RequestBody.create(MediaType.parse("image/*"), file);
            MultipartBody.Part parts = MultipartBody.Part.createFormData("newimage", file.getName(), requestBody);

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
        }
    }

    private void requestStoragePermission() {
        if ((ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE))
                && (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE))) {
            new AlertDialog.Builder(this)
                    .setTitle("Permission needed")
                    .setMessage("This permission is needed to Read and Write on File of your device")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            redirectToSettings();
//                            ActivityCompat.requestPermissions(ctx,
//                                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .create().show();

        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission GRANTED", Toast.LENGTH_SHORT).show();
                openFileFolder();
            } else {
                Toast.makeText(this, "Permission DENIED", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void redirectToSettings() {

        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", ctx.getPackageName(), null);
        intent.setData(uri);
        ctx.startActivityForResult(intent, PERMISSION_GRANTED_CODE);
    }

    public void openFileFolder() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        startActivityForResult(intent, 15);
    }
}