package expression.Operand
import expression.contract.*
import h3lp.h3lp
import h3lp.IReplacer
import kotlin.reflect.*

class valEvaluator(val operand:Operand) : IEvaluator {
	override fun eval (context: Context): Any ? {
		if (this.operand.returnType == null) {
			return this.operand.name
		}
		when (this.operand.returnType.kind) {
		Kind.string ->
			return this.operand.name
		Kind.boolean ->
            return this.operand.name.toString().toBoolean()
		Kind.integer ->
            return this.operand.name.toString().toIntOrNull()
		Kind.decimal ->
            return this.operand.name.toString().toFloatOrNull()
		else ->
			return this.operand.name
		}
	}
}
class VarEvaluator(val operand:Operand) : IEvaluator {
	override fun eval (context: Context): Any ? {
		return context.data.get(this.operand.name as String)
	}
}
class EnvEvaluator(val operand:Operand) : IEvaluator {
	override fun eval (context: Context): Any? {
		return System.getenv(this.operand.name as String)
	}
}

class TemplateReplacer(val context: Context): IReplacer {

	override fun replace (match: String): String? {
		var value = System.getenv(match)
		if (value == null) {
			value = this.context.data.get(match) as String?
		}
		if(value == null) {
			return match
		}
		return value
	}
}
class TemplateEvaluator(val operand:Operand) : IEvaluator {
	override fun eval (context: Context): Any ? {
		return h3lp.utils.template(this.operand.name.toString(), TemplateReplacer(context))
	}
}
class PropertyEvaluator(val operand:Operand) : IEvaluator {
	override fun eval (context: Context): Any ? {
		val value = this.operand.children[0].eval(context)
		if (value == null ) return null
		return h3lp.obj.getValue(value, this.operand.name as String)
	}
}
class ListEvaluator(val operand:Operand) : IEvaluator {
	override fun eval (context: Context): Any ? {
		val values = mutableListOf<Any ?>()
		for(child in this.operand.children) {
			values.add(child.eval(context))
		}
		return values
	}
}
class ObjEvaluator(val operand:Operand) : IEvaluator {
	override fun eval (context: Context): Any ? {
		val obj= mutableMapOf<String,Any ?>()
		for (child in this.operand.children) {
			obj.set(child.name as String, child.children[0].eval(context))
		}
		return obj
	}
}
// https://youtrack.jetbrains.com/issue/KT-8827/Kotlin-Reflection-Callable-FunctionCaller-needs-to-be-able-to-handle-default-parameters
// https://www.baeldung.com/kotlin/reflection
class CallFuncEvaluator(val operand:Operand, val function: KFunction<*>) : IEvaluator {	
	override fun eval (context: Context): Any ? {
		val args = mutableListOf<Any ?>()
		for (child in this.operand.children) {
			args.add(child.eval(context))
		}
		return this.function.call(args)
	}
}
class BlockEvaluator(val operand:Operand) : IEvaluator {
	override fun eval (context: Context): Any ? {
		var lastValue:Any ?= null
		for (child in this.operand.children) {
			lastValue = child.eval(context)
		}
		return lastValue
	}
}
class IfEvaluator(val operand:Operand) : IEvaluator {
	override fun eval (context: Context): Any ? {
		val condition = this.operand.children[0].eval(context)
		if (condition != null && condition==true) {
			val ifBlock = this.operand.children[1]
			return ifBlock.eval(context)
		} else if (this.operand.children.size > 2) {
			for (i in this.operand.children.size downTo 2){
				if (this.operand.children[i].type == OperandType.ElseIf) {
					val elseIfCondition = this.operand.children[i].children[0].eval(context)
					if (elseIfCondition==true) {
						val elseIfBlock = this.operand.children[i].children[1]
						return elseIfBlock.eval(context)
					}
				} else {
					val elseBlock = this.operand.children[i]
					return elseBlock.eval(context)
				}
			}
		}
		throw Exception("If evaluator error")
	}
}
class WhileEvaluator(val operand:Operand) : IEvaluator {
	override fun eval (context: Context): Any ? {
		var lastValue:Any ?= null
		val condition = this.operand.children[0]
		val block = this.operand.children[1]
		while (condition.eval(context) == true) {
			lastValue = block.eval(context)
		}
		return lastValue
	}
}
class ForEvaluator(val operand:Operand) : IEvaluator {
	override fun eval (context: Context): Any ? {
		var lastValue:Any ?= null
		val initialize = this.operand.children[0]
		val condition = this.operand.children[1]
		val increment = this.operand.children[2]
		val block = this.operand.children[3]
		initialize.eval(context)
		while (condition.eval(context)==true) {
			lastValue = block.eval(context)
			increment.eval(context)
		}
		return lastValue
	}
}
class ForInEvaluator(val operand:Operand) : IEvaluator {
	override fun eval (context: Context): Any ? {
		var lastValue:Any ? = null
		val item = this.operand.children[0]
		val list = this.operand.children[1].eval(context) as List<Any ?>
		val block = this.operand.children[2]
		for (i in list.indices ) {
			val value = list[i]
			context.data.set(item.name as String, value)
			
			lastValue = block.eval(context)
		}
		return lastValue
	}
}
class SwitchEvaluator(val operand:Operand) : IEvaluator {
	override fun eval (context: Context): Any? {
		val value = this.operand.children[0].eval(context)
		for (i in this.operand.children.size downTo 1){		
			val option = this.operand.children[i]
			if (option.type == OperandType.Case) {
				if (option.name == value) {
					return option.children[0].eval(context)
				}
			} else if (option.type == OperandType.Default) {
				return option.children[0].eval(context)
			}
		}
		throw Exception("Switch evaluator error")
	}
}

class BreakEvaluator(val operand:Operand) : IEvaluator {
	override fun eval (context: Context): Any? {
		TODO("not implemented") 
	}
}
class ContinueEvaluator(val operand:Operand) : IEvaluator {
	override fun eval (context: Context): Any? {
		TODO("not implemented") 
	}
}
class FuncEvaluator(val operand:Operand) : IEvaluator {
	override fun eval (context: Context): Any? {
		TODO("not implemented") 
	}
}
class ReturnEvaluator(val operand:Operand) : IEvaluator {
	override fun eval (context: Context): Any? {
		TODO("not implemented") 
	}
}
class TryEvaluator(val operand:Operand) : IEvaluator {
	override fun eval (context: Context): Any? {
		TODO("not implemented") 
	}
}
class CatchEvaluator(val operand:Operand) : IEvaluator {
	override fun eval (context: Context): Any? {
		TODO("not implemented") 
	}
}
class ThrowEvaluator(val operand:Operand) : IEvaluator {
	override fun eval (context: Context): Any? {
		TODO("not implemented") 
	}
}