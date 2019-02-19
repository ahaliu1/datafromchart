package tingyu.datafromchart_backend.controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import tingyu.datafromchart_backend.utils.OCR;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.UUID;


@Controller
public class Upload {


    @PostMapping("/upload")
    @ResponseBody
    public Object upload(HttpServletRequest request, HttpServletResponse response) {
        OCR ocr = new OCR();

        // 写入图片
        MultipartHttpServletRequest req = (MultipartHttpServletRequest) request;
        MultipartFile multipartFile = req.getFile("file");


        String uuid = UUID.randomUUID().toString().replace("-", "").toLowerCase();

        String fileName = uuid + ".jpg";
        // windows 输入图片存储路径
//        String filePath = "G:\\JAVA_project\\datafromchart_backend\\input\\";

        // linux 输入图片存储路径
        String filePath = "/home/ubuntu/datafromchart/input/";
        try {
            File dir = new File(filePath);
            if (!dir.exists()) {
                dir.mkdir();
            }
            File file = new File(filePath, fileName);
            multipartFile.transferTo(file);

            ocr.getDataFromCommonLineChat(filePath + fileName, OCR.BLUE,uuid+".xls");


        } catch (Exception e) {
            e.printStackTrace();
            return "";

        }


        return uuid+".xls";

        //messageService.insertImg(new Img(Integer.parseInt(questionOpenId), fileName));

    }

}
