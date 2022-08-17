package org.hse.android;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.io.File;
import java.io.IOException;
import java.security.Permission;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class MainActivity extends AppCompatActivity {

    private static String TAG = "BaseActivity";
    public static final String URL = "https://api.ipgeolocation.io/ipgeo?apiKey=b03018f75ed94023a005637878ec0977";

    protected TextView time;
    protected Date currentTime;

    public static String myTime = "";

    private OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        Button student = findViewById(R.id.student_button);
        Button teacher = findViewById(R.id.teacher_button);
        Button settings = findViewById(R.id.settings_button);

        student.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showStudent();
            }
        });

        teacher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTeacher();
            }
        });

        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSettings();
            }
        });
    }

    private void showStudent() {
        Intent intent = new Intent(this, StudentActivity.class);
        startActivity(intent);
    }

    private  void showTeacher(){
        Intent intent = new Intent(this, TeacherActivity.class);
        startActivity(intent);
    }

    private  void showSettings(){
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    protected void getTime() {
        Request request = new Request.Builder().url(URL).build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "getTime", e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                parseResponse(response);
            }
        });
    }

    protected void initTime() {
        getTime();
    }

    protected void getCurrTime(){
        getTime();
    }

    private void showTime(Date dateTime){
        if(dateTime == null){
            return;
        }
        currentTime = dateTime;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm, EEEE", Locale.forLanguageTag("ru"));
        time.setText(simpleDateFormat.format(currentTime));
    }

    private void setTime(Date dateTime){
        if(dateTime == null){
            return;
        }
        currentTime = dateTime;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEEE, dd MMMM", Locale.forLanguageTag("ru"));
        myTime = simpleDateFormat.format(currentTime);
    }

    private void parseResponse(@NonNull Response response){
        Gson gson = new Gson();
        ResponseBody body = response.body();
        try {
            if(body == null){
                return;
            }
            String string = body.string();
            Log.d(TAG, string);
            TimeResponse timeResponse = gson.fromJson(string, TimeResponse.class);
            String currentTimeVal = timeResponse.getTimeZone().getCurrentTime();
            Log.d(TAG, currentTimeVal);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault());
            Date dateTime = simpleDateFormat.parse(currentTimeVal);
            runOnUiThread(() -> showTime(dateTime));
        } catch (Exception e) {
            Log.e(TAG, "", e);
        }
    }


}

