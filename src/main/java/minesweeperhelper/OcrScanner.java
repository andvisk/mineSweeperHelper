package minesweeperhelper;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class OcrScanner {

    private static Logger log = LogManager.getLogger(OcrScanner.class);

    public String getTextFromImage(final String imageFilePath) {

        cvCvtColor(srcImage, destImage, CV_BGR2GRAY);
        cvSmooth(destImage, destImage, CV_MEDIAN, 3, 0, 0, 0);
        cvThreshold(destImage, destImage, 0, 255, CV_THRESH_OTSU);

        return getStringFromImage(imageFilePath/*cleanedFilePath*/);
    }

    private String getStringFromImage(final String pathToImageFile) {
        try {
            final URL tessDataResource = getClass().getResource("/");
            final File tessFolder = new File(tessDataResource.toURI());
            final String tessFolderPath = tessFolder.getAbsolutePath();

            BytePointer outText;
            tesseract.TessBaseAPI api = new tesseract.TessBaseAPI();
            api.SetVariable("tessedit_char_whitelist", "0123456789,/ABCDEFGHIJKLMNOPQRSTUVWXYZ");

            //init tesseract-ocr with english
            if (api.Init(tessFolderPath, "lit") != 0) {
                log.error("Could not initialize tesseract");
            }

            //open input image with leptonica lib
            PIX image = pixRead(pathToImageFile);
            api.SetImage(image);

            //get ocr result
            outText = api.GetUTF8Text();
            String stringOut = outText.getString();

            //destroy used object
            api.End();
            outText.deallocate();
            pixDestroy(image);
            return stringOut;
        }catch (Exception e){
            return e.getMessage();
        }
    }
    
}
