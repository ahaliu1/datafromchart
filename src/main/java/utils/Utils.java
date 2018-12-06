package utils;

import models.DateNumPoint;
import models.DatePoint;
import models.NumPoint;
import models.Point;
import net.sourceforge.tess4j.Word;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {
    public List<NumPoint> getRightNumPoints(List<Word> numList, Point originPoint) {
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
            int num = numPointList.get(i).getNumber();
            int rowNum = numPointList.get(i).getRowNum();
            double rate[] = new double[numPointList.size()];

            for (int j = 0; j < numPointList.size(); j++) {
                int targetNum = numPointList.get(j).getNumber();
                int targetRowNum = numPointList.get(j).getRowNum();
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

    public List<DatePoint> getRightDatePoints(List<Word> dateList, Point originPoint) {
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
            int rowNum = datePointList.get(i).getRowNum();
            double rate[] = new double[datePointList.size()];

            for (int j = 0; j < datePointList.size(); j++) {
                long targetTime = datePointList.get(j).getDate().getTimeInMillis();
                int targetRowNum = datePointList.get(j).getRowNum();
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

    public List<NumPoint> getPixelCoordinate(List<NumPoint> numPointList, Point originPoint) {
        for (NumPoint numPoint : numPointList) {
            numPoint.setColNum(numPoint.getColNum() - originPoint.getColNum());
            numPoint.setRowNum(originPoint.getRowNum() - numPoint.getRowNum());
        }
        return numPointList;
    }

    /**
     * @param linePointList      曲线坐标
     * @param originPoint        原点坐标
     * @param rigitNumPointList  y轴上正确的点
     * @param rightDatePointList x轴上正确的点
     * @return
     */
    public List<DateNumPoint> getCoordinate(List<Point> linePointList, Point originPoint, List<NumPoint> rigitNumPointList, List<DatePoint> rightDatePointList) {
        double yRate = Math.abs((rigitNumPointList.get(0).getNumber() - rigitNumPointList.get(1).getNumber())
                / (rigitNumPointList.get(0).getRowNum() - rigitNumPointList.get(1).getRowNum()));

        // 25为图片中最下方的y数字与原点像素差距
        double originY = rigitNumPointList.get(0).getNumber() - (Math.abs(rigitNumPointList.get(0).getRowNum() -
                originPoint.getRowNum()) + 25) * yRate;

        double xRate = Math.abs((rightDatePointList.get(1).getDate().getTimeInMillis() -
                rightDatePointList.get(2).getDate().getTimeInMillis())
                / (rightDatePointList.get(1).getColNum() - rightDatePointList.get(2).getColNum()));

        long originX = rightDatePointList.get(0).getDate().getTimeInMillis() -
                (long) ((Math.abs(rightDatePointList.get(0).getColNum() - originPoint.getColNum())) * xRate);

        // 计算每个点的日期与指数
        List<DateNumPoint> dateNumPointList = new ArrayList<DateNumPoint>();
        for (Point numPoint : linePointList) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(originX + (long) (Math.abs(numPoint.getColNum() - originPoint.getColNum()) * xRate));

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
                int yNum = (int) (Math.abs(numPoint.getRowNum() - originPoint.getRowNum()) * yRate + originY);

                DateNumPoint dateNumPoint = new DateNumPoint(calendar, yNum, numPoint.getRowNum(), numPoint.getColNum());

                dateNumPointList.add(dateNumPoint);
            }

        }

        return dateNumPointList;
    }
}
