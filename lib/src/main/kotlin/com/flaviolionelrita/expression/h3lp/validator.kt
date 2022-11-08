package com.flaviolionelrita.h3lp

class Validator {
    private val reInt: Regex = "^[0-9]+$/".toRegex()
    private val reDecimal: Regex = "^[0-9]*[.][0-9]+$".toRegex()
    private val reAlphanumeric: Regex = "[a-zA-Z0-9_.]+$".toRegex()
    private val reAlpha: Regex = "[a-zA-Z]+$".toRegex()
    private val reDate: Regex = "^\\d{4}-\\d{2}-\\d{2}$".toRegex()
    private val reDateTime: Regex =
            "\\d{4}-[01]\\d-[0-3]\\dT[0-2]\\d:[0-5]\\d:[0-5]\\d\\.\\d+([+-][0-2]\\d:[0-5]\\d|Z)".toRegex()
    private val reTime: Regex = "\\[0-2]\\d:[0-5]\\d:[0-5]\\d".toRegex()
    fun isPositiveInteger(value: Any): Boolean {
        if (value !is Int) {
            return false
        }
        return value >= 0
    }
}
