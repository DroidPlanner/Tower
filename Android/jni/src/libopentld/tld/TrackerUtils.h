#ifndef TRACKERUTILS_HPP
#define TRACKERUTILS_HPP

#include <vector>

namespace tld
{

#if defined ( ANDROID )  && defined (TRACKER_DEBUG)
#include <android/log.h>
#define  LOG_TAG    "OCV: Tracker JNI -"
#define  LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define  LOGW(...)  __android_log_print(ANDROID_LOG_WARN,LOG_TAG,__VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)
#else
#define  LOG_TAG    "OCV: Tracker JNI -"
#define  LOGD(...)  
#define  LOGI(...)   
#define  LOGW(...)   
#define  LOGE(...)   
#endif

unsigned long getThreadId();
long getTimeMillis();

void timerStart(int id);
long timerEnd(int id);

enum TrackerStatusMask
{
    STATUS_TRACKING=1,
    STATUS_DETECTING=2,
    STATUS_FUSING=4,
    STATUS_LEARNING=8,
    STATUS_TRACKER_FAILED = 16,
    STATUS_DETECTOR_FAILED = 32,
    STATUS_FUSER_FAILED = 64,
    STATUS_LEARNER_FAILED = 128,

    STATUS_INITED = 32768 // 2^15
};

int* getFBTCoords();
void setTF(int id, float x0, float y0, float x1, float y1);

int getFBTCount();
int* getFBTData();


unsigned int getTrackerStatus();
void TrackerStatusSet(unsigned long  status);
void TrackerStatusAdd(unsigned long status);

void imRoll(cv::Mat& mat, const char* name);

std::vector<unsigned long> getSteps();
void logBB(const char* msg, cv::Rect* bb);


} // namesapce

#endif
