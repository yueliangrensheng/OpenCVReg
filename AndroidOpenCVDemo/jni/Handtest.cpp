#include "opencv2/highgui/highgui.hpp"
#include "opencv2/opencv.hpp"
#include <iostream>
using namespace std;
using namespace cv;


int main(int argc, char** argv)
{


	Mat frame;
	int kvalue = 15;//双边滤波邻域大小
	char code = (char)-1;

	if(argc > 1)
    {
        return -1;
    }



		   frame=imread("c2.jpg");

		   Mat frame2=frame.clone();

		   Mat src_gray;//彩色图像转化成灰度图
		   cvtColor(frame, src_gray, COLOR_BGR2GRAY);

		   Mat bf;//对灰度图像进行双边滤波
		   bilateralFilter(src_gray, bf, kvalue, kvalue*2, kvalue/2);

		   vector<Vec3f> circles;//声明一个向量，保存检测出的圆的圆心坐标和半径
		   HoughCircles(bf, circles, CV_HOUGH_GRADIENT, 1.5, 20, 130, 38, 10, 50);//霍夫变换检测圆

/*******************************去掉误检测的圆*************************************/
		   Mat res,roi,imgHSV,imgROI;
		   int radius;

		   res= Mat::zeros(frame.size(), frame.type());   //提取出的图像部分
		   roi= Mat::zeros(frame.size(), CV_8UC1);   //遮罩位置


		   vector<Vec3f> circles_result(50,0);
		   int j=0;
		   int highCount;


		   for(size_t i = 0; i < circles.size();i++)//circles.size(); i++)
		   {
			   Point center(cvRound(circles[i][0]), cvRound(circles[i][1]));
			   radius = cvRound(circles[i][2]);
			   
				roi=0;
				res=0;
				highCount=0;
				/* 画出ROI 形状 */
			   circle(
				   roi,
				   center,
				   radius,//半径
				   CV_RGB(255, 255, 255),
				   -1,//厚度，可以按照半径的厚度计算
				   8, 0
				   );


			   Rect selection;
			   selection.x = MAX(center.x-radius, 0);
			   selection.y = MAX(center.y-radius, 0);
			   selection.width = radius*2;
			   selection.height = radius*2;
			   selection &= Rect(0, 0, frame.cols, frame.rows);

			   frame.copyTo(res, roi);			   			   
			   imgROI=res(selection);
			   cvtColor(imgROI, imgHSV, COLOR_BGR2HSV);  	


			   Mat imgThresholded;  
			   inRange(imgHSV, Scalar(1, 60, 90), Scalar(12, 140, 200), imgThresholded); //Threshold the image  
			   highCount = cv::countNonZero(imgThresholded);    

			   inRange(imgHSV, Scalar(170, 60, 90), Scalar(180, 140, 200), imgThresholded); //Threshold the image  
			   highCount = cv::countNonZero(imgThresholded)+highCount;   

			   cout << highCount << endl;
			   if(highCount>100)//  为真，记录
			   {
				   circles_result[j][0]=circles[i][0];
				   circles_result[j][1]=circles[i][1];
				   circles_result[j][2]=circles[i][2];
				   j++;
			   }
			   
		   }		   
/***********************************************************************************/



/*******************************画出检测到的圆*************************************/
		   cout << "i=\tx=\ty=\tr=" << endl;

		   for(int i = 0; i < j; i++)//把霍夫变换检测出的圆画出来
		   {
			   Point center(cvRound(circles_result[i][0]), cvRound(circles_result[i][1]));
			   radius = cvRound(circles_result[i][2]);
			   			   
    		   circle( frame, center, 2, Scalar(240,40,0), -1, 8, 0 );
			   circle( frame, center, radius, Scalar(240,40,0), -1, 8, 0 );

			   cout << i+1 << "\t"<< cvRound(circles_result[i][0]) << "\t" << cvRound(circles_result[i][1]) << "\t" 
				   << cvRound(circles_result[i][2]) << endl;//在控制台输出圆心坐标和半径				
		   }


		   imwrite("result.jpg", frame);		
		   namedWindow("wisemore cam preview",0); 
		   cvResizeWindow("wisemore cam preview",frame.cols/2,frame.rows/2);

		   imshow( "wisemore cam preview", frame2 );		
		   waitKey(2000);

		   imshow( "wisemore cam preview", frame );					

		   code = (char)waitKey();
		
		   
    return 0;

}

