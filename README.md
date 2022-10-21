Helps to solve Minesweeper game board.<br>
<br>
Install:<br>
<br>
OpenCV 4.5.4 -> { <br>
    from https://opencv.org/opencv-4-5-4/ Download OpenCV 4.5.4 -> Win pack<br>
    to C:/opencv/ <br>
        or if already installed, set the path in mineSweeperHelper/build.gradle file <br>
            def opencvPathWindows = 'C:/path/to/opencv/install/path<strong>/build/java/x64</strong>'<br>
    }<br>
<br>
Tesseract 5.2.0 -> {<br>
    from https://digi.bib.uni-mannheim.de/tesseract/tesseract-ocr-w64-setup-v5.2.0.20220712.exe<br>
    to C:\Program_Files\Tesseract_OCR<br>
        or if already installed, set the path in mineSweeperHelper/build.gradle file <br>
            def tesseractPathWindows = 'C:/path/to/tesseract'<br>
}<br>
<br>
git clone https://github.com/andvisk/mineSweeperHelper.git<br>
cd mineSweeperHelper<br>
gradlew run<br>
<br>
Tested with java 17, Windows 10<br>
<br>
![alt text](https://github.com/andvisk/mineSweeperHelper/blob/master/screenshot.jpg)

