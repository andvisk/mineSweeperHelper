Helps to solve Minesweeper game board.<br>

```

Install:

OpenCV 4.5.4 -> { 
    from https://opencv.org/opencv-4-5-4/ Download OpenCV 4.5.4 -> Win pack
    to C:/opencv/ <br>
        or if already installed, set the path in mineSweeperHelper/build.gradle file 
            def opencvPathWindows = 'C:/path/to/opencv/install/path<strong>/build/java/x64</strong>'
    }

Tesseract 5.2.0 -> {
    from https://digi.bib.uni-mannheim.de/tesseract/tesseract-ocr-w64-setup-v5.2.0.20220712.exe
    to C:\Program_Files\Tesseract_OCR<br>
        or if already installed, set the path in mineSweeperHelper/build.gradle file 
            def tesseractPathWindows = 'C:/path/to/tesseract'
}

git clone https://github.com/andvisk/mineSweeperHelper.git
cd mineSweeperHelper
gradlew run


```

Tested with java 17, Windows 10

<br>
<img src="https://github.com/andvisk/mineSweeperHelper/blob/master/screenshot.jpg" alt="minesweeper opencv tesseract java">

