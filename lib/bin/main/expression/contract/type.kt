package expression.contract

import com.google.gson.*

enum class Kind(val key: String) {
    Any("Any"),
    string("string"),
    integer("integer"),
    decimal("decimal"),
    number("number"),
    boolean("boolean"),
    date("date"),
    dateTime("dateTime"),
    time("time"),
    void("void"),
    obj("obj"),
    list("list")
}

data class PropertyType(val name: String, val type: Type?)

data class ObjType(val properties: List<PropertyType>)

data class ListType(val items: Type)

class Type {
    val kind: Kind
    var obj: ObjType?
    var list: ListType?
    constructor(kind: Kind, obj: ObjType? = null, list: ListType? = null) {
        this.kind = kind
        this.obj = obj
        this.list = list
    }

    companion object {
        val any
            get(): Type {
                return Type(Kind.Any)
            }
        val string
            get(): Type {
                return Type(Kind.string)
            }
        val integer
            get(): Type {
                return Type(Kind.integer)
            }
        val decimal
            get(): Type {
                return Type(Kind.decimal)
            }
        val number
            get(): Type {
                return Type(Kind.number)
            }
        val boolean
            get(): Type {
                return Type(Kind.boolean)
            }
        val date
            get(): Type {
                return Type(Kind.date)
            }
        val dateTime
            get(): Type {
                return Type(Kind.dateTime)
            }
        val time
            get(): Type {
                return Type(Kind.time)
            }
        val void
            get(): Type {
                return Type(Kind.void)
            }

        fun Obj(properties: List<PropertyType> = arrayListOf<PropertyType>()): Type {
            return Type(Kind.obj, ObjType(properties))
        }

        fun List(items: Type): Type {
            return Type(Kind.list, null, ListType(items))
        }

        fun isPrimitive(type: Type): Boolean {
            return Type.isPrimitive(type.kind.toString())
        }

        fun isPrimitive(type: String): Boolean {
            val primitives =
                    arrayOf(
                            "string",
                            "integer",
                            "decimal",
                            "number",
                            "boolean",
                            "date",
                            "dateTime",
                            "time"
                    )
            return primitives.contains(type)
        }

        fun to(kind: String): Type {
            return Type(Kind.valueOf(kind))
        }

        fun to(kind: Kind): Type {
            return Type(kind)
        }

        fun get(value: Any?): Type {
            if (value == null) {
                return Type.any
            } else if (value is Array<*> && value.isArrayOf<Any>()) {
                @Suppress("UNCHECKED_CAST") val array = value as List<Any>
                if (array.size > 0) {
                    return Type.List(this.get(value[0]))
                }
                return Type.any
            } else if (value is String) {
                // TODO determinar si es fecha.
                return Type.string
            } else if (value is Float) {
                return Type.decimal
            } else if (value is Int) {
                return Type.integer
            } else if (value is Boolean) {
                return Type.boolean
            } else if (value is Map<*, *>) {
                @Suppress("UNCHECKED_CAST") val map = value as Map<String, Any>
                val properties = ArrayList<PropertyType>()
                for (entry in map.entries) {
                    properties.add(PropertyType(entry.key, Type.get(entry.value)))
                }
                return Type.Obj(properties)
            }
            return Type.any
        }

        fun isList(type: String): Boolean {
            return type.startsWith("[") && type.endsWith("]")
        }

        fun isList(type: Type): Boolean {

            return type.kind === Kind.list
        }

        fun isObj(type: String): Boolean {
            return type.startsWith("{") && type.endsWith("}")
        }

        fun isObj(type: Type): Boolean {
            return type.kind === Kind.obj
        }

        fun toString(type: Type?): String {
            if (type == null) {
                return "any"
            }
            if (Type.isPrimitive(type)) {
                return type.kind.toString()
            }
            if (Type.isObj(type)) {
                val properties = arrayListOf<String>()
                val objectType = type.obj as ObjType
                for (propertyType in objectType.properties) {
                    properties.add("${propertyType.name}:${Type.toString(propertyType.type)}")
                }
                return "{" + properties.joinToString(",") + "}"
            }
            if (this.isList(type)) {
                val arrayType = type.list as ListType
                return "[" + Type.toString(arrayType.items) + "]"
            }
            return "any"
        }

        fun serialize(type: Type?): String? {
            if (type === null) {
                return null
            }
            val gson = Gson()
            return gson.toJson(type)
        }

        fun deserialize(type: String?): Type? {
            if (type != null && type.trim() != "") {
                return Gson().fromJson<Type>(type, Type::class.java)
            } else {
                return null
            }
        }
    }
}
