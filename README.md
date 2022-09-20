How to:

git clone https://github.com/andvisk/mineSweeperHelper.git

Install OpenCV 4.5.4 to C:/opencv/ 
    or if opencv is already installed, set the path in mineSweeperHelper/build.gradle file 
        systemProperties['java.library.path'] = 'C:/path/to/opencv/install/path/build/java/x64'

cd mineSweeperHelper
gradlew run

Tested with java 17, Windows 10

In case help sceen fails to open, increase or decrease zoom in minesweeper app

