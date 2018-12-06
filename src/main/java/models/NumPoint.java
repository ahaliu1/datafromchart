package models;

public class NumPoint extends Point {
    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    private int number;

    public NumPoint(int number,int rowNum,int colNum){
        super(rowNum, colNum);
        this.number=number;
    }

}
