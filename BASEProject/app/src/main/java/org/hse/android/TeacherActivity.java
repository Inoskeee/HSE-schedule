package org.hse.android;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.gson.Gson;

import java.io.IOException;
import java.sql.Time;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class TeacherActivity extends AppCompatActivity {

    //public TextView time;
    private static String TAG = "TeacherActivity";
    public TextView status;
    public TextView subject;
    public TextView cabinet;
    public TextView corp;
    public TextView teacher;
    public TextView time;

    public Date currentTime;

    public Spinner spinner;

    public static List<StudentActivity.Group> groups;

    private OkHttpClient client = new OkHttpClient();

    protected MainViewModel mainViewModel;
    protected ArrayAdapter<StudentActivity.Group> adapter;

    protected MutableLiveData<LocalDateTime> currentDateTime = new MutableLiveData<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);

        setContentView(R.layout.activity_teacher);

        spinner = findViewById(R.id.groupList);

        groups = new ArrayList<>();
        initGroupList(groups);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, groups);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent,
                                    View itemSelected, int selectedItemPosition, long selectedId) {
                Object item = adapter.getItem(selectedItemPosition);
                Log.d("TAG", "selectedItem" + item);
                initTime();
            }
            public void onNothingSelected(AdapterView<?> parent){
                //
            }
        });

        time = findViewById(R.id.textForTime);

        mainViewModel.getCurrentTime().observe(this, date -> {
            SimpleDateFormat simpleDateFormat =
                    new SimpleDateFormat(getString(R.string.HourMinuteWeekday), new Locale("RU"));
            currentTime = date;
            time.setText(simpleDateFormat.format(date));
            showTime();
        });

        status = findViewById(R.id.status);
        subject = findViewById(R.id.subject);
        cabinet = findViewById(R.id.cabinet);
        corp = findViewById(R.id.corp);
        teacher = findViewById(R.id.teacher);

        View scheduleDay = findViewById(R.id.day_schedule);
        scheduleDay.setOnClickListener(v -> showSchedule(ScheduleType.DAY));
        View scheduleWeek = findViewById(R.id.week_schedule);
        scheduleWeek.setOnClickListener(v -> showSchedule(ScheduleType.WEEK));
    }

    private void initGroupList(List<StudentActivity.Group> groups){
        mainViewModel.getTeachers().observe(this, new Observer<List<TeacherEntity>>() {
            @Override
            public void onChanged(List<TeacherEntity> list) {
                List<StudentActivity.Group> groupsResult = new ArrayList<>();
                for(TeacherEntity listEntity : list) {
                    groupsResult.add(new StudentActivity.Group(listEntity.id, listEntity.fio));
                }
                adapter.clear();
                adapter.addAll(groupsResult);
            }
        });
    }

    protected void initTime() {
        getTime();
    }

    protected void getTime() {
        mainViewModel.getTime();
    }

    private StudentActivity.Group getSelectedGroup(){
        return new StudentActivity.Group((int)spinner.getSelectedItemId()+1, spinner.getSelectedItem().toString());
    }
    private Date dateFromString(String val) {
        SimpleDateFormat simpleDateFormat =
                new SimpleDateFormat(getString(R.string.LongDateFormat), Locale.getDefault());
        try {
            return simpleDateFormat.parse(val);
        }catch (ParseException e){
            //
        }
        return null;
    }


    protected void showTime(){
        mainViewModel.getTimeTableTeacherByDateAndTeacher(currentTime, getSelectedGroup().getId()).observe(this, new Observer<List<TimeTableWithTeacherEntity>>() {
            @Override
            public void onChanged(List<TimeTableWithTeacherEntity> list) {
                Log.d("TAG", list.toString());
                if(!list.isEmpty()){
                    for (TimeTableWithTeacherEntity listEntity : list){
                        Log.d("TAG", listEntity.timeTableEntity.subjName);
                        initDataFromTimeTable(listEntity);
                    }
                }
                else{
                    initDataFromTimeTable(null);
                }
            }
        });
    }

    private void initDataFromTimeTable(TimeTableWithTeacherEntity timeTableTeacherEntity){
        if(timeTableTeacherEntity == null){
            status.setText(R.string.NoLesson);

            subject.setText(R.string.ObjectLesson);
            cabinet.setText(R.string.Cabinet);
            corp.setText(R.string.Corp);
            teacher.setText(R.string.Teacher);
            return;
        }
        status.setText(R.string.LessonNow);
        TimeTableEntity timeTableEntity = timeTableTeacherEntity.timeTableEntity;

        subject.setText(timeTableEntity.subjName);
        cabinet.setText(timeTableEntity.cabinet);
        corp.setText(timeTableEntity.corp);
        teacher.setText(timeTableTeacherEntity.teacherEntity.fio);
    }
    private void initData(){
        initDataFromTimeTable(null);
    }

    private void showSchedule(ScheduleType type){
        Object selectedItem = spinner.getSelectedItem();
        if(!(selectedItem instanceof StudentActivity.Group)){
            return;
        }
        showScheduleImpl(ScheduleMode.TEACHER, type, (StudentActivity.Group)selectedItem);
    }

    protected void showScheduleImpl(ScheduleMode mode, ScheduleType type, StudentActivity.Group group){
        Intent intent = new Intent(this, ScheduleActivity.class);
        intent.putExtra(ScheduleActivity.ARG_ID, group.getId());
        intent.putExtra(ScheduleActivity.ARG_TYPE, type);
        intent.putExtra(ScheduleActivity.ARG_MODE, mode);
        intent.putExtra(ScheduleActivity.ARG_DATE, currentTime);
        startActivity(intent);
    }
}