package tingyu.datafromchart_backend.utils;


import net.sourceforge.tess4j.Word;
import org.opencv.core.Mat;
import tingyu.datafromchart_backend.OCRmodels.DateNumPoint;
import tingyu.datafromchart_backend.OCRmodels.DatePoint;
import tingyu.datafromchart_backend.OCRmodels.NumPoint;
import tingyu.datafromchart_backend.OCRmodels.Point;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

    public List<NumPoint> getRightNumPointsForBaidu(List<Word> numList, Point originPoint) {
        List<NumPoint> numPointList = new ArrayList<NumPoint>();
        // 过滤数字
        for (Word word : numList) {
            if (word.getText().length() < 10) {
                int temp = 0;
                for (int i = 0; i < word.getText().length(); i++) {
                    char ch = word.getText().charAt(i);
                    if (Character.isDigit(ch)) {
                        temp = 10 * temp + ch - '0';
                    }
                }
                NumPoint numPoint = new NumPoint(temp, word.getBoundingBox().y, word.getBoundingBox().x);
                numPointList.add(numPoint);
            }
        }

        List<NumPoint> rightNumPointList = new ArrayList<NumPoint>();

        // 算法，计算得出数值差/纵坐标差
        for (int i = 0; i < numPointList.size(); i++) {
            int num = numPointList.get(i).getNumber1();
            int rowNum = numPointList.get(i).getY();
            double rate[] = new double[numPointList.size()];

            for (int j = 0; j < numPointList.size(); j++) {
                int targetNum = numPointList.get(j).getNumber1();
                int targetRowNum = numPointList.get(j).getY();
                if (targetRowNum - rowNum == 0) {
                    rate[j] = 0;
                } else {
                    rate[j] = (float) (targetNum - num) / (targetRowNum - rowNum);
                }
            }

            // 如果存在两个比值差小于level则认为该点识别正确
            boolean findFlag = false;
            double level = 0.5;
            for (int j = 0; j < numPointList.size(); j++) {
                for (int k = 0; k < numPointList.size(); k++) {
                    if (!findFlag) {
                        if (Math.abs(rate[j] - rate[k]) < level && j != k) {
                            findFlag = true;
                        }
                    } else {
                        break;
                    }
                }
            }

            if (findFlag) {
                rightNumPointList.add(numPointList.get(i));
            }
        }

        return rightNumPointList;
    }

    public List<DatePoint> getRightDatePointsForBaidu(List<Word> dateList, Point originPoint) {
        List<DatePoint> datePointList = new ArrayList<DatePoint>();

        Pattern y = Pattern.compile("(\\d{4})\\年*");
        Pattern md = Pattern.compile("(\\d{1,2})\\月(\\d{1,2})\\日");
        Matcher m;
        // 识别年份
        int year = 0;
        m = y.matcher(dateList.get(0).getText());
        if (m.find()) {
            year = Integer.parseInt(m.group(1));
        }
        // 过滤识别结果，整理成DatePoint的形式
        for (Word word : dateList) {
            m = md.matcher(word.getText());
            if (m.find()) {
                int month = Integer.parseInt(m.group(1));
                int day = Integer.parseInt(m.group(2));
                Calendar calendar = new GregorianCalendar();
                // 设置月份的时候canlendar内部的月份从0开始
                calendar.set(year, month - 1, day);
                DatePoint datePoint = new DatePoint(calendar, word.getBoundingBox().y, word.getBoundingBox().x);
                datePointList.add(datePoint);
            }
        }

        // 验证数据与坐标

        List<DatePoint> rightDatePointList = new ArrayList<DatePoint>();

        // 算法，计算得出数值差/横坐标差
        for (int i = 0; i < datePointList.size(); i++) {
            long time = datePointList.get(i).getDate().getTimeInMillis();
            int rowNum = datePointList.get(i).getY();
            double rate[] = new double[datePointList.size()];

            for (int j = 0; j < datePointList.size(); j++) {
                long targetTime = datePointList.get(j).getDate().getTimeInMillis();
                int targetRowNum = datePointList.get(j).getY();
                if (targetRowNum - rowNum == 0) {
                    rate[j] = 0;
                } else {
                    rate[j] = (float) (targetTime - time) / (targetRowNum - rowNum);
                }
            }

            // 如果存在两个比值差小于level则认为该点识别正确
            boolean findFlag = false;
            double level = 0.5;
            for (int j = 0; j < datePointList.size(); j++) {
                for (int k = 0; k < datePointList.size(); k++) {
                    if (!findFlag) {
                        if (Math.abs(rate[j] - rate[k]) < level && j != k) {
                            findFlag = true;
                        }
                    } else {
                        break;
                    }
                }
            }

            if (findFlag) {
                rightDatePointList.add(datePointList.get(i));
            }
        }

        return rightDatePointList;

    }

    /**
     * @param linePointList      曲线坐标
     * @param originPoint        原点坐标
     * @param rigitNumPointList  y轴上正确的点
     * @param rightDatePointList x轴上正确的点
     * @return
     */
    public List<DateNumPoint> getCoordinateForBaidu(List<Point> linePointList,
                                                    Point originPoint,
                                                    List<NumPoint> rigitNumPointList,
                                                    List<DatePoint> rightDatePointList) {
        double yRate = Math.abs((rigitNumPointList.get(0).getNumber1() - rigitNumPointList.get(1).getNumber1())
                / (rigitNumPointList.get(0).getY() - rigitNumPointList.get(1).getY()));

        // 25为图片中最下方的y数字与原点像素差距
        double originY = rigitNumPointList.get(0).getNumber1() - (Math.abs(rigitNumPointList.get(0).getY() -
                originPoint.getY()) + 25) * yRate;

        double xRate = Math.abs((rightDatePointList.get(1).getDate().getTimeInMillis() -
                rightDatePointList.get(2).getDate().getTimeInMillis())
                / (rightDatePointList.get(1).getX() - rightDatePointList.get(2).getX()));

        long originX = rightDatePointList.get(0).getDate().getTimeInMillis() -
                (long) ((Math.abs(rightDatePointList.get(0).getX() - originPoint.getX())) * xRate);

        // 计算每个点的日期与指数
        List<DateNumPoint> dateNumPointList = new ArrayList<DateNumPoint>();
        for (Point numPoint : linePointList) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(originX + (long) (Math.abs(numPoint.getX() - originPoint.getX()) * xRate));

            boolean existFlag = false;

            // 查看列表中是否已经有这一天
            // 0 ear 1 year 6 day of year
            for (DateNumPoint d : dateNumPointList) {
                if (d.getDate().get(1) == calendar.get(1) && d.getDate().get(2) == calendar.get(2) && d.getDate().get(Calendar.DAY_OF_MONTH) == calendar.get(Calendar.DAY_OF_MONTH)) {
                    existFlag = true;
                    break;
                }
            }
            // 如果还没有这一天的点则加入
            if (!existFlag) {
                int yNum = (int) (Math.abs(numPoint.getY() - originPoint.getY()) * yRate + originY);

                DateNumPoint dateNumPoint = new DateNumPoint(calendar, yNum, numPoint.getY(), numPoint.getX());

                dateNumPointList.add(dateNumPoint);
            }

        }

        return dateNumPointList;
    }

    /**
     * @param line
     * @param originalPoint
     * @param xRate
     * @param yRate
     * @param x0
     * @param y0
     * @return
     */
    public List<double[]> cal(Mat line, Point originalPoint, double xRate, double yRate, double x0, double y0) {
        List<double[]> points = new ArrayList<double[]>();

        for (int i = 0; i < line.rows(); i++) {
            for (int j = 0; j < line.cols(); j++) {
                if (line.get(i, j)[0] != 255) {
                    double x = (j - originalPoint.getX()) * xRate + x0;
                    double y = Math.abs(originalPoint.getY() - i) * yRate + y0;
                    double[] a = new double[2];
                    a[0] = x;
                    a[1] = y;
                    points.add(a);
                }
            }
        }

        return points;
    }

    /**
     * 将图片中所有文本分类识别出横，纵轴数据。
     *
     * @param words 包含识别图片中的所有的文本
     * @return
     */
    public List<List<Word>> splitIntoLateralAndDirect(List<Word> words) {
        List<List<Word>> ret = new ArrayList<List<Word>>();
        List<List<Word>> xEqualList = new ArrayList<List<Word>>();
        List<List<Word>> yEqualList = new ArrayList<List<Word>>();

        for (Word w : words) {
            int x = w.getBoundingBox().x;
            int y = w.getBoundingBox().y;

            boolean x_flag = false;
            boolean y_flag = false;

            for (List<Word> wordList : xEqualList) {
                if (x <= wordList.get(0).getBoundingBox().x + 3 && x >= wordList.get(0).getBoundingBox().x - 3) {
                    wordList.add(w);
                    x_flag = true;
                }
            }
            if (!x_flag) {
                List<Word> temp = new ArrayList<Word>();
                temp.add(w);
                xEqualList.add(temp);
            }

            for (List<Word> wordList : yEqualList) {
                if (y <= wordList.get(0).getBoundingBox().y + 3 && y >= wordList.get(0).getBoundingBox().y - 3) {
                    wordList.add(w);
                    y_flag = true;
                }
            }
            if (!y_flag) {
                List<Word> temp = new ArrayList<Word>();
                temp.add(w);
                yEqualList.add(temp);
            }
        }

        ret.add(getLongestList(yEqualList));
        ret.add(getLongestList(xEqualList));

        return ret;
    }

    public List<Word> filterPointByRate(List<Word> words, boolean isX) {
        List<Word> rightNumPointList = new ArrayList<Word>();

        // 算法，计算得出数值差/纵坐标差
        for (int i = 0; i < words.size(); i++) {
            int textNum = 0;
            try {
                textNum = Integer.parseInt(words.get(i).getText());

            } catch (NumberFormatException e) {
                continue;
            }
            int axisNum = isX ? (int) words.get(i).getBoundingBox().getX() : (int) words.get(i).getBoundingBox().getY();
            double rate[] = new double[words.size()];

            for (int j = 0; j < words.size(); j++) {
                int targetNum = 0;
                try {
                    targetNum = Integer.parseInt(words.get(j).getText());

                } catch (NumberFormatException e) {
                    continue;
                }

                int targetRowNum = isX ? (int) words.get(j).getBoundingBox().getX() : (int) words.get(j).getBoundingBox().getY();
                if (targetRowNum - axisNum == 0) {
                    rate[j] = 0;
                } else {
                    rate[j] = (float) (targetRowNum - axisNum) / (targetNum - textNum);
                }
            }

            // 如果存在两个比值差小于level,并且对比的两个点的比值与该点比值小于level，则认为该点识别正确
            boolean findFlag = false;
            double level = 1;
            for (int j = 0; j < words.size(); j++) {
                for (int k = 0; k < words.size(); k++) {
                    if (!findFlag) {
                        double f = Math.abs(rate[j] - rate[k]);
                        if (f < level && j != k && i != j && i != k) {
                            int a = 0;
                            try {
                                a = Integer.parseInt(words.get(j).getText()) - Integer.parseInt(words.get(k).getText());
                            } catch (NumberFormatException e) {
                                continue;
                            }

                            int b = isX ? (int) words.get(j).getBoundingBox().getX() : (int) words.get(j).getBoundingBox().getY();
                            int c = isX ? (int) words.get(k).getBoundingBox().getX() : (int) words.get(k).getBoundingBox().getY();
                            float backup = (float) Math.abs((c - b) / a);
                            double e = Math.abs(Math.abs(rate[j]) - Math.abs(backup));
                            if (e < level) {
                                findFlag = true;
                            }
                        }
                    } else {
                        break;
                    }
                }
            }

            if (findFlag) {
                rightNumPointList.add(words.get(i));
            }
        }

        return rightNumPointList;
    }

    /**
     * 得到最常的list
     *
     * @param lists
     * @return
     */
    private List<Word> getLongestList(List<List<Word>> lists) {
        int longest = 0;
        int longestIndex = 0;
        for (List<Word> wordList : lists) {
            if (wordList.size() > longest) {
                longest = wordList.size();
                longestIndex = lists.indexOf(wordList);
            }
        }
        return lists.get(longestIndex);

    }

    public List<Double[]> getCoordinate(List<Point> linePoints, Point originalPoint, List<Word> lateralAxisWords, List<Word> directAxisWords) {
        List<Double[]> coordinates = new ArrayList<Double[]>();

        double xRate, yRate;

        if (lateralAxisWords.size() < 3) {
            System.out.println("横坐标识别数目不够");
            return coordinates;
        } else {
            xRate = Math.abs((Double.valueOf(lateralAxisWords.get(0).getText()) - Double.valueOf(lateralAxisWords.get(1).getText())) /
                    (lateralAxisWords.get(0).getBoundingBox().getX() - lateralAxisWords.get(1).getBoundingBox().getX()));
        }

        if (directAxisWords.size() < 3) {
            System.out.println("纵坐标识别数目不够");
            return coordinates;
        } else {
            yRate = Math.abs((Double.valueOf(directAxisWords.get(0).getText()) - Double.valueOf(directAxisWords.get(1).getText())) /
                    (directAxisWords.get(0).getBoundingBox().getY() - directAxisWords.get(1).getBoundingBox().getY()));
        }

        double originalX = Double.valueOf(lateralAxisWords.get(0).getText()) -
                (lateralAxisWords.get(0).getBoundingBox().getCenterX() - originalPoint.getX()) * xRate;

        double originalY = Double.valueOf(directAxisWords.get(0).getText()) -
                (originalPoint.getY() - directAxisWords.get(0).getBoundingBox().getCenterY()) * yRate;


        for (Point point : linePoints) {
            double newX = (point.getX() - originalPoint.getX()) * xRate + originalX;
            double newY = (originalPoint.getY() - point.getY()) * yRate + originalY;
            Double temp[] = {newX, newY};
            coordinates.add(temp);
        }

        return coordinates;
    }
}