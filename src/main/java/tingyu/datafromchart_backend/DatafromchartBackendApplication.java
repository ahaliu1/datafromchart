package tingyu.datafromchart_backend;

import org.opencv.core.Core;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DatafromchartBackendApplication {

	public static void main(String[] args) {
//		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

//		String path = "G:\\opencv\\build\\java\\x64\\opencv_java320.dll";
//		System.load(path);

		try {
			String opencvPath = "/usr/local/share/OpenCV/java/libopencv_java320.so";
			System.load(opencvPath);
		} catch (Exception e) {
			throw new RuntimeException("Failed to load opencv native library", e);
		}

		SpringApplication.run(DatafromchartBackendApplication.class, args);
	}

}

