/*
******************************************************

next to do:

* lots of metrics.  raw base average generations for X, effect of weight fuzz amplitude, effect of mutation rates, effect of pop size, crossover?

* Change challenge away from feed, but instead to mirror a randomly-created model network.
(probably should fork the project to get away from feeds) Mimic? Copycat? BeatMatch? Carbon? Imitator? Mock?

* Support twinning networks, and test. with twinning though, you need different io jacks for self-talk vs. mirror talk.

* Measure efficiency of RARE random crossover


create alternative node class, with
  IOspecies type (done)
  IOdex (chosen from master feed vector)

we can dispose of the IoJack self-buffer members through refcounting.
HOWEVER, we cannot dispose of the master feed through refcounting. could have deathless Ref() and UnRef()?  Maybe even a separate counter?
*/

#include <iostream>
#include <vector> // example std::vector *vex;
#include <algorithm>    // std::sort
#include <math.h>
#include <time.h>       /* time */
#include <sys/time.h>
#include <stdio.h> // printf
#include <stdint.h>
#include <thread> // std::thread
#include <atomic> // std::atomic
#define __STDC_FORMAT_MACROS
#include <inttypes.h>

#include <hash_map>

#include "Base.h"
#include "Org.h"
#include "Pop.h"

using namespace std;
using namespace __gnu_cxx;

#define genomelen 1000
#define baseamp INT_MAX

class Genome;
class Org;

/*
population
creature
genome

each org is tested against reality, and the score is saved. The scores can go as high as we want.
how to know when to stop? success is when resonance has highest energy over input waveform.
should be (response minus input), so that it can't cheat with input==0;

sin embargo, there is no final success.  so we need a good running score. when the score increases

but in simulation, we have a target. just quit when we are within X dist of the target for awhile.

***

modifying length of genomes is just the 'copy' mutation, where a chunk of the genome is duplicated elsewhere.
back with nodes, we just duplicated one node. with whole runs of genome, we need to specify the start and finish places.
start place is easy, that's just like with nodes. but nodes didn't specify destination. new params will be: endmark, destination to insert.
well insert dest is easy. endmark can be anything from startmark to gene length (less than a limit).
pick source, copy source to other vector, insert other vector into source WHEN DONE. only one dupe per spawn?
how to do multi dupes: save all copied chunks in a list of vectors. go through the list randomly and insert them in whole genome.


*/
struct timeval tm0, tm1;
double tf0, tf1;
double delta;

/* ********************************************************************** */

int NumTrials;
double TotalGensToSuccess, AvgGensToSuccess;
int GensToSuccess;
/* ********************************************************************** */
void PopSession() {
  size_t PopMaxSize;
  PopPtr pop;
  OrgPtr org0;
  uint32_t gencnt;
  // 3.129000 seconds for a pop of 100, for 100 generations
  if(false){
    printf("PopSession()\n");
  }
  //int NumGenerations = 10000000;// ten million, for about 10 hours
  int NumGenerations = 100000000;// hundred million
  int CleanPause = 1;//16
  int MaxSize=0, SumSize = 0, AvgSize=0;
  double SumScore0 = 0.0, SumScore1 = 0.0;
  double SumAvgAvgScore = 0.0;
  double FlywheelScore = 0.0;

  //printf("Pop_Create!\n");
  pop = new Pop();
  //printf("Pop Init! %li\n", pop->forestv.size());

  gettimeofday(&tm0, NULL);
  for (gencnt=0; gencnt<NumGenerations; gencnt++) {
    pop->Gen();// running and testing happens here

    org0 = pop->ScoreDexv[0];//org0 = pop->forestv[0]->tenant;
    int numsamples = org0->Wav.size();
    double AvgBeastScore = pop->AvgBeastScore(0.75);
    SumAvgAvgScore += AvgBeastScore;
    double score0 = org0->Score[0];
    double score1 = org0->Score[1];
    SumScore0 += score0;
    SumScore1 += score1;

    double AvgAvgScore = SumAvgAvgScore/(double)(gencnt+1.0);
    double avgscore0 = SumScore0/(double)(gencnt+1.0);
    double avgscore1 = SumScore1/(double)(gencnt+1.0);
    FlywheelScore = (FlywheelScore*0.999) + (score1*0.001);

    if (false) {
      printf("Pop_Gen:%04li, s:%6.2f, %7.2f, %7.2f, numsamples:%3li, ", gencnt, score0, score1, AvgBeastScore, numsamples);
      printf("%7.2f, %7.2f ", avgscore0, avgscore1);
      printf("%7.2f, ", AvgAvgScore);
      printf("fw:%5.2f, ", FlywheelScore);
      //printf("[%s],  ", Undefeated ? "X" : "o");
      printf("\n");
    }
    if (NumGenerations-gencnt > 20) { // stop mutating for 20 generations in the final stretch
      //pop->Mutate(0.8, 0.8);
      //pop->Mutate(0.8, 0.1);
      //pop->Mutate(0.2, 0.2);
      //pop->Mutate(0.1, 0.1);
      //pop->Mutate(0.05, 0.05);// 5% of population is 5% mutated
      //pop->Mutate(0.1, 0.05);// 10% of population is 5% mutated
      //pop->Mutate(0.3, 0.05);// 30% of population is 5% mutated
      //pop->Mutate(0.5, 0.05);// 50% of population is 5% mutated  ******
      pop->Mutate(0.5, 0.20);// 50% of population is 20% mutated  ******
      //pop->Mutate(0.5, 0.40);// 50% of population is 20% mutated  ******
      //pop->Mutate(0.8, 0.05);// 80% of population is 5% mutated  ******
      //pop->Mutate(0.9, 0.05);// 90% of population is 5% mutated  ******
      //pop->Mutate(0.2, 0.05);// 20% of population is 5% mutated
      //pop->Mutate(0.2, 0.01);// 20% of population is 1% mutated
      //pop->Mutate(0.05, 0.05);
    }
    //pop->Clean_Me();
    PopMaxSize=pop->GetMaxSize();
    org0 = pop->forestv[0]->tenant;
    if (MaxSize<PopMaxSize) {
      MaxSize = PopMaxSize;
    }
    SumSize += org0->Wav.size();
  }
  gettimeofday(&tm1, NULL);
  GensToSuccess = gencnt;
  TotalGensToSuccess += GensToSuccess;
  AvgGensToSuccess = TotalGensToSuccess/(double)NumTrials;
  if (false) { // final survey
    pop->Print_Sorted_Scores();
    org0 = pop->ScoreDexv.at(pop->ScoreDexv.size()-1);
    org0 = pop->forestv[0]->tenant;
    if (false) {
      bugprintf("Org 0, gen:%li, ", GensToSuccess);
      org0->Print_Me();
    }
    bugprintf("Org 0, GensToSuccess:%li, ", GensToSuccess);
    AvgSize = SumSize/(double)GensToSuccess;
    bugprintf("size:%i, MaxSize:%i, AvgSize:%i\n", org0->Wav.size(), MaxSize, AvgSize);
  }
  if(false){
    double t0 = FullTime(tm0);
    double t1 = FullTime(tm1);
    double delta = t1-t0;
    bugprintf("delta T:%f,  minutes:%f,  hours:%f\n", delta, delta/60.0, delta/3600.0);
    bugprintf("Pop_Delete! %f\n", pop->forestv[0]->tenant->Score[0]);
  }
  delete pop;
}
/* ********************************************************************** */
void RepeatPopTest() {
  NumTrials = 0;
  TotalGensToSuccess=0; AvgGensToSuccess=0;
  for (int cnt=0;cnt<300;cnt++){
    NumTrials++;
    printf("TrialNum:%li, ", NumTrials);
    PopSession();
    printf("GensToSuccess:%li, ", GensToSuccess);
    printf("AvgGensToSuccess:%f\n", AvgGensToSuccess);
  }
}
/* ********************************************************************** */
int main() {
  // FeedTest(); return 0;
  //usleep(30*1000000L);// thirty seconds
  printf("main()\n");
  srand(time(NULL));
  RepeatPopTest(); return 0;
  PopSession(); return 0;
  return 0;
}
