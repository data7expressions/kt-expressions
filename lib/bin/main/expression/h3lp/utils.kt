package h3lp

import kotlin.collections.mutableListOf
import kotlin.collections.arrayListOf
import com.google.gson.Gson

class ContextReplacer(val obj: ObjectHelper) : IReplacer {
	private var _context: Any? = null

	fun context (value :Any): ContextReplacer {
		this._context = value
		return this
	}

    override fun replace(match: String): String? { 
        if (this._context != null) {
            return this.obj.getValue(this._context as Any, match).toString()
        } else {
            return null
        }
    }
}


class UtilsHelper(val obj: ObjectHelper, val validator: Validator) {
 
    public fun tryParse (value: String): Any? {
		try {
            if (value.trim().startsWith("[")) {
                return Gson().fromJson<ArrayList<Any>>(value, ArrayList::class.java)
            } else if (value.trim().startsWith("{")) {
                return Gson().fromJson<Map<String, Any?>>(value, Map::class.java)
            }
			return null            
		} catch(error:Exception) {
			return null
		}
	}

    public fun createContextReplacer(): ContextReplacer {
        return ContextReplacer(this.obj)
    }

    public fun template (template: Any, replacer: Any, parse: Boolean?): String {
        return this.template(template, this.createContextReplacer().context(replacer), parse)
    } 

    public fun template (template: Any, replacer: IReplacer, parse: Boolean?): String {
		val _parse:Boolean = parse?:false        
        val result = this.anyTemplate(template, replacer, _parse)
        if (result == null) {
            return ""
        }
        return result as String
	}

    private fun anyTemplate (source: Any, replacer: IReplacer, parse: Boolean): Any {
		if (source is Array<*>) {
			val result = arrayListOf<Any>()
            @Suppress("UNCHECKED_CAST")
            val list = source as List<Any>            
			for (item in list) {
				result.add(this.anyTemplate(item, replacer, parse))
			}
			return result            
		} else if (source is Map<*,*>) {            
			val result = mutableMapOf<String,Any?>()
            @Suppress("UNCHECKED_CAST")
            val obj = source as Map<String, Any>
			for (entry in obj.entries ) {
				result.set(entry.key ,this.anyTemplate(entry.value, replacer, parse))
			}
		} else if (source is String) {
			val result = this.stringTemplate(source, replacer)
			if (parse) {
				val obj = this.tryParse(result)
				if( obj != null) return obj
                return result
			} else {
				return result
			}
		} 
		return source		
	}

    private fun stringTemplate(template: String, replacer: IReplacer): String {
        val buffer = template.toCharArray().toTypedArray()
		val length = buffer.size
		val result = mutableListOf<String>()
		var chars = mutableListOf<String>()
		var isVar = false
		var close: Char = ' '
        var index = 0
        val parenthesisOpen = "{".toCharArray()[0]
        val parenthesisClose= "}".toCharArray()[0]
        while(index < length) {       	
			val current = buffer[index]
			if (isVar) {
				if (current  == close) {
					val match = chars.joinToString()
					val value = this.replace(match, close, replacer)
					result.add(value)
					if (close == ' ') {
						result.add(" ")
					}
					chars.clear()
					isVar = false
					close = ' '
				} else {
					chars.add(current.toString())
				}
			} else if (
				index < length - 1 &&
				current == '$' &&
				buffer[index + 1] == parenthesisOpen
			) {
				// Example: ${XXX}
				isVar = true
				close = parenthesisClose
				index++
			} else if (
				index < length - 1 &&
				current == '$' &&
				buffer[index + 1] != ' '
			) {
				// Example: $XXX
				isVar = true
				close = ' '
			} else {
				result.add(current.toString())
			}
            index++
		}
		if (chars.size > 0) {
			if (close == parenthesisClose) {
				// Example: 'words ${XXXX'
				result.add("\${")
				result.add(chars.joinToString())
			} else {
				// Example: 'words $XXXX'
				val value = this.replace(chars.joinToString(), close, replacer)
				result.add(value)
			}
		}
		return result.joinToString()
    }

    private fun replace (match:String, close:Char, replacer: IReplacer):String {
        val parenthesisClose= "}".toCharArray()[0]
		val value = replacer.replace(match)
		if (value != null) {
			return this.stringTemplate(value, replacer)
		} else if (close == parenthesisClose) {
			return "\${" + match + "}"
		} else {
			return "$" + match
		}
	}

}