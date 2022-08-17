package org.hse.android;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HseRepository {
    private DatabaseManager databaseManager;
    private HseDao dao;
    private OkHttpClient client = new OkHttpClient();

    public HseRepository(Context context) {
        databaseManager = DatabaseManager.getInstance(context);
        dao = databaseManager.getHseDao();
    }

    public LiveData<List<GroupEntity>> getGroups() { return dao.getAllGroup(); }
    public LiveData<List<TeacherEntity>> getTeachers() { return dao.getAllTeacher(); }

    public LiveData<List<TimeTableWithTeacherEntity>> getTimeTableByTeacher(Integer teacherId) {
        return dao.getTimeTableByTeacher(teacherId); }
    public LiveData<List<TimeTableWithTeacherEntity>> getTimeTableByGroup(Integer groupId) {
        return dao.getTimeTableByGroup(groupId); }
    public LiveData<List<TimeTableWithTeacherEntity>> getTimeTableTeacherByDate(Date date) {
        return dao.getTimeTableTeacher(); }

    public LiveData<List<TimeTableWithTeacherEntity>> getTimeTableTeacherByDateAndTeacher(Date date, Integer teacherId) {
        return dao.getTimeTableTeacherByDateAndTeacher(date, teacherId); }
    public LiveData<List<TimeTableWithTeacherEntity>> getTimeTableTeacherByDateAndGroup(Date date, Integer groupId) {
        return dao.getTimeTableTeacherByDateAndGroup(date, groupId); }

    protected void getTime(Callback callback) {
        Request request = new Request.Builder().url(MainActivity.URL).build();
        Call call = client.newCall(request);
        call.enqueue(callback);
    }
}
