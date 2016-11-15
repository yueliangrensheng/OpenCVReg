/*
 * DetectionBased.cpp
 *
 *  Created on: 2016年10月23日
 *      Author: zhaishaoping
 */
#include <com_diwen_android_opencv_DetectionBased.h>
#include <opencv2/core/core.hpp>
#include <opencv2/objdetect.hpp>
#include "opencv2/highgui/highgui.hpp"
#include "opencv2/opencv.hpp"
#include <iostream>

#include <string>
#include <vector>

#include <android/log.h>

#define LOG_TAG "Detection/DetectionBased"
#define LOGD(...) ((void)__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__))

using namespace std;
using namespace cv;

JNIEXPORT jobjectArray JNICALL Java_com_diwen_android_opencv_DetectionBased_nativeRecognized(
		JNIEnv * env, jclass, jlong frameAddr) {

	/*jstring      str;
		jobjectArray args = 0;
		jsize        len = 5;
		const char* sa[] = { "Hello,", "world!", " JNI", " is", " fun" };


		args = (env)->NewObjectArray(len, (env)->FindClass("java/lang/String"), 0);
		for(int i = 0; i < len; i++)
		    {
		        str = (env)->NewStringUTF(sa[i]);
		        (env)->SetObjectArrayElement(args, i, str);
		    }
		 return args;*/

	LOGD(
			"Java_com_diwen_android_opencv_DetectionBased_nativeRecognized START");

	LOGD( "Java_com_diwen_android_opencv_DetectionBased_nativeRecognized END");

	// frameAddr -->Mat
	Mat frame =*((Mat*)frameAddr);

	 Mat roi,imgHSV,imgROI;

	      cvtColor(frame, frame, COLOR_RGBA2BGR);

			   cvtColor(frame, imgHSV, COLOR_BGR2HSV);

			   Mat imgThresholded;
			   inRange(imgHSV, Scalar(70, 50, 70), Scalar(90, 255, 220), imgThresholded); //Threshold the image



				frame.copyTo(roi,imgThresholded);
	 		   Mat src_gray;//彩色图像转化成灰度图
			   cvtColor(roi, src_gray, COLOR_BGR2GRAY);



	// 		   Mat bf;//对灰度图像进行双边滤波
	// 		   bilateralFilter(src_gray, bf, kvalue, kvalue*2, kvalue/2);

			   vector<Vec3f> circles;//声明一个向量，保存检测出的圆的圆心坐标和半径
			   HoughCircles(src_gray, circles, CV_HOUGH_GRADIENT, 1.5, 20, 130, 38, 5, src_gray.rows/5);//霍夫变换检测圆



	/*******************************去掉误检测的圆*************************************/
			   int radius;
			   vector<Vec3f> circles_result(500,0);
			   int j=0;
			   int highCount;
			   float S;

			   char result[1000];
			   string sresult;
			   int location_count=0;


			   for(int k=0;k<1000;k++)
				   result[k]=0;

			   for(size_t i = 0; i < circles.size();i++)//circles.size(); i++)
			   {
				   Point center(cvRound(circles[i][0]), cvRound(circles[i][1]));
				   radius = cvRound(circles[i][2]);

				   Rect selection;
				   selection.x = MAX(center.x-radius, 0);
				   selection.y = MAX(center.y-radius, 0);
				   selection.width = radius*2;
				   selection.height = radius*2;
				   selection &= Rect(0, 0, frame.cols, frame.rows);

				   imgROI=imgHSV(selection);

				   Mat imgThresholded2;
				   inRange(imgROI, Scalar(70, 50, 70), Scalar(90, 255, 220), imgThresholded2); //Threshold the image
				   highCount = cv::countNonZero(imgThresholded2);


				   //与环形面积作比较
				   S=3.14159*radius*radius;

				   if(highCount/S>0.6)
				   {
					   cout <<highCount/S << endl;
					   circles_result[j][0]=circles[i][0];
					   circles_result[j][1]=circles[i][1];
					   circles_result[j][2]=circles[i][2];
					   j++;

				   }

			   }

	/***********************************************************************************/


	/*******************************画出检测到的圆*************************************/
	if (j>=0)
	location_count =sprintf( result, "%d,",j );
	for (int i = 0; i < j; i++) //把霍夫变换检测出的圆画出来
			{
//		Point center(cvRound(circles_result[i][0]),
//				cvRound(circles_result[i][1]));
//		radius = cvRound(circles_result[i][2]);

//		circle(frame, center, 2, Scalar(240, 40, 0), -1, 8, 0);
//		circle(frame, center, radius, Scalar(240, 40, 0), 3, 8, 0);

	    location_count += sprintf( result+location_count, "%.0f,%.0f,%.0f,", circles_result[i][0],circles_result[i][1],circles_result[i][2] );

/*//		cout << i + 1 << "\t" << cvRound(circles_result[i][0]) << "\t"
				<< cvRound(circles_result[i][1]) << "\t"
				<< cvRound(circles_result[i][2]) << endl;*/ //在控制台输出圆心坐标和半径
	}

//	imwrite("result.jpg", frame);
//	namedWindow("wisemore cam preview", 0);
//	cvResizeWindow("wisemore cam preview", frame.cols / 2, frame.rows / 2);
//
//	imshow("wisemore cam preview", frame2);
//	waitKey(2000);
//
//	imshow("wisemore cam preview", frame);
//
//	code = (char) waitKey();

	jstring      str;
	jobjectArray args = 0;
	jsize        len = 1;
//	const char* sa[] = result;


	const char* sa= result;
	args = (env)->NewObjectArray(len, (env)->FindClass("java/lang/String"), 0);
	for(int i = 0; i < len; i++)
	    {
	        str = (env)->NewStringUTF(sa);
	        (env)->SetObjectArrayElement(args, i, str);
	    }
	 return args;
	//return env->NewStringUTF("Hello from JNI!");*/
}
