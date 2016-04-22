LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

include sdk/OpenCV3-android-sdk/sdk/native/jni/OpenCV.mk

LOCAL_MODULE    := tracker-jni

#LOCAL_C_INCLUDES := 3rdparty/cvblobs

LOCAL_SRC_FILES := native.cpp libopentld/imacq/ImAcq.cpp libopentld/mftracker/BB.cpp libopentld/mftracker/BBPredict.cpp libopentld/mftracker/FBTrack.cpp libopentld/mftracker/Lk.cpp libopentld/mftracker/Median.cpp libopentld/tld/TrackerUtils.cpp libopentld/tld/Clustering.cpp libopentld/tld/DetectionResult.cpp libopentld/tld/DetectorCascade.cpp libopentld/tld/EnsembleClassifier.cpp libopentld/tld/ForegroundDetector.cpp libopentld/tld/MedianFlowTracker.cpp libopentld/tld/NNClassifier.cpp libopentld/tld/TLD.cpp libopentld/tld/TLDUtil.cpp libopentld/tld/VarianceFilter.cpp 3rdparty/cvblobs/blob.cpp 3rdparty/cvblobs/BlobContour.cpp 3rdparty/cvblobs/BlobOperators.cpp 3rdparty/cvblobs/BlobProperties.cpp 3rdparty/cvblobs/BlobResult.cpp 3rdparty/cvblobs/ComponentLabeling.cpp
    
LOCAL_LDLIBS    += -lm -llog -landroid
#LOCAL_STATIC_LIBRARIES := android_native_app_glue
#LOCAL_LDLIBS += -L../libopencv_java

include $(BUILD_SHARED_LIBRARY)

$(call import-module,android/native_app_glue)
