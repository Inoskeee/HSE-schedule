package org.hse.android;

import androidx.annotation.NonNull;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class StudentActivity extends AppCompatActivity {

    //public TextView time;
    private static String TAG = "StudentActivity";
    public TextView status;
    public TextView subject;
    public TextView cabinet;
    public TextView corp;
    public TextView teacher;
    public TextView time;

    public Date currentTime;

    public Spinner spinner;

    public static List<Group> groups;

    private OkHttpClient client = new OkHttpClient();

    protected MainViewModel mainViewModel;
    protected ArrayAdapter<Group> adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);
        setContentView(R.layout.activity_student);

        spinner = findViewById(R.id.groupList);

        String[] direction = {"ПИ", "БИ"};
        Integer[] year = {19, 20, 21};
        Integer[] group = {1, 2, 3};

        groups = new ArrayList<>();
        initGroupList(groups);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, groups);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent,
                                       View itemSelected, int selectedItemPosition, long selectedId) {
                Object item = adapter.getItem(selectedItemPosition);
                Log.d("QQ", "selectedItem" + item);
                initTime();
            }
            public void onNothingSelected(AdapterView<?> parent){
                Log.d("QQ", "qq");
            }
        });

        time = findViewById(R.id.textForTime);
        initTime();

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

        //initData();
        View scheduleDay = findViewById(R.id.day_schedule);
        scheduleDay.setOnClickListener(v -> showSchedule(ScheduleType.DAY));
        View scheduleWeek = findViewById(R.id.week_schedule);
        scheduleWeek.setOnClickListener(v -> showSchedule(ScheduleType.WEEK));
    }



    private void initGroupList(List<Group> groups){
        mainViewModel.getGroups().observe(this, new Observer<List<GroupEntity>>() {
            @Override
            public void onChanged(List<GroupEntity> list) {
                List<Group> groupsResult = new ArrayList<>();
                for(GroupEntity listEntity : list) {
                    groupsResult.add(new Group(listEntity.id, listEntity.name));
                }
                Log.d("TAG", groupsResult.get(0).id.toString());
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

    private Group getSelectedGroup(){
        return new Group((int)spinner.getSelectedItemId()+1, spinner.getSelectedItem().toString());
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
        /*currentDateTime.observe(this, localDateTime -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                currentTime = dateFromString(localDateTime.format(DateTimeFormatter.ofPattern(getString(R.string.FullDateFormatter), Locale.getDefault())));
                Log.d("TT", currentTime.toString());
            }
        });*/

        mainViewModel.getTimeTableTeacherByDateAndGroup(currentTime, getSelectedGroup().getId()).observe(this, new Observer<List<TimeTableWithTeacherEntity>>() {
            @Override
            public void onChanged(List<TimeTableWithTeacherEntity> list) {
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

    public static class Group {
        private Integer id;
        private  String name;

        public Group(Integer id, String name){
            this.id = id;
            this.name = name;
        }

        public Integer getId(){
            return id;
        }
        public void setId(Integer id){
            this.id = id;
        }

        @NonNull
        @Override
        public String toString(){
            return name;
        }

        public String getName(){
            return name;
        }
        public void setName(String name){
            this.name = name;
        }
    }

    private void showSchedule(ScheduleType type){
        Object selectedItem = spinner.getSelectedItem();
        if(!(selectedItem instanceof StudentActivity.Group)){
            return;
        }
        showScheduleImpl(ScheduleMode.STUDENT, type, (StudentActivity.Group)selectedItem);
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