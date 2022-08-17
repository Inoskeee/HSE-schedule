package org.hse.android;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.type.DateTime;

import java.net.MulticastSocket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ScheduleActivity extends MainActivity {

    public final static String ARG_ID = "id";
    public final static String ARG_TYPE = "type";
    public final static String ARG_MODE = "mode";
    public final static Integer DEFAULT_ID = 0;
    public final static String ARG_DATE = "date";

    protected ScheduleType type;
    protected ScheduleMode mode;
    protected Integer id;

    protected TextView title;
    protected TextView secondTitle;
    protected RecyclerView recyclerView;
    protected ItemAdapter adapter;
    protected Date dateTime;

    protected MainViewModel mainViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);

        setContentView(R.layout.activity_schedule);
        type = (ScheduleType) getIntent().getSerializableExtra(ARG_TYPE);
        mode = (ScheduleMode) getIntent().getSerializableExtra(ARG_MODE);
        id = getIntent().getIntExtra(ARG_ID, DEFAULT_ID);
        dateTime = (Date)getIntent().getSerializableExtra(ARG_DATE);

        title = findViewById(R.id.title);
        secondTitle = findViewById(R.id.secondHeader);
        recyclerView = findViewById(R.id.listView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        adapter = new ItemAdapter(this::onScheduleItemClick);
        recyclerView.setAdapter(adapter);

        initData();
    }

    private void onScheduleItemClick(ScheduleItem scheduleItem) {
        Toast toast = Toast.makeText(this, scheduleItem.toString(), Toast.LENGTH_SHORT);
        toast.show();
    }

    private void initData(){
        List<ScheduleItem> tableList = new ArrayList<>();

        if(mode == ScheduleMode.STUDENT){
            secondTitle.setText(StudentActivity.groups.get(id.intValue()-1).getName());
        }
        else if(mode == ScheduleMode.TEACHER){
            secondTitle.setText(TeacherActivity.groups.get(id.intValue()-1).getName());
        }


        FilterItem(tableList);

    }

    public void FilterItem(List<ScheduleItem> tableList){
        if(mode == ScheduleMode.STUDENT){
            if(type == ScheduleType.DAY){
                mainViewModel.getTimeTableByGroup(id).observe(this, new Observer<List<TimeTableWithTeacherEntity>>() {
                    @Override
                    public void onChanged(List<TimeTableWithTeacherEntity> list) {
                        for (TimeTableWithTeacherEntity listEntity : list){
                            Calendar cal = Calendar.getInstance();
                            cal.setTime(listEntity.timeTableEntity.timeStart);
                            Integer listDate = cal.get(Calendar.DATE);
                            cal.setTime(dateTime);
                            Integer currDate = cal.get(Calendar.DATE);
                            if(listDate.equals(currDate)){
                                SimpleDateFormat simpleDateFormat = new SimpleDateFormat(getString(R.string.DateWeekFormat), Locale.forLanguageTag("ru"));
                                String myTime = simpleDateFormat.format(listEntity.timeTableEntity.timeStart);
                                ScheduleItemHeader header = new ScheduleItemHeader();
                                header.setTitle(myTime);
                                Integer counter = 0;
                                for(ScheduleItem headItem : tableList){
                                    if(headItem.getClass() == ScheduleItemHeader.class){
                                        if(((ScheduleItemHeader)headItem).getTitle().equalsIgnoreCase(header.getTitle())){
                                            counter += 1;
                                        }
                                    }
                                }
                                if(counter == 0){
                                    tableList.add(header);
                                }

                                ScheduleItem item = new ScheduleItem();
                                SimpleDateFormat simpleFormat = new SimpleDateFormat(getString(R.string.HourMinuteFormat), Locale.forLanguageTag("ru"));
                                item.setStart(simpleFormat.format(listEntity.timeTableEntity.timeStart));
                                item.setEnd(simpleFormat.format(listEntity.timeTableEntity.timeEnd));
                                if(listEntity.timeTableEntity.type == 0){
                                    item.setType(getString(R.string.Lection));
                                }
                                else {
                                    item.setType(getString(R.string.PracticeLesson));
                                }
                                item.setName(listEntity.timeTableEntity.subjName);
                                item.setPlace(listEntity.timeTableEntity.corp + ", "+listEntity.timeTableEntity.cabinet);
                                item.setTeacher(listEntity.teacherEntity.fio);
                                tableList.add(item);
                            }
                        }
                        adapter.setDataList(tableList);
                    }
                });
            }
            else if(type == ScheduleType.WEEK){
                mainViewModel.getTimeTableByGroup(id).observe(this, new Observer<List<TimeTableWithTeacherEntity>>() {
                    @Override
                    public void onChanged(List<TimeTableWithTeacherEntity> list) {
                        for (TimeTableWithTeacherEntity listEntity : list){
                            Date lastWeekday = GetLastWeekDay();
                            Calendar cal = Calendar.getInstance();
                            cal.setTime(listEntity.timeTableEntity.timeStart);
                            Integer listDate = cal.get(Calendar.DATE);
                            cal.setTime(dateTime);
                            Integer currDate = cal.get(Calendar.DATE);
                            cal.setTime(lastWeekday);
                            Integer lastWeek = cal.get(Calendar.DATE);

                            if(listDate >= currDate && listDate <= lastWeek){
                                SimpleDateFormat simpleDateFormat = new SimpleDateFormat(getString(R.string.DateWeekFormat), Locale.forLanguageTag("ru"));
                                String myTime = simpleDateFormat.format(listEntity.timeTableEntity.timeStart);
                                ScheduleItemHeader header = new ScheduleItemHeader();
                                header.setTitle(myTime);
                                Integer counter = 0;
                                for(ScheduleItem headItem : tableList){
                                    if(headItem instanceof ScheduleItemHeader){
                                        if(((ScheduleItemHeader)headItem).getTitle().equalsIgnoreCase(header.getTitle())){
                                            counter += 1;
                                        }
                                    }
                                }
                                if(counter == 0){
                                    tableList.add(header);
                                }
                                ScheduleItem item = new ScheduleItem();
                                SimpleDateFormat simpleFormat = new SimpleDateFormat(getString(R.string.HourMinuteFormat), Locale.forLanguageTag("ru"));
                                item.setStart(simpleFormat.format(listEntity.timeTableEntity.timeStart));
                                item.setEnd(simpleFormat.format(listEntity.timeTableEntity.timeEnd));
                                if(listEntity.timeTableEntity.type == 0){
                                    item.setType(getString(R.string.Lection));
                                }
                                else {
                                    item.setType(getString(R.string.PracticeLesson));
                                }
                                item.setName(listEntity.timeTableEntity.subjName);
                                item.setPlace(listEntity.timeTableEntity.corp + ", "+listEntity.timeTableEntity.cabinet);
                                item.setTeacher(listEntity.teacherEntity.fio);
                                tableList.add(item);
                            }
                        }
                        adapter.setDataList(tableList);
                    }
                });
            }
        }
        else if(mode == ScheduleMode.TEACHER){
            if(type == ScheduleType.DAY){
                mainViewModel.getTimeTableByTeacher(id).observe(this, new Observer<List<TimeTableWithTeacherEntity>>() {
                    @Override
                    public void onChanged(List<TimeTableWithTeacherEntity> list) {
                        for (TimeTableWithTeacherEntity listEntity : list){
                            Calendar cal = Calendar.getInstance();
                            cal.setTime(listEntity.timeTableEntity.timeStart);
                            Integer listDate = cal.get(Calendar.DATE);
                            cal.setTime(dateTime);
                            Integer currDate = cal.get(Calendar.DATE);
                            if(listDate == currDate){
                                SimpleDateFormat simpleDateFormat = new SimpleDateFormat(getString(R.string.DateWeekFormat), Locale.forLanguageTag("ru"));
                                String myTime = simpleDateFormat.format(listEntity.timeTableEntity.timeStart);
                                ScheduleItemHeader header = new ScheduleItemHeader();
                                header.setTitle(myTime);
                                Integer counter = 0;
                                for(ScheduleItem headItem : tableList){
                                    if(headItem.getClass() == ScheduleItemHeader.class){
                                        if(((ScheduleItemHeader)headItem).getTitle().equalsIgnoreCase(header.getTitle())){
                                            counter += 1;
                                        }
                                    }
                                }
                                if(counter == 0){
                                    tableList.add(header);
                                }
                                ScheduleItem item = new ScheduleItem();
                                SimpleDateFormat simpleFormat = new SimpleDateFormat(getString(R.string.HourMinuteFormat), Locale.forLanguageTag("ru"));
                                item.setStart(simpleFormat.format(listEntity.timeTableEntity.timeStart));
                                item.setEnd(simpleFormat.format(listEntity.timeTableEntity.timeEnd));
                                if(listEntity.timeTableEntity.type == 0){
                                    item.setType(getString(R.string.Lection));
                                }
                                else {
                                    item.setType(getString(R.string.PracticeLesson));
                                }
                                item.setName(listEntity.timeTableEntity.subjName);
                                item.setPlace(listEntity.timeTableEntity.corp + ", "+listEntity.timeTableEntity.cabinet);
                                item.setTeacher(listEntity.teacherEntity.fio);
                                tableList.add(item);
                            }
                        }
                        adapter.setDataList(tableList);
                    }
                });
            }
            else if(type == ScheduleType.WEEK){
                mainViewModel.getTimeTableByTeacher(id).observe(this, new Observer<List<TimeTableWithTeacherEntity>>() {
                    @Override
                    public void onChanged(List<TimeTableWithTeacherEntity> list) {
                        for (TimeTableWithTeacherEntity listEntity : list){
                            Date lastWeekday = GetLastWeekDay();
                            Calendar cal = Calendar.getInstance();
                            cal.setTime(listEntity.timeTableEntity.timeStart);
                            Integer listDate = cal.get(Calendar.DATE);
                            cal.setTime(dateTime);
                            Integer currDate = cal.get(Calendar.DATE);
                            cal.setTime(lastWeekday);
                            Integer lastWeek = cal.get(Calendar.DATE);

                            if(listDate >= currDate && listDate <= lastWeek){
                                SimpleDateFormat simpleDateFormat = new SimpleDateFormat(getString(R.string.DateWeekFormat), Locale.forLanguageTag("ru"));
                                String myTime = simpleDateFormat.format(listEntity.timeTableEntity.timeStart);
                                ScheduleItemHeader header = new ScheduleItemHeader();
                                header.setTitle(myTime);
                                Log.d("SCH", header.getTitle());
                                Integer counter = 0;
                                for(ScheduleItem headItem : tableList){
                                    if(headItem.getClass() == ScheduleItemHeader.class){
                                        if(((ScheduleItemHeader)headItem).getTitle().equalsIgnoreCase(header.getTitle())){
                                            counter += 1;
                                        }
                                    }
                                }
                                if(counter == 0){
                                    tableList.add(header);
                                }
                                ScheduleItem item = new ScheduleItem();
                                SimpleDateFormat simpleFormat = new SimpleDateFormat(getString(R.string.HourMinuteFormat), Locale.forLanguageTag("ru"));
                                item.setStart(simpleFormat.format(listEntity.timeTableEntity.timeStart));
                                item.setEnd(simpleFormat.format(listEntity.timeTableEntity.timeEnd));
                                if(listEntity.timeTableEntity.type == 0){
                                    item.setType(getString(R.string.Lection));
                                }
                                else {
                                    item.setType(getString(R.string.PracticeLesson));
                                }
                                item.setName(listEntity.timeTableEntity.subjName);
                                item.setPlace(listEntity.timeTableEntity.corp + ", "+listEntity.timeTableEntity.cabinet);
                                item.setTeacher(listEntity.teacherEntity.fio);
                                tableList.add(item);
                            }
                        }
                        adapter.setDataList(tableList);
                    }
                });
            }
        }
    }

    public Date GetLastWeekDay(){
        int dayOfWeek = 1;
        Calendar now = Calendar.getInstance();
        int weekday = now.get(Calendar.DAY_OF_WEEK);


        int days = dayOfWeek - weekday;
        if (days < 0) days += 7;
        now.add(Calendar.DAY_OF_YEAR, days);

        Date date = now.getTime();
        return date;
    }

}