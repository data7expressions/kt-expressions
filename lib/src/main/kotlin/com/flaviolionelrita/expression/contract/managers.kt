package com.flaviolionelrita.expression.contract
import kotlin.reflect.KFunction

data class ActionObserverArgs(
        val expression: String,
        val context: Context,
        val result: Any?,
        val error: Any?
)

abstract class ActionObserver(val condition: String? = null) {
    abstract fun before(args: ActionObserverArgs)
    abstract fun after(args: ActionObserverArgs)
    abstract fun error(args: ActionObserverArgs)
}

interface ITypeManager {
    fun type(operand: Operand): Type
    fun parameters(operand: Operand): List<Parameter>
}

interface IModelManager {
    // https://stackoverflow.com/questions/56026971/kotlin-interface-property-only-require-public-getter-without-public-setter
    val enums: List<Pair<String, List<Pair<String, Any>>>> get
    val formats: List<Pair<String, Format>> get
    val constants: List<Pair<String, Any>> get
    val operators: List<Pair<String, OperatorMetadata>> get
    val functions: List<Pair<String, OperatorMetadata>> get 
    fun addEnum(name: String, values: List<Pair<String, Any>>)
    fun addEnum(name: String, values: Any)
    fun addConstant(key: String, value: Any)
    fun addFormat(key: String, pattern: String)
    fun addOperator(sing: String, source: KFunction<*>, info: OperatorAdditionalInfo)
    fun addOperator(sing: String, source: IPrototypeEvaluator, info: OperatorAdditionalInfo)
    fun addFunction(sing: String, source: KFunction<*>, info: FunctionAdditionalInfo?)
    fun addFunction(sing: String, source: IPrototypeEvaluator, info: FunctionAdditionalInfo?)
    fun addOperatorAlias(alias: String, reference: String)
    fun addFunctionAlias(alias: String, reference: String)
    fun getConstantValue(name: String): Any
    fun getEnumValue(name: String, option: String): Any
    fun getEnum(name: String): List<Pair<String, Any>>
    fun getFormat(name: String): Format
    fun getOperator(name: String, operands: Int?): OperatorMetadata
    fun getFunction(name: String): OperatorMetadata
    fun priority(name: String, cardinality: Int?): Int
    fun isEnum(name: String): Boolean
    fun isConstant(name: String): Boolean
    fun isOperator(name: String, operands: Int?): Boolean
    fun isFunction(name: String): Boolean
}

interface IOperandBuilder {
    fun build(expression: String): Operand
    fun clone(source: Operand): Operand
}

interface IEvaluatorFactory {
    fun create(operand: Operand): IEvaluator?
}

interface IExpressions {
    val enums: List<Pair<String, List<Pair<String, Any>>>>
    val formats: List<Pair<String, Format>>
    val constants: List<Pair<String, Any>>
    val operators: List<Pair<String, OperatorMetadata>>
    val functions: List<Pair<String, OperatorMetadata>>
    fun addEnum(name: String, values: List<Pair<String, Any>>)
    fun addEnum(name: String, values: Any)
    fun addConstant(key: String, value: Any)
    fun addFormat(key: String, pattern: String)
    fun addOperator(sing: String, source: Any, additionalInfo: OperatorAdditionalInfo)
    fun addFunction(sing: String, source: Any, additionalInfo: FunctionAdditionalInfo?)
    fun addOperatorAlias(alias: String, reference: String)
    fun addFunctionAlias(alias: String, reference: String)
    fun clone(operand: Operand): Operand
    fun parameters(expression: String): List<Parameter>
    fun type(expression: String): String
    fun eval(expression: String, data: MutableMap<String, Any>? = null): Any
    fun run(expression: String, data: MutableMap<String, Any>? = null): Any
    fun subscribe(observer: ActionObserver)
    fun unsubscribe(observer: ActionObserver)
}
