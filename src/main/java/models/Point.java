package models;

public class Point {
    private int rowNum;

    public int getRowNum() {
        return rowNum;
    }

    public void setRowNum(int rowNum) {
        this.rowNum = rowNum;
    }

    public int getColNum() {
        return colNum;
    }

    public void setColNum(int colNum) {
        this.colNum = colNum;
    }

    private int colNum;

    public Point(int rowNum,int colNum){
        this.rowNum=rowNum;
        this.colNum=colNum;
    }
    public Point(){
        this.rowNum=-1;
        this.colNum=-1;
    }
    public void print(){
        System.out.println(this.colNum+"\t"+this.rowNum+"\t");
    }
}