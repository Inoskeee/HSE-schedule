package org.hse.android;

public class ScheduleItem {
    private String start;
    private String end;
    private String type;
    private String name;
    private String place;
    private String teacher;

    public String getStart(){ return start;}
    public String getEnd(){ return end;}
    public String getType(){ return type;}
    public String getName(){ return name;}
    public String getPlace(){ return place;}
    public String getTeacher(){ return teacher;}

    public void setStart(String start){ this.start = start;}
    public void setEnd(String end){ this.end = end;}
    public void setType(String type){ this.type = type;}
    public void setName(String name){ this.name = name;}
    public void setPlace(String place){ this.place = place;}
    public void setTeacher(String teacher){ this.teacher = teacher;}
}
