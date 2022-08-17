package org.hse.android;

import android.app.Application;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.gson.Gson;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class MainViewModel extends AndroidViewModel {
    private HseRepository repository;
    private MutableLiveData<Date> currentTime = new MutableLiveData<>();

    public MutableLiveData<Date> getCurrentTime() {
        return currentTime;
    }

    public MainViewModel(@NonNull Application application){
        super(application);
        repository = new HseRepository(application);
    }

    public LiveData<List<GroupEntity>> getGroups() { return repository.getGroups(); }
    public LiveData<List<TeacherEntity>> getTeachers() { return repository.getTeachers(); }
    public LiveData<List<TimeTableWithTeacherEntity>> getTimeTableTeacherByDate(Date date) {
        return repository.getTimeTableTeacherByDate(date);
    }
    public LiveData<List<TimeTableWithTeacherEntity>> getTimeTableByTeacher(Integer teacherId) {
        return repository.getTimeTableByTeacher(teacherId);
    }
    public LiveData<List<TimeTableWithTeacherEntity>> getTimeTableByGroup(Integer groupId) {
        return repository.getTimeTableByGroup(groupId);
    }
    public LiveData<List<TimeTableWithTeacherEntity>> getTimeTableTeacherByDateAndTeacher(Date date, Integer teacherId) {
        return repository.getTimeTableTeacherByDateAndTeacher(date, teacherId); }
    public LiveData<List<TimeTableWithTeacherEntity>> getTimeTableTeacherByDateAndGroup(Date date, Integer groupId) {
        return repository.getTimeTableTeacherByDateAndGroup(date, groupId); }

    public void getTime(){
        repository.getTime(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("TAG", e.toString());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                parseResponse(response);
            }
        });
    }


    private void parseResponse(@NonNull Response response){
        Gson gson = new Gson();
        ResponseBody body = response.body();
        try {
            if(body == null){
                return;
            }
            String string = body.string();
            TimeResponse timeResponse = gson.fromJson(string, TimeResponse.class);
            String currentTimeVal = timeResponse.getTimeZone().getCurrentTime();
            Date date = dateFromString(currentTimeVal);
            currentTime.postValue(date);
        } catch (Exception e) {
        }
    }
    private Date dateFromString(String val) {
        SimpleDateFormat simpleDateFormat =
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault());
        try {
            return simpleDateFormat.parse(val);
        }catch (ParseException e){
            //
        }
        return null;
    }
}
