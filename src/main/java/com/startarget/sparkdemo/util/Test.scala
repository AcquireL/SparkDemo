package com.startarget.sparkdemo.util

object Test {
  private def parseToDtcCode(value: String): String = {
    try {
      var rawCode = value.toInt
      val codeBuffer = StringBuilder.newBuilder

      if (rawCode > 0xC000) {
        // C0-E9 U
        rawCode = rawCode - 0xC000
        codeBuffer.append("U")
      }
      else if (rawCode > 0x8000) {
        // 80-B9 B
        rawCode = rawCode - 0x8000
        codeBuffer.append("B")
      }
      else if (rawCode > 0x4000) {
        // 40-79 C
        rawCode = rawCode - 0x4000
        codeBuffer.append("C")
      } else {
        // 00-39 P
        codeBuffer.append("P")
      }

      val hex = "0000" + Integer.toHexString(rawCode).toUpperCase()
      val formatHex = hex.substring(hex.length - 4)
      codeBuffer.append(formatHex)

      return codeBuffer.toString()
    } catch {
      case e: Exception => {
        return null
      }
    }
  }

  def main(args: Array[String]): Unit = {
    print(Test.parseToDtcCode("50208"))
  }
}
