package h3lp

class ObjectHelper(val http: HttpHelper, val validator: Validator) {

    fun names(value: String): List<String> {
        if (value == ".") {
            // in case "".[0].name" where var is "."
            return listOf(value)
        } else if (value.startsWith("..")) {
            // in case ".name.filter"
            return listOf(".") + value.substring(2).split(".")
        } else if (value.startsWith(".")) {
            // in case ".name.filter"
            return listOf(".") + value.substring(1).split(".")
        } else {
            return value.split(".")
        }
    }

    fun getValue(source: Any, _name: String): Any? {
        val names = this.names(_name)
        var value = source
        for (name in names) {
            if (value is Array<*> && value.isArrayOf<Any>()) {
                // Example: orders.0.x
                val index = name.toIntOrNull()
                if (index != null) {
                    @Suppress("UNCHECKED_CAST") 
                    value = (value as List<Any>)[index]
                    continue
                }
                @Suppress("UNCHECKED_CAST") val array = value as List<Map<String, Any>>
                var result: ArrayList<Any> = ArrayList<Any>()
                for (item in array) {
                    if (item.containsKey(name)) {
                        val itemValue = item[name] as Any
                        if (itemValue is Array<*> && itemValue.isArrayOf<Any>()) {
                            // Example: orders.x[]
                            @Suppress("UNCHECKED_CAST") result.addAll(itemValue as List<Any>)
                        } else {
                            // Example: orders.x.x
                            result.add(itemValue)
                        }
                    }
                }
                value = result
            } else {
                @Suppress("UNCHECKED_CAST") val map = value as Map<String, Any>
                if (!map.containsKey(name)) {
                    return null
                }
                value = map.get(name) as Any
            }
        }
        return value
    }

    fun setValue(source: Any, _name: String, value: Any ?): Boolean {
        val names = _name.split('.')
        val level = names.size - 1
        var data = source
        for (i in names.indices) {
            val name = names[i]
            // if is an array and name is a positive integer
            val index = name.toIntOrNull()
            if (data is Array<*> && data.isArrayOf<Any>() && index != null) {
                // If the index exceeds the length of the array, nothing assigns it.
                if (index >= data.size) {
                    return false
                }
                if (i == level) {
                    @Suppress("UNCHECKED_CAST") 
                    (data as Array<Any?>)[index] = value
                } else {
                    @Suppress("UNCHECKED_CAST") 
                    data = (data as Array<Any>)[index]
                }
            } else {
                @Suppress("UNCHECKED_CAST") val map = data as MutableMap<String, Any?>
                if (i == level) {
                    map.set(name, value)
                } else if (map.containsKey(name)) {
                    data = map.get(name) as Any
                } else {
                    return false
                }
            }
        }
        return true
    }
}
