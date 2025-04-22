// sim_c/sim.cc
#include <iostream>
#include <bitset>

#include <verilated.h>
#include "Vtop.h"
#ifdef VCD
    #include "verilated_vcd_c.h"
    VerilatedVcdC* tfp = nullptr;
#endif

#include "sim.h"
#include "fp.h"

using namespace std; 

// init pointers
const std::unique_ptr<VerilatedContext> contextp{new VerilatedContext};
const std::unique_ptr<Vtop> top{new Vtop{contextp.get(), "TOP"}};

IOput Sim_IO;

/* sim initial */
void sim_init(int argc, char *argv[]) {
    top->reset = 1;
    top->clock = 0;
#ifdef VCD
    contextp->traceEverOn(true);
    tfp = new VerilatedVcdC;
    top->trace(tfp, 99);
    tfp->open("build/top.vcd");
#endif
    Verilated::commandArgs(argc,argv);
}

/* sim exit */
void sim_exit() {
    // finish work, delete pointer
    top->final();
#if VCD
    tfp->close();
    tfp = nullptr;
#endif
}


void display(){

}

void single_cycle() {
    contextp->timeInc(1);
    top->clock = 0; top->eval();
#ifdef VCD
 tfp->dump(contextp->time());
#endif

    contextp->timeInc(1);
    top->clock = 1; top->eval();
#ifdef VCD
 tfp->dump(contextp->time());
#endif
}

void gen_rand_vctrl() {

    Sim_IO.round_mode = 0;
    Sim_IO.fp_format = 2;

    top->io_fire = 1;
    top->io_opcode = 0x0;
    top->io_round_mode = Sim_IO.round_mode;
    top->io_fp_format = Sim_IO.fp_format;

}

void get_expected_result() {
}


void gen_rand_input() {
    // 数值 (Decimal)	FP16 二进制	十六进制 (Hex)
    // 1.0	0 01111 0000000000	0x3C00
    // 2.0	0 10000 0000000000	0x4000
    // 3.0	0 10000 1000000000	0x4200
    // 4.0	0 10001 0000000000	0x4400
    // 5.0	0 10001 0100000000	0x4560
    // 6.0	0 10001 1000000000	0x4600
    // 7.0	0 10001 1100000000	0x46E0
    // 8.0	0 10010 0000000000	0x4800
    // 9.0	0 10010 0010000000	0x3C00
    // 10.0	0 10010 0100000000	0x4900

    gen_rand_vctrl();
    Sim_IO.vs1 = 0x3f800000;
    Sim_IO.vs2 = 0x3f800000;
    Sim_IO.vs3 = 0x3f800000;
    get_expected_result();

    top->io_vs1 = Sim_IO.vs1;
    top->io_vs2 = Sim_IO.vs2;
    top->io_vs3 = Sim_IO.vs3;

}

void check_result(){
    return ;
}
void get_output() {
    // TODO:
    Sim_IO.vd = top->io_vd;
}   

void reset(int n) {
    top->reset = 1;
    while (n-- > 0) single_cycle();
    top->reset = 0;
    top->eval();
}

void sim_main(int argc, char *argv[]) {
    int max_cycles = 20;  
    sim_init(argc, argv);
    reset(10);
    gen_rand_input();
    single_cycle();
    /* main loop */
    while (max_cycles--) {
        single_cycle();
    }
    get_output();
    display();

    sim_exit();
}