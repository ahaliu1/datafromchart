package models;

public class NumPoint extends Point {
    public int getNumber1() {
        return number;
    }

    public void setNumber1(int number1) {
        this.number = number1;
    }

    private int number;

    public NumPoint(int number, int rowNum, int colNum){
        super(rowNum, colNum);
        this.number = number;
    }

}
