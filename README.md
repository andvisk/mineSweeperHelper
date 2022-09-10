OpenCV

git clone https://github.com/opencv/opencv.git

mkdir build

cmake -S opencv/ -B build/ -D CMAKE_BUILD_TYPE=RELEASE \ \
                              -D CMAKE_INSTALL_PREFIX=/usr/local \ \
                              -D OPENCV_GENERATE_PKGCONFIG=ON \ \
                              -D BUILD_EXAMPLES=OFF \ \
                              -D INSTALL_PYTHON_EXAMPLES=OFF \ \
                              -D INSTALL_C_EXAMPLES=OFF \ \
                              -D PYTHON_EXECUTABLE=$(which python2) \ \
                              -D BUILD_opencv_python2=OFF \ \
                              -D PYTHON3_EXECUTABLE=$(which python3) \ \
                              -D PYTHON3_INCLUDE_DIR=$(python3 -c "from distutils.sysconfig import get_python_inc; print(get_python_inc())") \ \
                              -D PYTHON3_PACKAGES_PATH=$(python3 -c "from distutils.sysconfig import get_python_lib; print(get_python_lib())")

make -j8 --directory=build/

cd build

sudo make install

-----------

build.gradle -> dependencies {

implementation files('/usr/local/share/java/opencv4/opencv-454.jar');

}

build.gradle -> run {

    systemProperties['java.library.path'] = '/usr/local/share/java/opencv4'

}

