package models;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class DateNumPoint extends Point {
    public Calendar getDate() {
        return date;
    }

    public void setDate(Calendar date) {
        this.date = date;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    private Calendar date;
    private int number;

    public DateNumPoint(Calendar date,int number,int rowNum, int colNum){
        super(rowNum,colNum);
        this.date=date;
        this.number=number;
    }

    @Override
    public void print() {
        SimpleDateFormat dft = new SimpleDateFormat("yyyy-MM-dd");
        System.out.println(dft.format(this.date.getTimeInMillis())+"\t"+this.number+"\t");
    }
}
