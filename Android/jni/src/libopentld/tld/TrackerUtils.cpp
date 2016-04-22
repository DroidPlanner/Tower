#include "TLDUtil.h"

#include <time.h>
#include <numeric>

#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/highgui/highgui.hpp>

#ifdef ANDROID
#include <pthread.h>

#else

#endif


using namespace cv;

namespace tld
{

bool globalDebugI = false;
int globalDebugModulo = 5;
unsigned int trackerStatus = 0;

 
unsigned long getThreadId()
{
#ifdef ANDROID // #ifdef __ANDROID__
        return (unsigned long)pthread_self();
#else
    return 0;
#endif    
}

 


long getTimeNano() 
{
#ifdef ANDROID
     struct timespec now;
     clock_gettime(CLOCK_MONOTONIC, &now);
     return (long)now.tv_sec * 1000000000LL + now.tv_nsec;
#else
    clock_t t = clock();
    return (((float)t/CLOCKS_PER_SEC)*1000000);
#endif
}

long getTimeMillis()
{
    return getTimeNano() / 1000000;
}

long timerId[100];
void timerStart(int id)
{
    timerId[id] = getTimeMillis();
}

long timerEnd(int id)
{
    return getTimeMillis() - timerId[id];
}

unsigned int getTrackerStatus()
{
    return trackerStatus;
}

void TrackerStatusSet(unsigned long  status)
{
    trackerStatus = status;
}

void TrackerStatusAdd(unsigned long status)
{
    trackerStatus |= status;
}


void imRoll(cv::Mat& mat, const char* name)
{
    if (globalDebugI)
    {
        // $ adb -d shell cat /sdcard/tldframe000.jpg
        static int  frameNo = 0;
        if (frameNo%globalDebugModulo==0)
        {
            char filename[256]; sprintf(filename, "/sdcard/_tld/roll%s%03d.jpg", name, frameNo%3); frameNo++;
            cv::imwrite(filename, mat);   
        }  
    }
}


void logBB(const char* msg, cv::Rect* bb)
{
    char s[256];
    if (bb)
    {
        sprintf(s, "%s [%d,%d,%d,%d]", msg, bb->x, bb->y, bb->width, bb->height);
    }
    else
    {
        //sprintf(s, "%s []", msg);
    }
    //LOGD("%s", s);
}


int coords[1+100*100*2*2];
int* getFBTCoords() { return &coords[0]; }
int getFBTCount() { return coords[0]; }
int* getFBTData() { return &coords[1]; }

void setTF(int num, float x0, float y0, float x1, float y1)
{
    coords[0] = num; // update feature count;

    int cix = num*2*2 + 1;
    coords[cix + 0] = (int)x0;
    coords[cix + 1] = (int)y0;
    coords[cix + 2] = (int)x1;
    coords[cix + 3] = (int)y1;
}




} /* namespace tld */

