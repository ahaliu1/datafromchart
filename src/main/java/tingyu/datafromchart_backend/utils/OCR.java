package tingyu.datafromchart_backend.utils;

import jxl.Workbook;
import jxl.write.Number;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import net.sourceforge.tess4j.Word;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.RotatedRect;
import org.opencv.imgcodecs.Imgcodecs;
import tingyu.datafromchart_backend.OCRmodels.Point;

import java.io.File;
import java.util.List;

public class OCR {
    public static final int BLUE = 1;
    public static final int RED = 2;
    public static final int GREEN = 3;

    private static OpenCVUtils openCVUtils = new OpenCVUtils();
    private static Tess4jUtils tess4jUtils = new Tess4jUtils();
    private static Utils utils = new Utils();

    public void getDataFromCommonLineChat(String path, int color, String outputFileName) throws Exception {
        // 打开图片
        Mat image = Imgcodecs.imread(path);
        if (image.empty()) {
            System.out.println("empty");
        }

        Mat imageCopy = image.clone();

        List<RotatedRect> textAreas = openCVUtils.detect(image);

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

        List<Point> points = openCVUtils.getLinePoint(imageCopy, color);

        List<Double[]> coordinates = utils.getCoordinate(points, originPoint, lateralAxisWords, directAxisWords);


//        File xlsFile = new File("/home/ubuntu/datafromchart/output/"+outputFileName);
        File xlsFile = new File("./output/" + outputFileName);

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
        for (int i = 0; i < points.size(); i++) {
            // 向工作表中添加数据
            sheet2.addCell(new Number(0, i, points.get(i).getX()));
            sheet2.addCell(new Number(1, i, points.get(i).getY()));

        }
        workbook.write();
        workbook.close();

    }
}
