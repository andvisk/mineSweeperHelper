Helps to solve Minesweeper game board.<br>
<br>
Install:<br>
<br>
OpenCV 4.5.4 -> { <br>
for (i in 1:4){cat('&nbsp;')}from https://opencv.org/opencv-4-5-4/ Download OpenCV 4.5.4 -> Win pack<br>
&nbsp;to C:/opencv/ <br>
&nbsp;&nbsp;or if already installed, set the path in mineSweeperHelper/build.gradle file <br>
&nbsp;&nbsp;&nbsp;def opencvPathWindows = 'C:/path/to/opencv/install/path<strong>/build/java/x64</strong>'<br>
&nbsp;}<br>
<br>
Tesseract 5.2.0 -> {<br>
&nbsp;from https://digi.bib.uni-mannheim.de/tesseract/tesseract-ocr-w64-setup-v5.2.0.20220712.exe<br>
&nbsp;to C:\Program_Files\Tesseract_OCR<br>
&nbsp;&nbsp;or if already installed, set the path in mineSweeperHelper/build.gradle file <br>
&nbsp;&nbsp;&nbsp;def tesseractPathWindows = 'C:/path/to/tesseract'<br>
}<br>
<br>
git clone https://github.com/andvisk/mineSweeperHelper.git<br>
cd mineSweeperHelper<br>
gradlew run<br>
<br>
Tested with java 17, Windows 10<br>
<br>
![alt text](https://github.com/andvisk/mineSweeperHelper/blob/master/screenshot.jpg)

