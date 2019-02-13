package utils;

import net.sourceforge.tess4j.*;
import net.sourceforge.tess4j.util.LoadLibs;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;


import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Tess4jUtils {

    private static File tessDataFolder = LoadLibs.extractTessResources("tessdata");
    private static ITesseract instance = new Tesseract();

//    /**
//     * 从图片中提取文字,默认设置英文字库,使用classpath目录下的训练库
//     * @param path
//     * @return
//     */
//    public static String readChar(String path){
//        // JNA Interface Mapping
//        ITesseract instance = new Tesseract();
//        // JNA Direct Mapping
//        // ITesseract instance = new Tesseract1();
//        File imageFile = new File(path);
//        //In case you don't have your own tessdata, let it also be extracted for you
//        //这样就能使用classpath目录下的训练库了
//        //Set the tessdata path
//        instance.setDatapath(tessDataFolder.getAbsolutePath());
//        //英文库识别数字比较准确
//        instance.setLanguage("eng");
//        return (instance, imageFile);
//    }


    public String doOCRInRectangle(BufferedImage bi, int x, int y, int width, int height) throws Exception {
        setlanguage("eng");
        //划定区域
        // x,y是以左上角为原点，width和height是以xy为基础
        Rectangle rect = new Rectangle(x, y, width, height);
        return instance.doOCR(bi, rect);

    }

    // 识别百度指数纵轴
    public List<Word> getNumWords(String path) throws Exception {
        setlanguage("eng");

        File imageFile = new File(path);

        BufferedImage bi = ImageIO.read(imageFile);
        //设置识别的level
        int level = ITessAPI.TessPageIteratorLevel.RIL_WORD;
        //得到
        List<Word> words = instance.getWords(bi, level);

        Pattern p = Pattern.compile("^[0-9]*$");
        List<Word> wordList = new ArrayList<Word>();

        for (int i = 0; i < words.size(); i++) {
            String text = words.get(i).getText().replaceAll("\n", "").replaceAll(" ", "");
            Matcher m = p.matcher(text);
            if (m.matches()) {
                Word word = words.get(i);
                wordList.add(word);
            }
        }
        return wordList;
    }

    // 识别百度指数横轴
    public List<Word> getChiWords(String path) throws Exception {

        File imageFile = new File(path);
        setlanguage("chi_sim");
        BufferedImage bi = ImageIO.read(imageFile);
        //设置识别的level
        int level = ITessAPI.TessPageIteratorLevel.RIL_WORD;
        //得到
        List<Word> words = instance.getWords(bi, level);

        List<Word> wordList = new ArrayList<Word>();
        for (int i = 0; i < words.size(); i++) {
            String text = words.get(i).getText();

            if (text.contains("月") && text.contains("日")) {

                Word word = words.get(i);
//                Rectangle rect = word.getBoundingBox();
//                System.out.println(String.format("Box[%d]: x=%d, y=%d, w=%d, h=%d word=%s", i, rect.x, rect.y, rect.width, rect.height, words.get(i).getText()));

                wordList.add(word);
            }
        }
        return wordList;
    }

    // 获取文本区域中的文本
    public List<Word> getWordListFromArea(List<RotatedRect> rects, String path, String lang, boolean isAllNum) {
        List<Word> words = new ArrayList<Word>();

        setlanguage(lang);

        File imageFile = new File(path);

        BufferedImage bi = null;


        try {
            bi = ImageIO.read(imageFile);
            // Pattern p = Pattern.compile("^[0-9]|.*$");
            Pattern p = Pattern.compile("([0-9]|\\.)");

            for (RotatedRect rect : rects) {
                Rect r = rect.boundingRect();

                // 识别区域内数字。适度扩大
                int extendPixel = 3;
                String text = null;
                if (r.y - extendPixel > 0 && r.height + 2 * extendPixel < bi.getHeight()) {
                    text = doOCRInRectangle(bi, r.x, r.y - extendPixel, r.width, r.height + 2 * extendPixel);
                } else {
                    int y = r.y < 0 ? 0 : r.y;//不知道为什么有些框y值为负数
                    text = doOCRInRectangle(bi, r.x, y, r.width, r.height);
                }

                text = text.replaceAll("\n", "").replaceAll(",",".");

                if (text.equals("")){
                    continue;
                }

                // 匹配数字
                Matcher m = p.matcher(text);

                // 保留数字与小数点
                StringBuffer buffer = new StringBuffer();
                while (m.find()) {
                    for (int i = 1; i <= m.groupCount(); i++) {
                        buffer.append(m.group(i));
                    }
                }


                if (isAllNum) {
                    //if (m.matches()) {
                    String a = buffer.toString();
                    Word word = new Word(a, 0, new Rectangle(r.x, r.y, r.width, r.height));
                    words.add(word);
                    //}

                } else {
                    Word word = new Word(text, 0, new Rectangle(r.x, r.y, r.width, r.height));
                    words.add(word);
                }


            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        return words;
    }

    private void setlanguage(String lan) {
        //Set the tessdata path
        instance.setDatapath(tessDataFolder.getAbsolutePath());
        //英文库识别数字比较准确
        instance.setLanguage(lan);
    }
}