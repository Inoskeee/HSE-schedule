package org.hse.android;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.Date;
import java.util.List;

@Dao
public interface HseDao {
    @Query("SELECT * FROM `group`")
    LiveData<List<GroupEntity>> getAllGroup();

    @Insert
    void insertGroup(List<GroupEntity> data);

    @Delete
    void delete(GroupEntity data);

    @Query("SELECT * FROM `teacher`")
    LiveData<List<TeacherEntity>> getAllTeacher();

    @Insert
    void insertTeacher(List<TeacherEntity> data);

    @Delete
    void delete(TeacherEntity data);

    @Query("SELECT * FROM time_table")
    LiveData<List<TimeTableWithTeacherEntity>> getTimeTableTeacher();

    @Query("SELECT * FROM time_table WHERE teacher_id = :teacherId")
    LiveData<List<TimeTableWithTeacherEntity>> getTimeTableByTeacher(Integer teacherId);

    @Query("SELECT * FROM time_table WHERE group_id = :groupId")
    LiveData<List<TimeTableWithTeacherEntity>> getTimeTableByGroup(Integer groupId);

    @Query("SELECT * FROM time_table WHERE teacher_id = :teacherId AND time_start <= :date AND time_end >= :date")
    LiveData<List<TimeTableWithTeacherEntity>> getTimeTableTeacherByDateAndTeacher(Date date, Integer teacherId);

    @Query("SELECT * FROM time_table WHERE group_id = :groupId AND time_start <= :date AND time_end >= :date")
    LiveData<List<TimeTableWithTeacherEntity>> getTimeTableTeacherByDateAndGroup(Date date, Integer groupId);

    @Insert
    void insertTimeTable(List<TimeTableEntity> data);
}
