package tingyu.datafromchart_backend.OCRmodels;

import java.util.Calendar;

public class DatePoint extends Point {

    public Calendar getDate() {
        return date;
    }

    public void setDate(Calendar date) {
        this.date = date;
    }

    private Calendar date;

    public DatePoint(Calendar date, int rowNum, int colNum){
        super(rowNum, colNum);
        this.date=date;
    }
}
