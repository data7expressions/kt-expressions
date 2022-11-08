package expression.contract

interface ISerializer<T> {
    fun serialize(value: T): Any
    fun deserialize(value: Any): T
    fun clone(value: T): T
}

interface IBuilder<T> {
    fun build(): T
}

data class Parameter(
        val name: String,
        val type: String? = null,
        val default: Any? = null,
        val value: Any? = null,
        val multiple: Boolean? = null
)

data class Sing(
        val name: String,
        val params: List<Parameter>,
        val returnType: String,
        val isAsync: Boolean
)

data class Format(val name: String, val pattern: String, val regExp: Regex)
