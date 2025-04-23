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
    val vs2           = Input(UInt(32.W))
    val vs1           = Input(UInt(32.W))
    val vs3           = Input(UInt(32.W))
  
    val vd            = Output(UInt(32.W))
    val fflags        = Output(UInt(5.W))
    val mixed_vd      = Output(UInt(32.W))
    val mixed_fflags  = Output(UInt(5.W))
  })

  io.mixed_vd     := 0.U
  io.mixed_fflags := 0.U

  val fadd = Module(new FloatAdderMixedFP16BF16)
  fadd.io.fire :=  io.fire
  fadd.io.fp_a :=  io.vs1
  fadd.io.fp_b :=  io.vs2
  fadd.io.is_frs1 := false.B
  fadd.io.frs1 :=  io.vs3

  fadd.io.round_mode := io.round_mode
  fadd.io.mask := 0.U
  fadd.io.fp_format := io.fp_format
  fadd.io.op_code := io.opcode
  fadd.io.fp_aIsFpCanonicalNAN := 0.U 
  fadd.io.fp_bIsFpCanonicalNAN := 0.U 

  io.vd     := fadd.io.fp_result  
  io.fflags := fadd.io.fflags
}

object topMain extends App {
  (new ChiselStage).emitVerilog(new top, args)
}