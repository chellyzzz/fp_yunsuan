package fp_yunsuan

import chisel3._
import chisel3.util._
import chisel3.stage._
import scala.collection.mutable.ListBuffer
import javax.swing.InputMap

import fp_yunsuan._

class CSA_Nto2With3to2MainPipeline(n :Int, width :Int, pipeLevel :Int) extends Module{
  val io = IO(new Bundle() {
    val fire    = Input(Bool())
    val in      = Input(Vec(n,UInt(width.W)))
    val out_sum = Output(UInt(width.W))
    val out_car = Output(UInt(width.W))

  })
  val fire = io.fire
  val in = ListBuffer[UInt]()
  io.in.foreach(a => in += a)
  val in_next = ListBuffer[UInt]()
  var n_next = n
  var CSA4to2Num = if(n_next==8 || n_next==4) n_next/4 else 0
  var CSA3to2Num = if(n_next==8 || n_next==4) 0 else n_next/3
  var remainder = n_next - CSA4to2Num*4 - CSA3to2Num*3

  var ceng = 0
  var is_piped = false
  while (CSA4to2Num!=0 || CSA3to2Num!=0){
    ceng = ceng + 1

    if (ceng == pipeLevel) is_piped = true

    in_next.remove(0,in_next.length)
    for (i <- 0 until CSA4to2Num) {
      val U_CSA4to2 = Module(new CSA4to2(width = width))
      U_CSA4to2.io.in_a := (if (is_piped) RegEnable(in(i*4+0), fire) else in(i*4+0))
      U_CSA4to2.io.in_b := (if (is_piped) RegEnable(in(i*4+1), fire) else in(i*4+1))
      U_CSA4to2.io.in_c := (if (is_piped) RegEnable(in(i*4+2), fire) else in(i*4+2))
      U_CSA4to2.io.in_d := (if (is_piped) RegEnable(in(i*4+3), fire) else in(i*4+3))
      in_next += U_CSA4to2.io.out_sum
      in_next += U_CSA4to2.io.out_car
    }
    for (i <- 0 until CSA3to2Num) {
      val U_CSA3to2 = Module(new CSA3to2(width = width))
      U_CSA3to2.io.in_a := (if (is_piped) RegEnable(in(i*3+0), fire) else in(i*3+0))
      U_CSA3to2.io.in_b := (if (is_piped) RegEnable(in(i*3+1), fire) else in(i*3+1))
      U_CSA3to2.io.in_c := (if (is_piped) RegEnable(in(i*3+2), fire) else in(i*3+2))
      in_next += U_CSA3to2.io.out_sum
      in_next += U_CSA3to2.io.out_car
    }

    if (remainder == 1) in_next += in.last
    if (remainder == 2) {
      in_next += in(in.length-2)
      in_next += in.last
    }
    in.remove(0,in.length)
    in_next.foreach(a => in += a)
    n_next = (CSA4to2Num+CSA3to2Num)*2 + remainder
    CSA4to2Num = if(n_next==8 || n_next==4) n_next/4 else 0
    CSA3to2Num = if(n_next==8 || n_next==4) 0 else n_next/3
    remainder = n_next - CSA4to2Num*4 - CSA3to2Num*3
  }

  io.out_sum := (if (pipeLevel>ceng) RegEnable(in_next(0), fire) else in_next(0))
  io.out_car := (if (pipeLevel>ceng) RegEnable(in_next(1), fire) else in_next(1))



}


class CSA3to2(width :Int) extends Module{
  val io = IO(new Bundle() {
    val in_a   = Input(UInt(width.W))
    val in_b   = Input(UInt(width.W))
    val in_c   = Input(UInt(width.W))
    val out_sum = Output(UInt(width.W))
    val out_car = Output(UInt(width.W))

  })
  io.out_sum := io.in_a ^ io.in_b ^ io.in_c
  io.out_car := Cat( ( (io.in_a & io.in_b) | (io.in_a & io.in_c) | (io.in_b & io.in_c) )(width-2,0),0.U)



}

class CSA4to2(width :Int) extends Module{
  val io = IO(new Bundle() {
    val in_a   = Input(UInt(width.W))
    val in_b   = Input(UInt(width.W))
    val in_c   = Input(UInt(width.W))
    val in_d   = Input(UInt(width.W))
    val out_sum = Output(UInt(width.W))
    val out_car = Output(UInt(width.W))

  })
  val cout_vec  = Wire(Vec(width,UInt(1.W)))
  val sum_vec   = Wire(Vec(width,UInt(1.W)))
  val carry_vec = Wire(Vec(width,UInt(1.W)))
  val cin = 0.U
  for (i <- 0 until width) {
    cout_vec(i) := Mux(io.in_a(i) ^ io.in_b(i), io.in_c(i), io.in_a(i))
    if (i ==0){
      sum_vec(i)   := io.in_a(i) ^ io.in_b(i) ^ io.in_c(i) ^ io.in_d(i)
      carry_vec(i) := Mux(io.in_a(i) ^ io.in_b(i) ^ io.in_c(i) ^ io.in_d(i), cin , io.in_d(i))
    }
    else {
      sum_vec(i)   :=  io.in_a(i) ^ io.in_b(i) ^ io.in_c(i) ^ io.in_d(i) ^ cout_vec(i-1)
      carry_vec(i) := Mux(io.in_a(i) ^ io.in_b(i) ^ io.in_c(i) ^ io.in_d(i), cout_vec(i-1), io.in_d(i))
    }
  }

  val sum_temp_vec   = Wire(Vec(width,UInt(1.W)))
  val carry_temp_vec = Wire(Vec(width,UInt(1.W)))
  carry_temp_vec(0) := 0.U
  sum_temp_vec(0)   := sum_vec(0)
  for (i <- 1 until width) {
    if (i%2==1) {
      carry_temp_vec(i) := sum_vec(i)
      sum_temp_vec(i)   := carry_vec(i-1)
    }
    else {
      carry_temp_vec(i) := carry_vec(i-1)
      sum_temp_vec(i)   := sum_vec(i)
    }
  }


  io.out_sum := sum_temp_vec.asUInt
  io.out_car := carry_temp_vec.asUInt


}

