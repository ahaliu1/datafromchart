package utils;

import models.Point;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;


public class OpenCVUtils {

    public static final int BLUE = 1;
    public static final int RED = 2;
    public static final int GREEN = 3;

    public List<Point> getlinePoints(Mat line) {
        List<Point> points = new ArrayList<Point>();
        for (int i = 0; i < line.rows(); i++) {
            for (int j = 0; j < line.cols(); j++)
                if (line.get(i, j)[0] == 255) {
                    Point point = new Point(i, j);
                    points.add(point);
                    break;
                }
        }
        return points;
    }


    public Mat printPointInImg(Mat image, Point point) {
        int x = point.getY();
        int y = point.getX();
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

//         转化百度指数使用
        Imgproc.threshold(rgb.get(2), line, 65, 0, Imgproc.THRESH_TOZERO_INV);
        Imgproc.threshold(line, line, 61, 255, Imgproc.THRESH_BINARY_INV);

//        // 转化论文图片使用
//        Imgproc.threshold(rgb.get(0), line, 170, 255, Imgproc.THRESH_TOZERO_INV);
//        Imgproc.threshold(line, line, 165, 255, Imgproc.THRESH_BINARY_INV);
//        将直线化为单像素直线
        for (int i = 0; i < line.cols(); i++) {
            for (int j = line.rows() - 1; j > -1; j--) {
                if (line.get(j, i)[0] != 255) {

                    // 转化为黑底白线
                    line.put(j, i, 255);
                    for (int k = j - 1; k > -1; k--) {
                        line.put(k, i, 0);
                    }
                    break;
                }
            }
        }
        return line;
    }


    /**
     * 返回图像中曲线的坐标列表
     *
     * @param image 原图
     * @param color 线条颜色
     * @return
     */
    public List<Point> getLinePoint(Mat image, int color) {
        Mat line = getLineImg(image, color);
        List<Point> points = lineThinner(line);

        return points;
    }

    /**
     * @param image
     * @param color
     * @return
     */
    private Mat getLineImg(Mat image, int color) {
        ArrayList<Mat> bgr = new ArrayList<Mat>();

        Core.split(image, bgr);
        Mat r = bgr.get(0).clone();
        Mat g = bgr.get(1).clone();
        Mat b = bgr.get(2).clone();
        Mat dif = new Mat();

        switch (color) {
            case BLUE:
                Core.absdiff(b, r, dif);
                break;
            case RED:
                Core.absdiff(r, g, dif);
                break;
            case GREEN:
                Core.absdiff(g, b, dif);
                break;
        }
//        Imgcodecs.imwrite(".\\img\\dif.jpg", dif);
//        Imgcodecs.imwrite(".\\img\\r.jpg", r);
//        Imgcodecs.imwrite(".\\img\\g.jpg", g);
//        Imgcodecs.imwrite(".\\img\\b.jpg", b);


        return dif;
    }

    /**
     *
     */
    private List<Point> lineThinner(Mat line) {
        // 二值化
        Mat binary = new Mat();
        Imgproc.threshold(line, binary, 0, 255, Imgproc.THRESH_BINARY);

        List<Point> points = new ArrayList<Point>();
        Imgcodecs.imwrite(".\\img\\linerThinner.jpg", binary);

        for (int i = 0; i < binary.cols(); i++) {
            for (int j = binary.rows() - 1; j > -1; j--) {
                if (binary.get(j, i)[0] == 255) {
                    points.add(new Point(i, j));
//                    for (int k = j - 1; k > -1; k--) {
//                        binary.put(k, i, 0);
//                    }
                    break;
                }
            }
        }
        return points;
    }

    // 已知曲线的第一个点，向下寻找灰色作为原点
    public Point getOriginPoint(Mat image, Mat line) {
        ArrayList<Mat> bgr = new ArrayList<Mat>();
        Core.split(image, bgr);
        Point startPoint = new Point();

        boolean firstFlag = false;
        for (int i = 0; i < line.cols(); i++) {
            for (int j = line.rows() - 1; j > -1; j--) {
                if (!firstFlag && line.get(j, i)[0] != 255) {
                    //曲线起点
                    firstFlag = true;
                    startPoint.setX(i);
                    startPoint.setY(j);
                    break;

                }
            }
            if (firstFlag) {
                break;
            }
        }

        Point originalPoint = new Point();
        int j = startPoint.getX();
        for (int i = startPoint.getY(); i < image.rows(); i++) {
            if (bgr.get(2).get(i, startPoint.getX())[0] == bgr.get(0).get(i, startPoint.getX())[0] && bgr.get(2).get(i, startPoint.getX())[0] == bgr.get(1).get(i, startPoint.getX())[0]) {
                originalPoint.setY(i);
                originalPoint.setX(startPoint.getX());
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

    public Mat preprocess(Mat gray) {
        //1.Sobel算子，x方向求梯度
        Mat sobel = new Mat();
        Imgproc.Sobel(gray, sobel, -1, 1, 0, 3, 1, 0, Core.BORDER_DEFAULT);

        //2.二值化
        Mat binary = new Mat();
        Imgproc.threshold(sobel, binary, 0, 255, Imgproc.THRESH_OTSU + Imgproc.THRESH_BINARY);

        //3.膨胀和腐蚀操作核设定
        Mat element1 = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(30, 9));
        //控制高度设置可以控制上下行的膨胀程度，例如3比4的区分能力更强,但也会造成漏检
        Mat element2 = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(24, 4));

        //4.膨胀一次，让轮廓突出
        Mat dilate1 = new Mat();
        Imgproc.dilate(binary, dilate1, element2);

        //5.腐蚀一次，去掉细节，表格线等。这里去掉的是竖直的线
        Mat erode1 = new Mat();
        Imgproc.erode(dilate1, erode1, element1);

        //6.再次膨胀，让轮廓明显一些
        Mat dilate2 = new Mat();
        Imgproc.dilate(erode1, dilate2, element2);

        //7.存储中间图片
        Imgcodecs.imwrite(".\\img\\binary.jpg", binary);
        Imgcodecs.imwrite(".\\img\\dilate1.jpg", dilate1);
        Imgcodecs.imwrite(".\\img\\erode1.jpg", erode1);
        Imgcodecs.imwrite(".\\img\\dilate2.jpg", dilate2);

        return dilate2;
    }

    public List<RotatedRect> findTextRegion(Mat img) {
        List<RotatedRect> rects = new ArrayList<RotatedRect>();

        //1.查找轮廓
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Mat hierarchy = new Mat();

        Imgproc.findContours(img, contours, hierarchy, Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE, new org.opencv.core.Point(0, 0));

        List<MatOfPoint2f> newContours = new ArrayList<MatOfPoint2f>();
        for (MatOfPoint point : contours) {
            MatOfPoint2f newPoint = new MatOfPoint2f(point.toArray());
            newContours.add(newPoint);
        }


        //2.筛选那些面积小的
        for (int i = 0; i < contours.size(); i++) {
            //计算当前轮廓的面积
            double area = Imgproc.contourArea(contours.get(i));


//            System.out.println(area);
//            面积小于100的全部筛选掉
            if (area < 100 || area > 1000)
                continue;


            //轮廓近似，作用较小，approxPolyDP函数有待研究
            double epsilon = 0.001 * Imgproc.arcLength(newContours.get(i), true);
            MatOfPoint2f approx = new MatOfPoint2f();
            Imgproc.approxPolyDP(newContours.get(i), approx, epsilon, true);

            //找到最小矩形，该矩形可能有方向
            RotatedRect rect = Imgproc.minAreaRect(newContours.get(i));
            //计算高和宽
            int m_width = rect.boundingRect().width;
            int m_height = rect.boundingRect().height;

            //筛选那些太细的矩形，留下扁的
            if (m_height > m_width * 0.8)
                continue;

            //符合条件的rect添加到rects集合中
            rects.add(rect);

        }
        return rects;
    }

    public List<RotatedRect> detect(Mat image) {
        // 灰度变换
        Mat gray = getGrayImg(image);
        // 形态学变换的预处理，得到可以查找矩形的轮廓
        Mat dilation = preprocess(gray);
        // 查找和筛选文字区域
        List<RotatedRect> rects = findTextRegion(dilation);

        return rects;
    }

    public List<RotatedRect> drawTextArea(Mat image) {

        // 灰度变换
        Mat gray = getGrayImg(image);
        // 形态学变换的预处理，得到可以查找矩形的轮廓
        Mat dilation = preprocess(gray);
        // 查找和筛选文字区域
        List<RotatedRect> rects = findTextRegion(dilation);

        // 用绿线画出这些找到的轮廓
        for (RotatedRect rect : rects) {

            org.opencv.core.Point P[] = new org.opencv.core.Point[4];
            rect.points(P);
            for (int j = 0; j <= 3; j++) {
                Imgproc.line(image, P[j], P[(j + 1) % 4], new org.opencv.core.Scalar(0, 255, 0), 1);
            }
        }

        //5.显示带轮廓的图像
        Imgcodecs.imwrite(".\\img\\imgDrawRect.jpg", image);


        return rects;
    }

    public Mat houghGetLine(Mat image) {
        Mat srcImage = image.clone();
        Mat dstImage = srcImage.clone();
        Imgproc.Canny(srcImage, dstImage, 400, 500, 5, false);
        Mat storage = new Mat();
        Imgproc.HoughLines(dstImage, storage, 1, Math.PI / 180, 150, 0, 0, 0, 10);
        for (int x = 0; x < storage.rows(); x++) {
            double[] vec = storage.get(x, 0);

            double rho = vec[0];
            double theta = vec[1];

            org.opencv.core.Point pt1 = new org.opencv.core.Point();
            org.opencv.core.Point pt2 = new org.opencv.core.Point();

            double a = Math.cos(theta);
            double b = Math.sin(theta);

            double x0 = a * rho;
            double y0 = b * rho;

            pt1.x = Math.round(x0 + 1000 * (-b));
            pt1.y = Math.round(y0 + 1000 * (a));
            pt2.x = Math.round(x0 - 1000 * (-b));
            pt2.y = Math.round(y0 - 1000 * (a));

            if (theta >= 0) {
                Imgproc.line(srcImage, pt1, pt2, new Scalar(0, 255, 255), 3, Imgproc.LINE_4, 0);
            }
        }
        return srcImage;
    }

    public Mat houghProbilityGetLine(Mat image) {
        Mat srcImage = image.clone();
        Mat dstImage = srcImage.clone();
        Imgproc.Canny(srcImage, dstImage, 400, 500, 5, false);
        Mat storage = new Mat();
        Imgproc.HoughLinesP(dstImage, storage, 1, Math.PI / 180, 100, 0, 0);
        for (int x = 0; x < storage.rows(); x++) {
            double[] vec = storage.get(x, 0);
            double x1 = vec[0], y1 = vec[1], x2 = vec[2], y2 = vec[3];
            org.opencv.core.Point start = new org.opencv.core.Point(x1, y1);
            org.opencv.core.Point end = new org.opencv.core.Point(x2, y2);
            Imgproc.line(srcImage, start, end, new Scalar(0, 255, 255), 2, Imgproc.LINE_4, 0);
        }
        return srcImage;
    }
}
