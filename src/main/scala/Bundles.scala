package fp_yunsuan

import chisel3._
import chisel3.util._

object VfaddOpCode {
  def dummy    = "b11111".U(5.W)
  def fadd     = "b00000".U(5.W)
  def fsub     = "b00001".U(5.W)
  def fmin     = "b00010".U(5.W)
  def fmax     = "b00011".U(5.W)
  def fmerge   = "b00100".U(5.W)
  def fmove    = "b00101".U(5.W)
  def fsgnj    = "b00110".U(5.W)
  def fsgnjn   = "b00111".U(5.W)
  def fsgnjx   = "b01000".U(5.W)
  def feq      = "b01001".U(5.W)
  def fne      = "b01010".U(5.W)
  def flt      = "b01011".U(5.W)
  def fle      = "b01100".U(5.W)
  def fgt      = "b01101".U(5.W)
  def fge      = "b01110".U(5.W)
  def fclass   = "b01111".U(5.W)
  def fmv_f_s  = "b10001".U(5.W)
  def fmv_s_f  = "b10010".U(5.W)
  def fsum_ure = "b11010".U(5.W) // unordered
  def fmin_re  = "b10100".U(5.W)
  def fmax_re  = "b10101".U(5.W)
  def fsum_ore = "b10110".U(5.W) // ordered
  def fminm    = "b11110".U(5.W)
  def fmaxm    = "b10011".U(5.W)
  def fleq     = "b11100".U(5.W)
  def fltq     = "b11011".U(5.W)
}

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

object VectorElementFormat {
  def width = 2

  def b = "b00".U(width.W)
  def h = "b01".U(width.W)  // f16
  def w = "b10".U(width.W)  // f32
  def d = "b11".U(width.W)  // f64

  def apply() = UInt(width.W)
}
