LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE := gpuimage-library
LOCAL_LDFLAGS := -Wl,--build-id
LOCAL_SRC_FILES := \
	C:\android_code\LikePet_Release_20160107\LikePet_Release_20160106_3\gpuimage\src\main\jni\Android.mk \
	C:\android_code\LikePet_Release_20160107\LikePet_Release_20160106_3\gpuimage\src\main\jni\Application.mk \
	C:\android_code\LikePet_Release_20160107\LikePet_Release_20160106_3\gpuimage\src\main\jni\yuv-decoder.c \

LOCAL_C_INCLUDES += C:\android_code\LikePet_Release_20160107\LikePet_Release_20160106_3\gpuimage\src\main\jni
LOCAL_C_INCLUDES += C:\android_code\LikePet_Release_20160107\LikePet_Release_20160106_3\gpuimage\src\release\jni

include $(BUILD_SHARED_LIBRARY)
