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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        OpenCVUtils openCVUtils = new OpenCVUtils();
        Tess4jUtils tess4jUtils = new Tess4jUtils();
        Utils utils = new Utils();


        // 打开图片
        String path = ".\\img\\test1.png";
        Mat image = Imgcodecs.imread(path);
        if (image.empty()) {
            System.out.println("empty");
        }

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
        originPoint.setColNum(dateList.get(0).getBoundingBox().x);
        originPoint.setRowNum(dateList.get(0).getBoundingBox().y - 15);

        // 得到曲线点集坐标
        List<Point> linePointList = openCVUtils.getlinePoints(line,originPoint);

        // 过滤错误的纵轴数据
        List<NumPoint> rigitNumPointList = utils.getRightNumPoints(numList,originPoint);
        if (rigitNumPointList.size()<2){
            System.out.println("识别的纵坐标的数目不足");
            return;
        }

        //过滤错误的横轴数据
        List<DatePoint> rigitDatePointList = utils.getRightDatePoints(dateList,originPoint);
        if (rigitDatePointList.size()<2){
            System.out.println("识别的横坐标的数目不足");
            return;
        }

        // 得到曲线上每个点的时间和指数
        List<DateNumPoint> dateNumPointList = utils.getCoordinate(linePointList ,originPoint,rigitNumPointList,rigitDatePointList);

        for (int i = 0; i < dateNumPointList.size(); i++) {
            dateNumPointList.get(i).print();
        }
    }

}


