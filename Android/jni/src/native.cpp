#include <errno.h>
#include <jni.h>
#include <sys/time.h>
#include <time.h>
#include <android/log.h>

#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include <float.h>
#include <queue>

#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/highgui/highgui.hpp>

#include "libopentld/tld/TLD.h"
#include "libopentld/tld/TLDUtil.h"
#include "libopentld/tld/TrackerUtils.h"

#define jjobject jclass // jobject

using namespace cv;
using namespace std;
using namespace tld;

// globals 
bool globalDebugI;
int globalDebugModulo;

bool init(cv::Mat& mat, int* ibbx);
cv::Rect process(cv::Mat& mat, int* ibbx, unsigned long cmd);


bool debugOn()
{
    return false;
}

#ifdef __cplusplus
extern "C" {
#endif


JNIEXPORT jboolean JNICALL 
Java_com_o3dr_solo_android_util_tracker_Tracker_nativeInit (JNIEnv*jenv, jjobject, 
    jobject buffer, jint width, jint height, 
    jobject bbx, 
    jint debugLevel);

JNIEXPORT void JNICALL 
Java_com_o3dr_solo_android_util_tracker_Tracker_nativeDetect (JNIEnv*jenv, jjobject, 
    jobject buffer, jint width, jint height, jobject bbx, jint, jint);


#ifdef __cplusplus
}
#endif
 
// tracker variables
tld::TLD tracker;
int initialBB[4];
cv::Rect bb;

// debugging stuff
static int frameNo = 0;
static int failures = 0;


bool init(cv::Mat& mat, int*ibbx)
{
    initialBB[0] = ibbx[0]; //mat.cols/3;
    initialBB[1] = ibbx[1]; //mat.rows/3;
    initialBB[2] = ibbx[2];  //mat.cols/3;
    initialBB[3] = ibbx[3]; //mat.rows/3;

    bb = tld::tldArrayToRect(initialBB);

    cv::Mat grey(mat.size().width, mat.size().height, CV_8UC1);

    grey = mat;
    tracker.detectorCascade->imgWidth = (int)grey.cols;
    tracker.detectorCascade->imgHeight = (int)grey.rows;
    tracker.detectorCascade->imgWidthStep = (int)grey.step;
        
    LOGD("tracker.selectObject b[%d,%d,%d,%d] w=%d, h=%d", 
        bb.x,bb.y, bb.width, bb.height, mat.cols, mat.rows);

    tracker.selectObject(grey, &bb);

    LOGD("tracker.selectObject ended");    
}
 
cv::Rect process(cv::Mat& mat, int*ibbx, unsigned long cmd)
{
    //LOGD("processImage %dx%d cmd=%ld", mat.cols, mat.rows, cmd);
    tracker.processImage(mat, cmd);
 
    if(tracker.currBB != NULL)
    {
        LOGD("%.2d %.2d %.2d %.2d\n",  
                tracker.currBB->x, 
                tracker.currBB->y, 
                tracker.currBB->width, 
                tracker.currBB->height);

        cv::rectangle(mat, 
            cv::Point(tracker.currBB->x, tracker.currBB->y), 
            cv::Point(tracker.currBB->x+tracker.currBB->width, tracker.currBB->y+tracker.currBB->height), 
            cv::Scalar(200, 200, 0));
        
        return cv::Rect(tracker.currBB->x, tracker.currBB->y, tracker.currBB->width, tracker.currBB->height);
    }
    else
    {
        return cv::Rect(0,0,0,0);
    }
}

void dumpImage(Mat& frame8)
{
    if (debugOn())
    {
        // $ adb -d shell cat /sdcard/tldframe000.jpg
        if (frameNo%globalDebugModulo==0)
        {
            char filename[256]; sprintf(filename, "/sdcard/_tld/tldframe%02d.jpg", frameNo%10);
            cv::imwrite(filename, frame8); 
        }
    }
}

void jniToCpp(JNIEnv* jenv, jobject& buffer, jobject& bbx, 
    unsigned char* & refFrameBufferData, 
    int* & ibb)
{
    ibb = (int*)jenv->GetDirectBufferAddress(bbx);        
    refFrameBufferData = (unsigned char*)jenv->GetDirectBufferAddress(buffer);
}

JNIEXPORT jboolean JNICALL 
Java_com_o3dr_solo_android_util_tracker_Tracker_nativeInit(JNIEnv*jenv, jjobject, 
    jobject buffer, jint width, jint height, 
    jobject bbx, 
    jint debugLevel)
{
    try
    {    
        int* ibb;
        unsigned char* frameDataRGBA;
        jniToCpp(jenv, buffer, bbx, frameDataRGBA, ibb);

        cv::Mat frame8;
        cv::Mat frame = cv::Mat(Size(width, height), CV_8UC4, frameDataRGBA);
        cv::cvtColor(frame, frame8, CV_RGBA2GRAY); 

        LOGD("INIT TRACKER ******* accepted failures#=%d ********", failures);
        failures = 0;

        bool ok = init(frame8, ibb);

        return ok;
    }
    catch(cv::Exception& e)
    {
        LOGD("nativeCreateObject caught cv::Exception: %s", e.what());
        jclass je = jenv->FindClass("org/opencv/core/CvException");
        if(!je)
            je = jenv->FindClass("java/lang/Exception");
        jenv->ThrowNew(je, e.what());
    }
    catch (...)
    {
        LOGD("nativeDetect caught unknown exception");
        jclass je = jenv->FindClass("java/lang/Exception");
        jenv->ThrowNew(je, "Unknown exception in JNI code DetectionBasedTracker.nativeDetect()");
    }        
}

JNIEXPORT void JNICALL 
Java_com_o3dr_solo_android_util_tracker_Tracker_nativeDetect(JNIEnv*jenv, jjobject, 
    jobject buffer, jint width, jint height, jobject bbx, jint cmd, jint debugLevel)
{
     frameNo++;
     
    try
    {
        int* ibb;
        unsigned char* frameDataRGBA;
        jniToCpp(jenv, buffer, bbx, frameDataRGBA, ibb);

        cv::Mat frame8;
        //convertImage(frameDataRGBA, width, height, frame8);
        cv::Mat frame = cv::Mat(Size(width, height), CV_8UC4, frameDataRGBA);
        cv::cvtColor(frame, frame8, CV_RGBA2GRAY); 

        cv::Rect bb = process(frame8, ibb, cmd);

        if (bb.width > 0)
        {
            //LOGD("****RESULT*****:  [%d,%d,%d,%d]", bb.x, bb.y, bb.width, bb.height);  
       
            // scale back to original resolution's coordinate system
            float sx = (float)width/(float)frame8.cols;
            float sy = (float)height/(float)frame8.rows;

            ibb[0] = (float)bb.x * sx;
            ibb[1] = (float)bb.y * sy;
            ibb[2] = (float)bb.width *  sx;
            ibb[3] = (float)bb.height *  sy;

            // tracker status
            ibb[5]  = (int)getTrackerStatus();

            // tracker points
            int* t = getFBTCoords();
            int numPoints = t[0];
            ibb[10] = numPoints;
            memcpy(&ibb[10], &t[0], (1+numPoints*4)*sizeof(int));
        }
        else
        {
            ibb[0] = 0;
            ibb[1] = 0;
            ibb[2] = 0;
            ibb[3] = 0;                
            failures++;
        }
        //LOGD(" nativeDetect END ibb[%d,%d,%d,%d] trackerStatus=%d", 
        //    ibb[0], ibb[1], ibb[2], ibb[3], ibb[5]);
    }
    catch(cv::Exception& e)
    {
        LOGD("nativeCreateObject caught cv::Exception: %s", e.what());
        jclass je = jenv->FindClass("org/opencv/core/CvException");
        if(!je)
            je = jenv->FindClass("java/lang/Exception");
        jenv->ThrowNew(je, e.what());
    }
    catch (...)
    {
        LOGD("nativeDetect caught unknown exception");
        jclass je = jenv->FindClass("java/lang/Exception");
        jenv->ThrowNew(je, "Unknown exception in JNI code DetectionBasedTracker.nativeDetect()");
    }
}


