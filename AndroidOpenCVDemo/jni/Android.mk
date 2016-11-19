   LOCAL_PATH := $(call my-dir)
 
   include $(CLEAR_VARS)


	OPENCV_CAMERA_MODULES:=off
	OPENCV_INSTALL_MODULES:=on #自动将依赖的OpenCV的so库拷贝到libs目录下，很遗憾的是，这个命令只对 OPENCV_CAMERA_MODULES 有效。只有当OPENCV_CAMERA_MODULES:=on时，可以看到他会自动将里面的带camera的so拷贝至工程下的libs文件夹下。
	OPENCV_LIB_TYPE:=STATIC # SHARED STATIC
	ifdef OPENCV_ANDROID_SDK
	  ifneq ("","$(wildcard $(OPENCV_ANDROID_SDK)/OpenCV.mk)")
	    #include ${OPENCV_ANDROID_SDK}/OpenCV.mk
		#include /Users/zhaishaoping/Documents/work/OpenCV/OpenCV-android-sdk/sdk/OpenCV.mk
		include ${OPENCV_ANDROID_SDK}/sdk/OpenCV.mk
	  else
#	    include ${OPENCV_ANDROID_SDK}/sdk/native/jni/OpenCV.mk
	    include /Users/zhaishaoping/Documents/work/OpenCV/OpenCV-android-sdk/sdk/native/jni/OpenCV.mk
	  endif
	else
#	  include ../../sdk/native/jni/OpenCV.mk
	  include /Users/zhaishaoping/Documents/work/OpenCV/OpenCV-android-sdk/sdk/native/jni/OpenCV.mk
	endif

	LOCAL_SRC_FILES  := DetectionBased.cpp #DetectionBasedTracker_jni.cpp #jni文件夹下的cpp文件，其中的src说明我的jni下还有个子文件夹名字是“src”，这块替换成自己的源码文件就ok了
	LOCAL_C_INCLUDES += $(LOCAL_PATH)
	LOCAL_LDLIBS     += -llog -ldl

   LOCAL_MODULE    :=OpenCV

   include $(BUILD_SHARED_LIBRARY)