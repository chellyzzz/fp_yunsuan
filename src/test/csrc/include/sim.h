#ifndef __SIM_H__
#define __SIM_H__

#include <stdint.h>

#define XLEN 64
void single_cycle();
void reset(int n);
void sim_init(int argc, char *argv[]);
void sim_exit();
void sim_main(int argc, char *argv[]);


typedef struct {
    uint64_t vs1;
    uint64_t vs2;
    uint64_t vs3;
    uint64_t vd;
    uint64_t expected_vd;
    uint64_t round_mode;
    uint64_t fp_format;
} IOput;

#endif