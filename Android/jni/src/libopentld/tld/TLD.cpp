/*  Copyright 2011 AIT Austrian Institute of Technology
*
*   This file is part of OpenTLD.
*
*   OpenTLD is free software: you can redistribute it and/or modify
*   it under the terms of the GNU General Public License as published by
*    the Free Software Foundation, either version 3 of the License, or
*   (at your option) any later version.
*
*   OpenTLD is distributed in the hope that it will be useful,
*   but WITHOUT ANY WARRANTY; without even the implied warranty of
*   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*   GNU General Public License for more details.
*
*   You should have received a copy of the GNU General Public License
*   along with OpenTLD.  If not, see <http://www.gnu.org/licenses/>.
*
*/
/*
 * TLD.cpp
 *
 *  Created on: Nov 17, 2011
 *      Author: Georg 

 *
    modified by Aaron Licata

 */

#include "TLD.h"

#include <iostream>

#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/highgui/highgui.hpp>

#include "NNClassifier.h"
#include "TLDUtil.h"
#include "TrackerUtils.h"

using namespace std;
using namespace cv;


namespace tld
{

const bool useMedian  = true;
const int medianSize = 7;

TLD::TLD()
{
    trackerEnabled = true;
    detectorEnabled = true;
    learningEnabled = true;

    alternating = true;
    learningFrames = 0;
    
    valid = false;
    wasValid = false;

    learning = false;

    currBB = NULL;
    prevBB = new Rect(0,0,0,0);


    detectorCascade = new DetectorCascade();
    nnClassifier = detectorCascade->nnClassifier;

    medianFlowTracker = new MedianFlowTracker();
}

TLD::~TLD()
{
    storeCurrentData();

    if(currBB)
    {
        delete currBB;
        currBB = NULL;
    }

    if(detectorCascade)
    {
        delete detectorCascade;
        detectorCascade = NULL;
    }

    if(medianFlowTracker)
    {
        delete medianFlowTracker;
        medianFlowTracker = NULL;
    }

    if(prevBB)
    {
        delete prevBB;
        prevBB = NULL;
    }
}

void TLD::release()
{
    detectorCascade->release();
    medianFlowTracker->cleanPreviousData();

    if(currBB)
    {
        delete currBB;
        currBB = NULL;
    }
}

void TLD::storeCurrentData()
{
    prevImg.release();
    prevImg = currImg; //Store old image (if any)
    if(currBB)//Store old bounding box (if any)
    {
        prevBB->x = currBB->x;
        prevBB->y = currBB->y;
        prevBB->width = currBB->width;
        prevBB->height = currBB->height;
    }
    else
    {
        prevBB->x = 0;
        prevBB->y = 0;
        prevBB->width = 0;
        prevBB->height = 0;
    }

    detectorCascade->cleanPreviousData(); //Reset detector results
    medianFlowTracker->cleanPreviousData();

    wasValid = valid;
}

bool checkRoiValidity(const Mat &img, Rect *bb)
{
    const int minEdgeResponseCount = 10000;

    Mat roi = img(*bb);

    Mat gradx, grady;
    cv::Sobel(roi, gradx, -1, 1, 0, 3);
    cv::Sobel(roi, grady, -1, 0, 1, 3);
    Mat grad = 0.5*gradx + 0.5*grady;

    Scalar s = cv::sum(grad);
    int s0 = s[0];
    int s1 = s[1];

#ifndef ANDROID
    cv::imshow("roi", grad);
#endif

    if (s0 < minEdgeResponseCount)
        return  false;

    return true;
}

bool TLD::selectObject(const Mat &img, Rect *bb)
{
    TrackerStatusSet(0);
    
    LOGD("selectObject: checkvalidity");
    if (!checkRoiValidity(img, bb))
    {
        LOGD("selectObject: checkvalidity failed");
        return false;
    }

    //Delete old object
    detectorCascade->release();

    detectorCascade->objWidth = bb->width;
    detectorCascade->objHeight = bb->height;

    LOGD("selectObject: init detectorCascade");
    //Init detector cascade
    detectorCascade->init();

    if (useMedian)
    {    
        cv::medianBlur(img, currImg, medianSize);
    }
    else
    {
        currImg = img;
    }

    if(currBB)
    {
        delete currBB;
        currBB = NULL;
    }
    LOGD("selectObject: convert BB to TLD format");
    currBB = tldCopyRect(bb);
    currConf = 1;
    valid = true;
    learningFrames = 0;

    LOGD("selectObject: init learnding");
    initialLearning();
    LOGD("selectObject: init completed.");

    TrackerStatusAdd(STATUS_INITED);

    return true;
}

void TLD::processImage(const Mat &img, unsigned long cmd=1|2|4)
{
    const int maxLearningFrames = -1;

    TrackerStatusSet(0);

    // toggle tld components
    trackerEnabled = (cmd & 1);
    detectorEnabled = (cmd & 2);
    learningEnabled = (cmd & 4);

    // check if learning has completed
    if (maxLearningFrames>0 
        && learningFrames++ > maxLearningFrames)
    {
        learningEnabled = false;        
    }

    storeCurrentData();
    Mat grey_frame;

    if (useMedian)
    {    
        cv::medianBlur(img, grey_frame, medianSize);
    }
    else
    {
        grey_frame = img;
    }
    currImg = grey_frame; // Store new image , right after storeCurrentData();
    
    imRoll(currImg, "curImage");

    if(trackerEnabled)
    {
        imRoll(prevImg, "prevImg");
        TrackerStatusAdd(STATUS_TRACKING);

        LOGD("TLD track");
        medianFlowTracker->track(prevImg, currImg, prevBB);

        if (medianFlowTracker->trackerBB == NULL)
        {
            TrackerStatusAdd(STATUS_TRACKER_FAILED);       
        } 
        LOGD("TLD track completed.");       
    }

    if(detectorEnabled && (!alternating || medianFlowTracker->trackerBB == NULL))
    {
        TrackerStatusAdd(STATUS_DETECTING);
        LOGD("TLD detect");
        detectorCascade->detect(grey_frame);
        LOGD("TLD detect completed.");

        if (detectorCascade->detectionResult->detectorBB == NULL)
        {
            TrackerStatusAdd(STATUS_DETECTOR_FAILED);
        }
    }

    TrackerStatusAdd(STATUS_FUSING);
    LOGD("TLD fuse");
    fuseHypotheses();
    LOGD("TLD fuse completed");

    LOGD("TLD learn"); 
    learn();
    LOGD("TLD learn completed");

}

void TLD::fuseHypotheses()
{
    Rect *trackerBB = medianFlowTracker->trackerBB;
    int numClusters = detectorCascade->detectionResult->numClusters;
    Rect *detectorBB = detectorCascade->detectionResult->detectorBB;

    if(currBB)
    {
        delete currBB;
        currBB = NULL;
    }
    currConf = 0;
    valid = false;

    float confDetector = 0;

    if(numClusters == 1)
    {
        confDetector = nnClassifier->classifyBB(currImg, detectorBB);
    }

    if(trackerBB != NULL)
    {
        float confTracker = nnClassifier->classifyBB(currImg, trackerBB);
        if(currBB)
        {
            delete currBB;
            currBB = NULL;
        }

        if(numClusters == 1 && confDetector > confTracker && tldOverlapRectRect(*trackerBB, *detectorBB) < 0.5)
        {

            currBB = tldCopyRect(detectorBB);
            currConf = confDetector;
        }
        else
        {
            currBB = tldCopyRect(trackerBB);
            currConf = confTracker;

            if(confTracker > nnClassifier->thetaTP)
            {
                valid = true;
            }
            else if(wasValid && confTracker > nnClassifier->thetaFP)
            {
                valid = true;
            }
        }
    }
    else if(numClusters == 1)
    {
        if(currBB)
        {
            delete currBB;
            currBB = NULL;
        }
        currBB = tldCopyRect(detectorBB);
        currConf = confDetector;
    }


    if (!valid)
    {
        TrackerStatusAdd(STATUS_FUSER_FAILED);
    }
 
    /*
    float var = CalculateVariance(patch.values, nn->patch_size*nn->patch_size);

    if(var < min_var) { //TODO: Think about incorporating this
        printf("%f, %f: Variance too low \n", var, classifier->min_var);
        valid = 0;
    }*/
}

void TLD::initialLearning()
{
    learning = true; //This is just for display purposes
    TrackerStatusAdd(STATUS_LEARNING);

    DetectionResult *detectionResult = detectorCascade->detectionResult;

    detectorCascade->detect(currImg);

    //This is the positive patch
    NormalizedPatch patch;
    tldExtractNormalizedPatchRect(currImg, currBB, patch.values);
    patch.positive = 1;

    float initVar = tldCalcVariance(patch.values, TLD_PATCH_SIZE * TLD_PATCH_SIZE);
    detectorCascade->varianceFilter->minVar = initVar / 2;


    float *overlap = new float[detectorCascade->numWindows];
    tldOverlapRect(detectorCascade->windows, detectorCascade->numWindows, currBB, overlap);

    //Add all bounding boxes with high overlap

    vector< pair<int, float> > positiveIndices;
    vector<int> negativeIndices;

    //First: Find overlapping positive and negative patches

    for(int i = 0; i < detectorCascade->numWindows; i++)
    {

        if(overlap[i] > 0.6)
        {
            positiveIndices.push_back(pair<int, float>(i, overlap[i]));
        }

        if(overlap[i] < 0.2)
        {
            float variance = detectionResult->variances[i];

            if(!detectorCascade->varianceFilter->enabled || variance > detectorCascade->varianceFilter->minVar)   //TODO: This check is unnecessary if minVar would be set before calling detect.
            {
                negativeIndices.push_back(i);
            }
        }
    }

    sort(positiveIndices.begin(), positiveIndices.end(), tldSortByOverlapDesc);

    vector<NormalizedPatch> patches;

    patches.push_back(patch); //Add first patch to patch list

    int numIterations = std::min<size_t>(positiveIndices.size(), 10); //Take at most 10 bounding boxes (sorted by overlap)

    for(int i = 0; i < numIterations; i++)
    {
        int idx = positiveIndices.at(i).first;
        //Learn this bounding box
        assert(idx >=  0);
     
        int featureVectorsBase = detectorCascade->numTrees * idx;
        assert(featureVectorsBase >= 0);

        //TODO: Somewhere here image warping might be possible
        detectorCascade->ensembleClassifier->learn(
            &detectorCascade->windows[TLD_WINDOW_SIZE * idx], 
            true, 
            &detectionResult->featureVectors[featureVectorsBase]);
    }

    srand(1); //TODO: This is not guaranteed to affect random_shuffle

    random_shuffle(negativeIndices.begin(), negativeIndices.end());

    //Choose 100 random patches for negative examples
    for(size_t i = 0; i < std::min<size_t>(100, negativeIndices.size()); i++)
    {
        int idx = negativeIndices.at(i);

        NormalizedPatch patch;
        tldExtractNormalizedPatchBB(currImg, &detectorCascade->windows[TLD_WINDOW_SIZE * idx], patch.values);
        patch.positive = 0;
        patches.push_back(patch);
    }

    detectorCascade->nnClassifier->learn(patches);

    delete[] overlap;

}

//Do this when current trajectory is valid
void TLD::learn()
{
    if(!learningEnabled || !valid || !detectorEnabled)
    {
        learning = false;
        return;
    }

    learning = true;
    TrackerStatusAdd(STATUS_LEARNING);

    LOGD("TLD learn: enabled"); 
    DetectionResult *detectionResult = detectorCascade->detectionResult;

    if(!detectionResult->containsValidData)
    {
        LOGD("TLD learn: re-run detectorCascade detect (there was no valid data)"); 
        detectorCascade->detect(currImg);
        LOGD("TLD learn: came back from detectorCascade detect"); 
    }

    //This is the positive patch
    LOGD("TLD learn: extract patches"); 
    NormalizedPatch patch;
    tldExtractNormalizedPatchRect(currImg, currBB, patch.values);

    LOGD("TLD learn: rect overlap"); 
    float *overlap = new float[detectorCascade->numWindows];
    tldOverlapRect(detectorCascade->windows, detectorCascade->numWindows, currBB, overlap);

    //Add all bounding boxes with high overlap

    vector<pair<int, float> > positiveIndices;
    vector<int> negativeIndices;
    vector<int> negativeIndicesForNN;

    //First: Find overlapping positive and negative patches

    timerStart(0);

    LOGD("TLD learn: label overlaps as positve and negatives"); 
    for(int i = 0; i < detectorCascade->numWindows; i++)
    {
        long tdiff = timerEnd(0);  if (tdiff > 1000)
        {
            LOGD("scanning window takes too long, skip to next  frame");
            return;
        }


        if(overlap[i] > 0.6)
        {
            positiveIndices.push_back(pair<int, float>(i, overlap[i]));
        }

        if(overlap[i] < 0.2)
        {
            if(!detectorCascade->ensembleClassifier->enabled || detectionResult->posteriors[i] > 0.5)   //Should be 0.5 according to the paper
            {
                negativeIndices.push_back(i);
            }

            if(!detectorCascade->ensembleClassifier->enabled || detectionResult->posteriors[i] > 0.5)
            {
                negativeIndicesForNN.push_back(i);
            }

        }
    }

    sort(positiveIndices.begin(), positiveIndices.end(), tldSortByOverlapDesc);

    vector<NormalizedPatch> patches;

    patch.positive = 1;
    patches.push_back(patch);
    //TODO: Flip


    int numIterations = std::min<size_t>(positiveIndices.size(), 10); //Take at most 10 bounding boxes (sorted by overlap)

    LOGD("TLD learn: detectorCascade->ensembleClassifier->learn NEGATIVES"); 
    for(size_t i = 0; i < negativeIndices.size(); i++)
    {
        long tdiff = timerEnd(0);  if (tdiff > 1000)
        {
            LOGD("negative learning takes too long, skip to next frame");
            return;
        }

        int idx = negativeIndices.at(i);
        //TODO: Somewhere here image warping might be possible
        detectorCascade->ensembleClassifier->learn(&detectorCascade->windows[TLD_WINDOW_SIZE * idx], false, &detectionResult->featureVectors[detectorCascade->numTrees * idx]);
    }

    LOGD("TLD learn: detectorCascade->ensembleClassifier->learn POSITIVES"); 
    //TODO: Randomization might be a good idea
    for(int i = 0; i < numIterations; i++)
    {
        long tdiff = timerEnd(0);  if (tdiff > 1000)
        {
            LOGD("positive learning takes too long, skip to next frame");
            return;
        }  

        int idx = positiveIndices.at(i).first;
        //TODO: Somewhere here image warping might be possible
        detectorCascade->ensembleClassifier->learn(&detectorCascade->windows[TLD_WINDOW_SIZE * idx], true, &detectionResult->featureVectors[detectorCascade->numTrees * idx]);
    }

    LOGD("TLD learn: extract NEGATIVES patches for NN"); 
    for(size_t i = 0; i < negativeIndicesForNN.size(); i++)
    {
        long tdiff = timerEnd(0);  if (tdiff > 1000)
        {
            LOGD("NN patch extraction takes too long, skip to next frame");
            return;
        }

        int idx = negativeIndicesForNN.at(i);

        NormalizedPatch patch;
        tldExtractNormalizedPatchBB(currImg, &detectorCascade->windows[TLD_WINDOW_SIZE * idx], patch.values);
        patch.positive = 0;
        patches.push_back(patch);
    }

    LOGD("TLD learn: learn NEGATIVES patches with 1D-NN"); 
    detectorCascade->nnClassifier->learn(patches);


    if ((patches.size()+numIterations+ negativeIndices.size()) ==0)
    {
        TrackerStatusAdd(STATUS_LEARNER_FAILED);
    }
    //cout << "NN has now " << detectorCascade->nnClassifier->truePositives->size() << " positives and " << detectorCascade->nnClassifier->falsePositives->size() << " negatives.\n";

    LOGD("TLD learn: delete overlaps and return"); 
    delete[] overlap;
}

typedef struct
{
    int index;
    int P;
    int N;
} TldExportEntry;

void TLD::writeToFile(const char *path)
{
    NNClassifier *nn = detectorCascade->nnClassifier;
    EnsembleClassifier *ec = detectorCascade->ensembleClassifier;

    FILE *file = fopen(path, "w"); 
    if (!file)
    {
        printf("error! cannot open %s file", path);
        return;
    }

    fprintf(file, "#Tld ModelExport\n");
    fprintf(file, "%d #width\n", detectorCascade->objWidth);
    fprintf(file, "%d #height\n", detectorCascade->objHeight);
    fprintf(file, "%f #min_var\n", detectorCascade->varianceFilter->minVar);
    fprintf(file, "%d #Positive Sample Size\n", nn->truePositives->size());



    for(size_t s = 0; s < nn->truePositives->size(); s++)
    {
        float *imageData = nn->truePositives->at(s).values;

        for(int i = 0; i < TLD_PATCH_SIZE; i++)
        {
            for(int j = 0; j < TLD_PATCH_SIZE; j++)
            {
                fprintf(file, "%f ", imageData[i * TLD_PATCH_SIZE + j]);
            }

            fprintf(file, "\n");
        }
    }

    fprintf(file, "%d #Negative Sample Size\n", nn->falsePositives->size());

    for(size_t s = 0; s < nn->falsePositives->size(); s++)
    {
        float *imageData = nn->falsePositives->at(s).values;

        for(int i = 0; i < TLD_PATCH_SIZE; i++)
        {
            for(int j = 0; j < TLD_PATCH_SIZE; j++)
            {
                fprintf(file, "%f ", imageData[i * TLD_PATCH_SIZE + j]);
            }

            fprintf(file, "\n");
        }
    }

    fprintf(file, "%d #numtrees\n", ec->numTrees);
    detectorCascade->numTrees = ec->numTrees;
    fprintf(file, "%d #numFeatures\n", ec->numFeatures);
    detectorCascade->numFeatures = ec->numFeatures;

    for(int i = 0; i < ec->numTrees; i++)
    {
        fprintf(file, "#Tree %d\n", i);

        for(int j = 0; j < ec->numFeatures; j++)
        {
            float *features = ec->features + 4 * ec->numFeatures * i + 4 * j;
            fprintf(file, "%f %f %f %f # Feature %d\n", features[0], features[1], features[2], features[3], j);
        }

        //Collect indices
        vector<TldExportEntry> list;

        for(int index = 0; index < pow(2.0f, ec->numFeatures); index++)
        {
            int p = ec->positives[i * ec->numIndices + index];

            if(p != 0)
            {
                TldExportEntry entry;
                entry.index = index;
                entry.P = p;
                entry.N = ec->negatives[i * ec->numIndices + index];
                list.push_back(entry);
            }
        }

        fprintf(file, "%d #numLeaves\n", list.size());

        for(size_t j = 0; j < list.size(); j++)
        {
            TldExportEntry entry = list.at(j);
            fprintf(file, "%d %d %d\n", entry.index, entry.P, entry.N);
        }
    }

    fclose(file);

}

void TLD::readFromFile(const char *path)
{
    release();

    NNClassifier *nn = detectorCascade->nnClassifier;
    EnsembleClassifier *ec = detectorCascade->ensembleClassifier;

    FILE *file = fopen(path, "r");

    if(file == NULL)
    {
        printf("Error: Model not found: %s\n", path);
        exit(1);
    }

    int MAX_LEN = 255;
    char str_buf[255];
    fgets(str_buf, MAX_LEN, file); /*Skip line*/

    fscanf(file, "%d \n", &detectorCascade->objWidth);
    fgets(str_buf, MAX_LEN, file); /*Skip rest of line*/
    fscanf(file, "%d \n", &detectorCascade->objHeight);
    fgets(str_buf, MAX_LEN, file); /*Skip rest of line*/

    fscanf(file, "%f \n", &detectorCascade->varianceFilter->minVar);
    fgets(str_buf, MAX_LEN, file); /*Skip rest of line*/

    int numPositivePatches;
    fscanf(file, "%d \n", &numPositivePatches);
    fgets(str_buf, MAX_LEN, file); /*Skip line*/


    for(int s = 0; s < numPositivePatches; s++)
    {
        NormalizedPatch patch;

        for(int i = 0; i < 15; i++)   //Do 15 times
        {

            fgets(str_buf, MAX_LEN, file); /*Read sample*/

            char *pch;
            pch = strtok(str_buf, " \n");
            int j = 0;

            while(pch != NULL)
            {
                float val = atof(pch);
                patch.values[i * TLD_PATCH_SIZE + j] = val;

                pch = strtok(NULL, " \n");

                j++;
            }
        }

        nn->truePositives->push_back(patch);
    }

    int numNegativePatches;
    fscanf(file, "%d \n", &numNegativePatches);
    fgets(str_buf, MAX_LEN, file); /*Skip line*/


    for(int s = 0; s < numNegativePatches; s++)
    {
        NormalizedPatch patch;

        for(int i = 0; i < 15; i++)   //Do 15 times
        {

            fgets(str_buf, MAX_LEN, file); /*Read sample*/

            char *pch;
            pch = strtok(str_buf, " \n");
            int j = 0;

            while(pch != NULL)
            {
                float val = atof(pch);
                patch.values[i * TLD_PATCH_SIZE + j] = val;

                pch = strtok(NULL, " \n");

                j++;
            }
        }

        nn->falsePositives->push_back(patch);
    }

    fscanf(file, "%d \n", &ec->numTrees);
    detectorCascade->numTrees = ec->numTrees;
    fgets(str_buf, MAX_LEN, file); /*Skip rest of line*/

    fscanf(file, "%d \n", &ec->numFeatures);
    detectorCascade->numFeatures = ec->numFeatures;
    fgets(str_buf, MAX_LEN, file); /*Skip rest of line*/

    int size = 2 * 2 * ec->numFeatures * ec->numTrees;
    ec->features = new float[size];
    ec->numIndices = pow(2.0f, ec->numFeatures);
    ec->initPosteriors();

    for(int i = 0; i < ec->numTrees; i++)
    {
        fgets(str_buf, MAX_LEN, file); /*Skip line*/

        for(int j = 0; j < ec->numFeatures; j++)
        {
            float *features = ec->features + 4 * ec->numFeatures * i + 4 * j;
            fscanf(file, "%f %f %f %f", &features[0], &features[1], &features[2], &features[3]);
            fgets(str_buf, MAX_LEN, file); /*Skip rest of line*/
        }

        /* read number of leaves*/
        int numLeaves;
        fscanf(file, "%d \n", &numLeaves);
        fgets(str_buf, MAX_LEN, file); /*Skip rest of line*/

        for(int j = 0; j < numLeaves; j++)
        {
            TldExportEntry entry;
            fscanf(file, "%d %d %d \n", &entry.index, &entry.P, &entry.N);
            ec->updatePosterior(i, entry.index, 1, entry.P);
            ec->updatePosterior(i, entry.index, 0, entry.N);
        }
    }

    detectorCascade->initWindowsAndScales();
    detectorCascade->initWindowOffsets();

    detectorCascade->propagateMembers();

    detectorCascade->initialised = true;

    ec->initFeatureOffsets();

    fclose(file);
}


} /* namespace tld */
