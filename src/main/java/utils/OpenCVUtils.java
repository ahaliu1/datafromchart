package utils;

import models.Point;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class OpenCVUtils {

    public List<Point> getlinePoints(Mat line, Point originalPoint) {
        List<Point> points = new ArrayList<Point>();
        for (int i = 0; i < line.rows(); i++) {
            for (int j = 0; j < line.cols(); j++)
                if (line.get(i, j)[0] != 255) {
                    Point point = new Point(i, j);
                    points.add(point);
                }
        }
        return points;
    }

    public Mat printPointInImg(Mat image, Point point) {
        int x = point.getRowNum();
        int y = point.getColNum();
        ArrayList<Mat> bgr = new ArrayList<Mat>();
        Core.split(image, bgr);
        Mat pointImg = bgr.get(0).clone();
        pointImg.put(x, y, 0);
        pointImg.put(x + 1, y, 0);
        pointImg.put(x - 1, y, 0);
        pointImg.put(x, y + 1, 0);
        pointImg.put(x, y - 1, 0);
        pointImg.put(x + 1, y + 1, 0);
        pointImg.put(x + 1, y - 1, 0);
        pointImg.put(x - 1, y + 1, 0);
        return pointImg;
    }

    public Mat getlineImg(Mat image) {
        ArrayList<Mat> rgb = new ArrayList<Mat>();
        Core.split(image, rgb);

        Mat line = new Mat();
        Imgproc.threshold(rgb.get(2), line, 65, 0, Imgproc.THRESH_TOZERO_INV);
        Imgproc.threshold(line, line, 61, 255, Imgproc.THRESH_BINARY_INV);

        //将直线化为单像素直线
        for (int i = 0; i < line.cols(); i++) {
            for (int j = line.rows() - 1; j > -1; j--) {
                if (line.get(j, i)[0] != 255) {

                    for (int k = j - 1; k > -1; k--) {
                        line.put(k, i, 255);
                    }
                    break;
                }
            }
        }
        return line;
    }

    // 已知曲线的第一个点，向下寻找灰色作为原点
    public Point getOriginPoint(Mat image, Mat line) {
        ArrayList<Mat> bgr = new ArrayList<Mat>();
        Core.split(image, bgr);
        Point startPoint = new Point();

        boolean firstFlag = false;
        for (int i = 0; i < line.cols(); i++) {
            for (int j = line.rows() - 1; j > -1; j--) {
                if (!firstFlag&&line.get(j, i)[0] != 255) {
                    //曲线起点
                    firstFlag = true;
                    startPoint.setColNum(i);
                    startPoint.setRowNum(j);
                    break;

                }
            }
            if (firstFlag) {
                break;
            }
        }

        Point originalPoint = new Point();
        int j = startPoint.getColNum();
        for (int i = startPoint.getRowNum(); i < image.rows(); i++) {
            if (bgr.get(2).get(i, startPoint.getColNum())[0] == bgr.get(0).get(i, startPoint.getColNum())[0] && bgr.get(2).get(i, startPoint.getColNum())[0] == bgr.get(1).get(i, startPoint.getColNum())[0]) {
                originalPoint.setRowNum(i);
                originalPoint.setColNum(startPoint.getColNum());
                break;
            }
        }

        return originalPoint;
    }

    public Mat getGrayImg(Mat image) {
        Mat grayImg = new Mat();
        Imgproc.cvtColor(image, grayImg, Imgproc.COLOR_BGR2GRAY, 0);
        return grayImg;
    }
}
