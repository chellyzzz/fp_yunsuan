// src/main/scala/top.scala
package top

import chisel3._
import chisel3.util._
import chisel3.stage._

import fp_yunsuan._

class top extends Module{
  val io = IO(new Bundle {
    val fire          = Input(Bool())
    val opcode        = Input(UInt(4.W))
    val round_mode    = Input(UInt(3.W))
    val fp_format     = Input(UInt(2.W))
    val vs2           = Input(UInt(64.W))
    val vs1           = Input(UInt(64.W))
    val vs3           = Input(UInt(64.W))
  
    val vd            = Output(UInt(64.W))
    val fflags        = Output(UInt(5.W))
    val mixed_vd      = Output(UInt(64.W))
    val mixed_fflags  = Output(UInt(5.W))
  })

  val mix_fmul = Module(new FloatFMAMixedWithDifferentFormat(support_fp64 = true, support_fp32 = true, support_fp16 = true, support_bf16 = true))
  mix_fmul.io.fire := io.fire
  mix_fmul.io.fp_a := io.vs1
  mix_fmul.io.fp_b := io.vs2
  mix_fmul.io.fp_c := io.vs3

  mix_fmul.io.round_mode := io.round_mode
  mix_fmul.io.fp_format := io.fp_format
  mix_fmul.io.op_code := io.opcode
  mix_fmul.io.fp_aIsFpCanonicalNAN := 0.U 
  mix_fmul.io.fp_bIsFpCanonicalNAN := 0.U 
  mix_fmul.io.fp_cIsFpCanonicalNAN := 0.U 

  io.mixed_vd     := mix_fmul.io.fp_result  
  io.mixed_fflags := mix_fmul.io.fflags

  val fmul = Module(new SingleWidthFloatFMA(exponentWidth = 8, significandWidth = 24))
  fmul.io.fire := io.fire
  fmul.io.fp_a :=  io.vs1
  fmul.io.fp_b :=  io.vs2
  fmul.io.fp_c :=  io.vs3

  fmul.io.round_mode := io.round_mode
  fmul.io.fp_format := io.fp_format
  fmul.io.op_code := io.opcode
  fmul.io.fp_aIsFpCanonicalNAN := 0.U 
  fmul.io.fp_bIsFpCanonicalNAN := 0.U 
  fmul.io.fp_cIsFpCanonicalNAN := 0.U 

  io.vd     := fmul.io.fp_result  
  io.fflags := fmul.io.fflags
}

object topMain extends App {
  (new ChiselStage).emitVerilog(new top, args)
}