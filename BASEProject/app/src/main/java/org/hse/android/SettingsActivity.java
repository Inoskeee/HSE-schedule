package org.hse.android;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class SettingsActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor light;
    private TextView sensorLight;
    private TextView allSensors;

    private PreferenceManager preference;
    private EditText getName;
    private ImageView getAvatar;
    private Bitmap avatar;
    private final String key = "save_key";
    private final String keyImg = "save_img";

    private String PERMISSION_CAMERA = Manifest.permission.CAMERA;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        init();
        sensorLight = findViewById(R.id.light_level);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        light = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

        allSensors = findViewById(R.id.all_sensors);

        List<Sensor> sensors = sensorManager.getSensorList(1);

        String temp = "Доступные датчики:\n";
        for(int i = 0; i<sensors.size(); i++){
            temp+=sensors.get(i).getName()+";\n";
        }

        allSensors.setText(temp);

        getName.setText(preference.getValue(key,""));

        getAvatar = findViewById(R.id.user_avatar);
        getAvatar.setImageDrawable(Drawable.createFromPath(Environment.getExternalStorageDirectory().getAbsolutePath() + "/DCIM/Camera/image.jpg"));
        Button saveButton = findViewById(R.id.save_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onSaveClick();
            }
        });

        Button takePictureButton = findViewById(R.id.take_picture);

        takePictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkPermissions();
            }
        });

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        //...
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        float lux = sensorEvent.values[0];
        sensorLight.setText("Текущая освещенность: "+lux);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, light, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    private void init(){
        preference = new PreferenceManager(this);
        getName = findViewById(R.id.get_name);
        getAvatar = findViewById(R.id.user_avatar);
    }

    public void onSaveClick(){

        if(avatar != null){
            File root = Environment.getExternalStorageDirectory();
            File cachePath = new File(root.getAbsolutePath() + "/DCIM/Camera/image.jpg");
            Log.e("TAG", cachePath.getAbsolutePath());
            try {
                cachePath.createNewFile();
                FileOutputStream ostream = new FileOutputStream(cachePath);
                avatar.compress(Bitmap.CompressFormat.JPEG, 100, ostream);
                ostream.flush();
                ostream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        preference.saveValue(key,getName.getText().toString());
    }

    public void checkPermissions(){
        int permissionCamera = ContextCompat.checkSelfPermission(this, PERMISSION_CAMERA);
        if (permissionCamera != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, PERMISSION_CAMERA)) {
                Toast.makeText(getApplicationContext(),"Необходимо выдать доступ к камере устройства.",Toast.LENGTH_LONG).show();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{PERMISSION_CAMERA}, 1);
            }
        } else {
            checkPermissionsFiles();
            dispatchTakePictureIntent();
        }
    }

    public void checkPermissionsFiles(){
        int permissionCamera = ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET);
        if (permissionCamera != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.INTERNET)) {
                Toast.makeText(getApplicationContext(),"Необходимо выдать доступ к камере устройства.",Toast.LENGTH_LONG).show();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET}, 1);
            }
        } else {
            dispatchTakePictureIntent();
        }
    }

    private void dispatchTakePictureIntent() {
        Log.i("CAMERA","dispatchTakePictureIntent entered: ");
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        someActivityResultLauncher.launch(takePictureIntent);
    }

    ActivityResultLauncher<Intent> someActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // There are no request codes
                        avatar = (Bitmap)result.getData().getExtras().get("data");
                        getAvatar = findViewById(R.id.user_avatar);
                        getAvatar.setImageBitmap(avatar);
                    }
                }
            });
}