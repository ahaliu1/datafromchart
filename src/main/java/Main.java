import jxl.Workbook;
import jxl.write.Number;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import models.DateNumPoint;
import models.DatePoint;
import models.NumPoint;
import models.Point;
import net.sourceforge.tess4j.Word;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import utils.OpenCVUtils;
import utils.Tess4jUtils;
import utils.Utils;

import java.io.File;
import java.util.*;


public class Main {

    private static OpenCVUtils openCVUtils = new OpenCVUtils();
    private static Tess4jUtils tess4jUtils = new Tess4jUtils();
    private static Utils utils = new Utils();

    public static void main(String[] args) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
//        String path1 = ".\\img\\test1.png";
//        getDataFromBaidu(path1);

        String path2 = ".\\img\\3.png";
        try {
            getDataFromCommonLineChat(path2);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 处理百度指数
     *
     * @param path
     */
    private static void getDataFromBaidu(String path) {

        // 打开图片
        Mat image = Imgcodecs.imread(path);
        if (image.empty()) {
            System.out.println("empty");
        }


        // 识别论文图片使用
//        //Mat line = Imgcodecs.imread(".\\img\\line.jpg");
//        Point originPoint = new Point(827,43);
//
//        //500/140
//        double xRate = 3.5714;
//        //0.2/85
//        double yRate = 0.0025;
//
//        double x0 = 3750;
//
//        double y0 = 0.33;

//        Point originPoint = new Point(816,49);
//
//        //500/140
//        double xRate = 3.5714;
//        //0.2/85
//        double yRate = 0.0025;
//
//        double x0 = 3750;
//
//        double y0 = 0.33;
//        List<double[]> a =utils.cal(line,originPoint,xRate,yRate,x0,y0);
//        for (double[] item:a) {
//            System.out.println(item[0]+"\t"+item[1]);
//        }
//
//        Imgcodecs.imwrite(".\\img\\line.jpg", line);

        // 识别横轴日期和纵轴数字
        // 横轴日期中文list

        List<Word> dateList = new ArrayList<Word>();
        // 纵轴指数数字list
        List<Word> numList = new ArrayList<Word>();

        try {
            dateList = tess4jUtils.getChiWords(path);
            numList = tess4jUtils.getNumWords(path);

        } catch (Exception e) {
            e.printStackTrace();
        }

        // 得到曲线单像素图片
        Mat line = openCVUtils.getlineImg(image);

        // 设置原点，横坐标日期识别第一个位于原点下方
        //models.Point originPoint = openCVUtils.getOriginPoint(image,line);
        models.Point originPoint = new Point();
        originPoint.setX(dateList.get(0).getBoundingBox().x);
        originPoint.setY(dateList.get(0).getBoundingBox().y - 15);

        // 得到曲线点集坐标
        List<Point> linePointList = openCVUtils.getlinePoints(line);

        // 过滤错误的纵轴数据
        List<NumPoint> rigitNumPointList = utils.getRightNumPointsForBaidu(numList, originPoint);
        if (rigitNumPointList.size() < 2) {
            System.out.println("识别的纵坐标的数目不足");
            return;
        }

        //过滤错误的横轴数据
        List<DatePoint> rigitDatePointList = utils.getRightDatePointsForBaidu(dateList, originPoint);
        if (rigitDatePointList.size() < 2) {
            System.out.println("识别的横坐标的数目不足");
            return;
        }

        // 得到曲线上每个点的时间和指数
        List<DateNumPoint> dateNumPointList = utils.getCoordinateForBaidu(linePointList, originPoint, rigitNumPointList, rigitDatePointList);

        for (int i = 0; i < dateNumPointList.size(); i++) {
            dateNumPointList.get(i).print();
        }
    }


    /**
     * 处理横纵坐标均为数字
     *
     * @param path
     */
    private static void getDataFromCommonLineChat(String path) throws Exception {
        // 打开图片
        Mat image = Imgcodecs.imread(path);
        if (image.empty()) {
            System.out.println("empty");
        }

        Mat imageCopy = image.clone();

        List<RotatedRect> textAreas = openCVUtils.drawTextArea(image);

        List<Word> words = tess4jUtils.getWordListFromArea(textAreas, path, "eng", true);

        List<List<Word>> lateralAndDirectListAxis = utils.splitIntoLateralAndDirect(words);

        // 横轴数字list
        List<Word> lateralAxisWords = lateralAndDirectListAxis.get(0);
        // 纵轴数字list
        List<Word> directAxisWords = lateralAndDirectListAxis.get(1);

        // 原点
        Point originPoint = new Point((int) (directAxisWords.get(0).getBoundingBox().x + directAxisWords.get(0).getBoundingBox().getWidth())
                , lateralAxisWords.get(0).getBoundingBox().y);

        //
        lateralAxisWords = utils.filterPointByRate(lateralAxisWords, true);

        directAxisWords = utils.filterPointByRate(directAxisWords, false);

        List<Point> points = openCVUtils.getLinePoint(imageCopy, openCVUtils.BLUE);

        List<Double[]> coordinates = utils.getCoordinate(points, originPoint, lateralAxisWords, directAxisWords);


        File xlsFile = new File("data.xls");
        // 创建一个工作簿
        WritableWorkbook workbook = Workbook.createWorkbook(xlsFile);
        // 创建一个工作表
        WritableSheet sheet = workbook.createSheet("sheet1", 0);


        for (int i = 0; i < coordinates.size(); i++) {
            // 向工作表中添加数据
            sheet.addCell(new Number(0, i, coordinates.get(i)[0]));
            sheet.addCell(new Number(1, i, coordinates.get(i)[1]));

        }

        WritableSheet sheet2 = workbook.createSheet("sheet2", 1);
        for (int i = 0; i <points.size(); i++) {
            // 向工作表中添加数据
            sheet2.addCell(new Number(0, i, points.get(i).getX()));
            sheet2.addCell(new Number(1, i, points.get(i).getY()));

        }
        workbook.write();
        workbook.close();

    }

}


