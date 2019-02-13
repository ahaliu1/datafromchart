package models;

public class Point {
    private int y;

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    private int x;

    public Point(int x,int y){
        this.y =y;
        this.x =x;
    }
    public Point(){
        this.y =-1;
        this.x =-1;
    }
    public void print(){
        System.out.println(this.x +"\t"+this.y +"\t");
    }
}