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

    fun isEmpty (value:Any?):Boolean {
		return value == null || value.toString().trim().length == 0
	}
    fun isNotEmpty (value: Any?): Boolean {
		return !this.isEmpty(value)
	}

    fun isBooleanFormat (value: Any?): Boolean {
		if (value == null) {
			return false
		}
		return value.toString() == "true" || value.toString() == "false"
	}

    fun isPositiveInteger(value: Any?): Boolean {
        if (value == null || value !is Int) {
            return false
        }
        return value >= 0
    }

	fun isNumberFormat (value: String?): Boolean {
        if (value == null ) {
			return false
		}
		return this.isDecimalFormat(value)
	}

	fun isIntegerFormat (value: String?): Boolean {
		if (value == null ) {
			return false
		}
		return this.reInt.matches(value)
	}

	fun isDecimalFormat (value: String?): Boolean {
		if (value == null ) {
			return false
		}
		return this.reDecimal.matches(value)
	}

    fun isAlphanumeric (value: Char?): Boolean {
		if (value == null ) {
			return false
		}
		return this.isAlphanumeric(value.toString())
	}
	fun isAlphanumeric (value: String?): Boolean {
		if (value == null ) {
			return false
		}
		return this.reAlphanumeric.matches(value)
	}

	fun isAlpha (value: String?): Boolean {
		if (value == null ) {
			return false
		}
		return this.reAlpha.matches(value)
	}

	fun isDateFormat (value: String?): Boolean {
		if (value == null ) {
			return false
		}
		return this.reDate.matches(value)
	}

	fun isDateTimeFormat (value: String?): Boolean {
        if (value == null ) {
			return false
		}
		return this.reDateTime.matches(value)
	}

	fun isTimeFormat (value: String?): Boolean {
		if (value == null ) {
			return false
		}
		return this.reTime.matches(value)
	}
}
