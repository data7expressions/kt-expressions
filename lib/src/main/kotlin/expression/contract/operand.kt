package expression.contract

enum class OperandType(val key: String) {
    Const("Const"),
    Var("Var"),
    Env("Env"),
    Property("Property"),
    Template("Template"),
    KeyVal("KeyVal"),
    List("List"),
    Obj("Obj"),
    Operator("Operator"),
    CallFunc("CallFunc"),
    Arrow("Arrow"),
    ChildFunc("ChildFunc"),
    Block("Block"),
    If("If"),
    ElseIf("ElseIf"),
    Else("Else"),
    While("While"),
    For("For"),
    ForIn("ForIn"),
    Switch("Switch"),
    Case("Case"),
    Default("Default"),
    Break("Break"),
    Continue("Continue"),
    Func("Func"),
    Return("Return"),
    Try("Try"),
    Catch("Catch"),
    Throw("Throw"),
    Args("Args")
}

data class ParameterDoc(val name: String, val description: String)

data class OperatorDoc(val description: String, val params: List<ParameterDoc>)

data class OperatorAdditionalInfo(val priority: Int, val doc: OperatorDoc?)

data class FunctionAdditionalInfo(val deterministic: Boolean?, val doc: OperatorDoc?)

interface IEvaluator {
    fun eval(context: Context): Any
}

interface IPrototypeEvaluator : IEvaluator {
    fun clone(operand: Operand): IEvaluator
}

class Operand {
    val pos: Pair<Int, Int>
    val name: Any
    val type: OperandType
    val children: ArrayList<Operand>
    val returnType: Type?
    var evaluator: IEvaluator? = null
    var number: Int? = null
    var id: String? = null
    constructor(
            pos: Pair<Int, Int>,
            name: Any,
            type: OperandType,
            children: ArrayList<Operand> = arrayListOf<Operand>(),
            returnType: Type? = null
    ) {
        this.pos = pos
        this.name = name
        this.type = type
        this.children = children
        this.returnType = returnType
    }
    fun eval(context: Context): Any {
        if (this.evaluator == null) {
            throw Exception("Evaluator not implemented")
        }
        return (this.evaluator as IEvaluator).eval(context)
    }
}

data class OperandMetadata(
        val type: OperandType,
        val name: String,
        val children: List<OperandMetadata>?,
        var returnType: String?,
        var number: Int?
)

data class OperatorMetadata(
        val params: List<Parameter>,
        val deterministic: Boolean,
        val operands: Int,
        val returnType: String,
        val doc: OperatorDoc?,
        val priority: Int?,
        val function: Any,
        val custom: IPrototypeEvaluator?
)
