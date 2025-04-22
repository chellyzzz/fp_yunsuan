package fp_yunsuan

import chisel3._
import chisel3.util._

object FmaOpCode {
  def width = 4
  def fmul    = "b0000".U(width.W)
  def fmacc   = "b0001".U(width.W)
  def fmsac   = "b0011".U(width.W)
  def fnmacc  = "b0010".U(width.W)
  def fnmsac  = "b0100".U(width.W)
}

object GatedValidRegNext {
  def apply(next: Bool, init: Bool = false.B): Bool = {
    val last = Wire(next.cloneType)
    last := RegEnable(next, init, next || last)
    last
  }
}

object LZD {
  def apply(in: UInt): UInt = PriorityEncoder(Reverse(Cat(in, 1.U)))
}