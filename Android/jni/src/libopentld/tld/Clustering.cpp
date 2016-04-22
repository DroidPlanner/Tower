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
 * Clustering.cpp
 *
 *  Created on: Nov 16, 2011
 *      Author: Georg Nebehay
 */

#include "Clustering.h"

#include "TLDUtil.h"
#include "DetectorCascade.h"
#include "TrackerUtils.h"

using namespace cv;

namespace tld
{

Clustering::Clustering()
{
    cutoff = .5;
    windows = NULL;
    numWindows = 0;
}

Clustering::~Clustering()
{
}

void Clustering::release()
{
    windows = NULL;
    numWindows = 0;
}

void Clustering::calcMeanRect(std::vector<int> * indices)
{

    float x, y, w, h;
    x = y = w = h = 0;

    int numIndices = indices->size();

    for(int i = 0; i < numIndices; i++)
    {
        int *bb = &windows[TLD_WINDOW_SIZE * indices->at(i)];
        x += bb[0];
        y += bb[1];
        w += bb[2];
        h += bb[3];
    }

    x /= numIndices;
    y /= numIndices;
    w /= numIndices;
    h /= numIndices;

    Rect *rect = new Rect();
    detectionResult->detectorBB = rect;
    rect->x = floor(x + 0.5);
    rect->y = floor(y + 0.5);
    rect->width = floor(w + 0.5);
    rect->height = floor(h + 0.5);

}

//distances must be of size n*(n+1)/2
void Clustering::calcDistances(float *distances)
{
    float *distances_tmp = distances;

    std::vector<int> confidentIndices = *detectionResult->confidentIndices;

    size_t indices_size = confidentIndices.size();

    for(size_t i = 0; i < confidentIndices.size(); i++)
    {
        int firstIndex = confidentIndices.at(0);
        confidentIndices.erase(confidentIndices.begin());
        tldOverlapOne(windows, numWindows, firstIndex, &confidentIndices, distances_tmp);
        distances_tmp += indices_size - i - 1;
    }

    for(size_t i = 0; i < indices_size * (indices_size - 1) / 2; i++)
    {
        distances[i] = 1 - distances[i];
    }

}

void Clustering::clusterConfidentIndices()
{

    int numConfidentIndices = detectionResult->confidentIndices->size();
    float *distances = new float[numConfidentIndices * (numConfidentIndices - 1) / 2];
    LOGD("Clustering::clusterConfidentIndices ");
    calcDistances(distances);
    int *clusterIndices = new int[numConfidentIndices];
    LOGD("Clustering::clusterConfidentIndices cluster");
    cluster(distances, clusterIndices);

    if(detectionResult->numClusters == 1)
    {
        LOGD("Clustering::clusterConfidentIndices calcMeanRect ");
        calcMeanRect(detectionResult->confidentIndices);
        //TODO: Take the maximum confidence as the result confidence.
    }
    LOGD("Clustering::clusterConfidentIndices delete distances and indices");

    delete []distances;
    distances = NULL;
    delete []clusterIndices;
    clusterIndices = NULL;
}

void Clustering::cluster(float *distances, int *clusterIndices)
{
    int numConfidentIndices = detectionResult->confidentIndices->size();

    if(numConfidentIndices == 1)
    {
        clusterIndices[0] = 0;
        detectionResult->numClusters = 1;
        return;
    }

    int numDistances = numConfidentIndices * (numConfidentIndices - 1) / 2;

    //Now: Cluster distances
    int *distUsed = new int[numDistances];

    for(int i = 0; i < numDistances; i++)
    {
        distUsed[i] = 0;
    }

    for(int i = 0; i < numConfidentIndices; i++)
    {
        clusterIndices[i] = -1;
    }

    int newClusterIndex = 0;

    int numClusters = 0;

    LOGD("Clustering::cluster start main loop with %d distances and %d conf indices", 
        numDistances, numConfidentIndices);

    timerStart(46);

    while(true)
    {
        long tdiff = timerEnd(46); 
        //LOGD("Clustering::cluster timediff = %ld", tdiff);
        if (tdiff>1000 || tdiff < 0) 
        { 
            LOGD("clustering taking too long - kill clustering"); 
           return; 
        }

        //Search for the shortest distance
        float shortestDist = -1;
        int shortestDistIndex = -1;
        int i1;
        int i2;
        int distIndex = 0;

        //LOGD("Clustering::cluster comp shortest distance");
        for(int i = 0; i < numConfidentIndices; i++)   //Row index
        {
            for(int j = i + 1; j < numConfidentIndices; j++) //Start from i+1
            {

                if(!distUsed[distIndex] && (shortestDistIndex == -1 || distances[distIndex] < shortestDist))
                {
                    shortestDist = distances[distIndex];
                    shortestDistIndex = distIndex;
                    i1 = i;
                    i2 = j;
                }

                distIndex++;
            }
        }

        if(shortestDistIndex == -1)
        {
            LOGD("Clustering::cluster done computing shortestDist index");
            break; // We are done
        }

        distUsed[shortestDistIndex] = 1;

        //Now: Compare the cluster indices
        //If both have no cluster and distance is low, put them both to a new cluster
        if(clusterIndices[i1] == -1 && clusterIndices[i2] == -1)
        {
            //LOGD("Clustering::cluster test  no cluster and distance is low");
            //If distance is short, put them to the same cluster
            if(shortestDist < cutoff)
            {
                clusterIndices[i1] = clusterIndices[i2] = newClusterIndex;
                newClusterIndex++;
                numClusters++;
            }
            else     //If distance is long, put them to different clusters
            {
                //LOGD("Clustering::cluster test distance is long");
                clusterIndices[i1] = newClusterIndex;
                newClusterIndex++;
                numClusters++;
                clusterIndices[i2] = newClusterIndex;
                newClusterIndex++;
                numClusters++;
            }

            //Second point is  in cluster already
        }
        else if(clusterIndices[i1] == -1 && clusterIndices[i2] != -1)
        {
            //LOGD("Clustering::cluster test Second point is  in cluster already");
            if(shortestDist < cutoff)
            {
                clusterIndices[i1] = clusterIndices[i2];
            }
            else     //If distance is long, put them to different clusters
            {
                clusterIndices[i1] = newClusterIndex;
                newClusterIndex++;
                numClusters++;
            }
        }
        else if(clusterIndices[i1] != -1 && clusterIndices[i2] == -1)
        {
            //LOGD("Clustering::cluster test distance long");
            if(shortestDist < cutoff)
            {
                clusterIndices[i2] = clusterIndices[i1];
            }
            else     //If distance is long, put them to different clusters
            {
                clusterIndices[i2] = newClusterIndex;
                newClusterIndex++;
                numClusters++;
            }
        }
        else     //Both indices are in clusters already
        {
            //LOGD("Clustering::cluster test Both indices are in clusters already");

            if(clusterIndices[i1] != clusterIndices[i2] && shortestDist < cutoff)
            {
                //Merge clusters

                int oldClusterIndex = clusterIndices[i2];

                for(int i = 0; i < numConfidentIndices; i++)
                {
                    if(clusterIndices[i] == oldClusterIndex)
                    {
                        clusterIndices[i] = clusterIndices[i1];
                    }
                }

                numClusters--;
            }
        }
    }

    LOGD("Clustering::cluster numClusters found = %d", numClusters);

    detectionResult->numClusters = numClusters;

    delete []distUsed;
    distUsed = NULL;
}

} /* namespace tld */
