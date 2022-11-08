package com.flaviolionelrita.expression.operand

import com.flaviolionelrita.expression.contract.*
import com.flaviolionelrita.h3lp.h3lp

class Parser {
	private val model:IModelManager	
	private var positions: List<Pair<Char, Pair<Int,Int>>> = emptyList()
	private var buffer: List<Char>
	private var length: Int=0
	private var index: Int=0
	private val singleOperators = arrayListOf<String>()
	private val doubleOperators = arrayListOf<String>()
	private val tripleOperators= arrayListOf<String>()
	private val assignmentOperators = arrayListOf<String>()
    private val twoPoints = ":".toCharArray()[0]
	private val parenthesisOpen = "(".toCharArray()[0]
    private val parenthesisClose= ")".toCharArray()[0]
	private val keyOpen = "{".toCharArray()[0]
    private val keyClose = "}".toCharArray()[0]
	private val doubleQuote = "\"".toCharArray()[0]

	constructor (model: IModelManager, expression: String) {
		this.model = model
		this.positions = this.normalize(expression)
		this.buffer = this.positions.map { it.first }
		this.length = this.buffer.size		
		for (entry in model.operators) {
			val name = entry.first
			val metadata = entry.second
			if (name.length == 1) {
				this.singleOperators.add(name)
			} else if (name.length == 2) {
				this.doubleOperators.add(name)
				if (metadata.priority == 1) {
					this.assignmentOperators.add(name)
				}
			} else if (name.length == 3) {
				this.tripleOperators.add(name)
			}
		}
	}

    private fun normalize (expression: String): ArrayList<Pair<Char, Pair<Int,Int>>> {
		var isString = false
		var quotes:Char = ' '
		val buffer = expression.toCharArray().toTypedArray()
		val length = buffer.size
		val result = arrayListOf<Pair<Char, Pair<Int,Int>>>()
		var line = 0
		var col = 0
		var i = 0
        val doubleQuote = "\"".toCharArray()[0]
		while (i < length) {
			val p = buffer[i]
			if (isString && p == quotes) {
				isString = false
			} else if (!isString && (p == '\'' || p == doubleQuote || p == '`')) {
				isString = true
				quotes = p
			}
			if (isString) {
				result.add(Pair(p ,Pair(line, col)))
			} else if (p == ' ') {
				// Only leave spaces when it's between alphanumeric characters.
				// for example in the case of "} if" there should not be a space
				if (i + 1 < length && i - 1 >= 0 && h3lp.validator.isAlphanumeric(buffer[i - 1]) && h3lp.validator.isAlphanumeric(buffer[i + 1])) {
					result.add(Pair(p, Pair(line, col)))
				}
			// when there is a block that ends with "}" and then there is an enter , replace the enter with ";"
			// TODO: si estamos dentro de un objecto NO debería agregar ; luego de } sino rompe el obj
			// } else if (p == '\n' && result.size > 0 && result[result.size - 1] == this.keyClose) {
			// result.add(';')
			} else if (p == '\n') {
				line++
				col = 0
			} else if (p != '\r' && p != '\t') {
				result.add(Pair(p, Pair(line, col)))
			}
			i++
			col++
		}
		val index= result.size - 1
		if (result[index].first == ';') {
			result.removeAt(index)
		}
		return result
	}

	val end : Boolean get() {
	    return this.index >= this.length
	}

	val current : Char get () {
		return this.buffer[this.index]
	}

	private fun offset (offset:Int = 0): Char {
		return this.buffer[this.index + offset]
	}

	private fun pos (offset:Int = 0): Pair<Int, Int> {
		return this.positions[this.index - offset].second
	}

	private fun nextIs (key: Char): Boolean {
		return this.nextIs(key)
	}
	private fun nextIs (key: String): Boolean {
		val array = key.toCharArray().toTypedArray()
		var i = 0
		while (i < array.size) {
			val index = this.index + i
			if (this.buffer[index] != array[i]) {
				return false
			}
			i++
		}
		return true
	}

	private fun getValue (increment: Boolean = true): String {
		val buff = arrayListOf<Char>()
		if (increment) {
			while (!this.end && h3lp.validator.isAlphanumeric(this.current.toString())) {
				buff.add(this.current)
				this.index += 1
			}
		} else {
			var index = this.index
			while (!this.end && h3lp.validator.isAlphanumeric(this.buffer[index].toString())) {
				buff.add(this.buffer[index])
				index += 1
			}
		}
		return buff.joinToString()
	}

	private fun getString (char: Char): String {
		val buff = arrayListOf<Char>()
		while (!this.end) {
			if (this.current == char) {
				if (!(( (this.index + 1) < this.length && this.offset(1) == char) || (this.offset(-1) == char))) { break }
			}
			buff.add(this.current)
			this.index += 1
		}
		this.index += 1
		return buff.joinToString()
	}

	public fun parse ():Operand {
		val operands = arrayListOf<Operand>()
		while (!this.end) {
			val operand = this.getExpression(null, null, ";")
			if (operand == null) {
				break
			}
			operands.add(operand)
		}
		if (operands.size == 1){
			return operands[0] 
		} else {
			return Operand(Pair(0, 0), "block", OperandType.Block, operands)
		}
	}

	private fun getExpression (operand1: Operand?, operator: String?, _break:Char): Operand? {
		return this.getExpression(operand1,operator,_break.toString())
	}
	private fun getExpression (_operand1: Operand?, _operator: String?, _break:String): Operand? {
		var expression:Operand? = null
		var operand2:Operand? = null
		var isBreak = false
		val pos = this.pos()
		var operand1 = _operand1
		var operator = _operator
		while (!this.end) {
			if (operand1 == null && operator==null) {
				operand1 = this.getOperand()
				operator = this.getOperator()
				if (operator == null || _break.contains(this.current)) {
					if (_break.contains(this.current)) {
						this.index += 1
					}
					expression = operand1
					isBreak = true
					break
				}
			}
			operand2 = this.getOperand()
			val nextOperator = this.getOperator()
			if (operator != null && operand1 != null) {
				if (nextOperator == null || _break.contains(this.current)) {
					if (_break.contains(this.current)) {
						this.index += 1
					}
					expression = Operand(this.pos(operator.length), operator, OperandType.Operator, arrayListOf(operand1, operand2))
					isBreak = true
					break
				} else if (this.model.priority(operator, null) >= this.model.priority(nextOperator, null)) {
					operand1 = Operand(this.pos(operator.length), operator, OperandType.Operator, arrayListOf(operand1, operand2))
					operator = nextOperator
				} else {
					operand2 = this.getExpression(operand2, nextOperator, _break) as Operand
					expression = Operand(this.pos(operator.length), operator, OperandType.Operator, arrayListOf(operand1, operand2))
					isBreak = true
					break
				}
			}
		}
		if (!isBreak && operator!= null && operand1 != null && operand2 != null) {
			expression = Operand(pos, operator, OperandType.Operator, arrayListOf(operand1, operand2))
		}
		return expression as Operand
	}

	private fun getOperand (): Operand {
		var isNegative = false
		var isNot = false
		var isBitNot = false
		var operand:Operand? = null
		var char = this.current
		if (char == '-') {
			isNegative = true
			this.index += 1
			char = this.current
		} else if (char == '~') {
			isBitNot = true
			this.index += 1
			char = this.current
		} else if (char == '!') {
			isNot = true
			this.index += 1
			char = this.current
		}
		val pos = this.pos()
		if (h3lp.validator.isAlphanumeric(char)) {
			var value: String = this.getValue()
			if (value == "function" && this.current == this.parenthesisOpen) {
				this.index += 1
				operand = this.getFunctionBlock(pos)
			} else if (value == "if" && this.current == this.parenthesisOpen) {
				this.index += 1
				operand = this.getIfBlock(pos)
			} else if (value == "for" && this.current == this.parenthesisOpen) {
				this.index += 1
				operand = this.getForBlock(pos)
			} else if (value == "while" && this.current == this.parenthesisOpen) {
				this.index += 1
				operand = this.getWhileBlock(pos)
			} else if (value == "switch" && this.current == this.parenthesisOpen) {
				this.index += 1
				operand = this.getSwitchBlock(pos)
			} else if (!this.end && this.current == this.parenthesisOpen) {
				this.index += 1
				if (value.contains('.')) {
					val names = h3lp.obj.names(value)
					val functionName = names.drop(1)[0]
					val variableName = names.joinToString(separator = ".")
					val variable = Operand(pos, variableName, OperandType.Var)
					operand = this.getChildFunc(functionName, variable)
				} else {
					if (this.current == this.parenthesisClose) {
						this.index++
						operand = Operand(pos, value, OperandType.CallFunc)
					} else {
						val args = this.getArgs(this.parenthesisClose)
						operand = Operand(pos, value, OperandType.CallFunc, args)
					}
				}
			} else if (value == "try" && this.current == this.keyOpen) {
				operand = this.getTryCatchBlock(pos)
			} else if (value == "throw") {
				operand = this.getThrow(pos)
			} else if (value == "return") {
				operand = this.getReturn(pos)
			} else if (value == "break") {
				operand = Operand(pos, "break", OperandType.Break)
			} else if (value == "continue") {
				operand = Operand(pos, "continue", OperandType.Continue)
			} else if (!this.end && this.current == '[') {
				this.index += 1
				operand = this.getIndexOperand(value, pos)
			} else if (h3lp.validator.isIntegerFormat(value)) {
				if (isNegative) {
					operand = Operand(pos, value.toInt() * -1, OperandType.Const, null, Type.integer)
					isNegative = false
				} else if (isBitNot) {
					operand = Operand(pos,value.toInt().inv(), OperandType.Const, null, Type.integer)
					isBitNot = false
				} else {
					operand = Operand(pos, value.toInt(), OperandType.Const, null, Type.integer)
				}
			} else if (h3lp.validator.isDecimalFormat(value)) {
				if (isNegative) {
					operand = Operand(pos, value.toFloat() * -1, OperandType.Const, null, Type.decimal)
					isNegative = false
				} else if (isBitNot) {
					operand = Operand(pos,value.toInt().inv(), OperandType.Const, null, Type.decimal)
					isBitNot = false
				} else {
					operand = Operand(pos, value.toFloat(), OperandType.Const, null, Type.decimal)
				}
				
			} else if (this.model.isConstant(value)) {
				val constantValue = this.model.getConstantValue(value)
				operand = Operand(pos, constantValue, OperandType.Const, arrayListOf<Operand>(), Type.get(constantValue))
			} else if (this.model.isEnum(value)) {
				operand = this.getEnum(value, pos)
			} else {
				operand = Operand(pos, value, OperandType.Var)
			}
		} else if (char == '\'' || char == this.doubleQuote ) {
			this.index += 1
			val result = this.getString(char)
			operand = Operand(pos, result, OperandType.Const, null, Type.string)
		} else if (char == '`') {
			this.index += 1
			val result = this.getTemplate()
			operand = Operand(pos, result, OperandType.Template, null, Type.string)
		} else if (char == '(') {
			this.index += 1
			operand = this.getExpression(null, null, this.parenthesisClose)
		} else if (char == this.keyOpen) {
			this.index += 1
			operand = this.getObject(pos)
		} else if (char == '[') {
			this.index += 1
			val elements = this.getArgs(']')
			operand = Operand(pos, "array", OperandType.List, elements)
		} else if (char == '$') {
			var variableName: String
			if (this.offset(1) == this.keyOpen) {
				this.index += 2
				variableName = this.getValue()
				if (!this.end && this.nextIs(this.keyClose)) {
					this.index += 1
				} else {
					throw Error("Not found character } in Environment variable $variableName")
				}
			} else {
				this.index += 1
				variableName = this.getValue()
			}
			operand = Operand(pos, variableName, OperandType.Env)
		}
		if (operand == null) {
			throw Exception("Operand undefined")
		}

		operand = this.solveChain(operand, pos)
		if (isNegative) operand = Operand(Pair(pos.first, pos.second - 1), "-", OperandType.Operator, arrayListOf(operand))
		if (isNot) operand = Operand(Pair(pos.first, pos.second - 1), "!", OperandType.Operator, arrayListOf(operand))
		if (isBitNot) operand = Operand(Pair(pos.first, pos.second - 1), "~", OperandType.Operator, arrayListOf(operand))
		return operand
	}

	private fun solveChain (operand: Operand, pos:Pair<Int,Int>): Operand {
		if (this.end) {
			return operand
		}
		if (this.current == '.') {
			this.index += 1
			val name = this.getValue()
			if (this.current == '(') {
				this.index += 1
				if (name.contains('.')) {
					// .xxx.xxx(p=> p.xxx)
					val names = h3lp.obj.names(name)
					val propertyName = names.subList(0, names.size -1).joinToString(separator = ".")
					val functionName = names.last()
					val property = Operand(pos, propertyName, OperandType.Property, arrayListOf(operand))
					return this.solveChain(this.getChildFunc(functionName, property), pos)
				} else {
					// .xxx(p=> p.xxx)
					return this.solveChain(this.getChildFunc(name, operand), pos)
				}
			} else if (this.current == '[') {
				this.index += 1
				if (name.contains('.')) {
					// .xxx.xxx[x]
					val property = Operand(pos, name, OperandType.Property, arrayListOf(operand))
					val idx = this.getExpression(null, null, "]") as Operand
					return Operand(pos, "[]", OperandType.Operator, arrayListOf(property, idx))
				} else {
					// .xxx[x]
					val property = Operand(pos, name, OperandType.Property, arrayListOf(operand))
					val idx = this.getExpression(null, null, "]") as Operand
					return Operand(pos, "[]", OperandType.Operator, arrayListOf(property, idx))
				}
			} else {
				// .xxx
				return Operand(pos, name, OperandType.Property, arrayListOf(operand))
			}
		} else if (this.current == '[') {
			// xxx[x][x] or xxx[x].xxx[x]
			this.index += 1
			val idx = this.getExpression(null, null, "]") as Operand
			return Operand(pos, "[]", OperandType.Operator, arrayListOf(operand, idx))
		} else {
			return operand
		}
	}

	private fun getOperator (): String? {
		if (this.end) {
			return null
		}
		var op:String? = null
		if (this.index + 2 < this.length) {
			val triple = this.current.toString() + this.offset(1).toString() + this.offset(2).toString()
			if (this.tripleOperators.contains(triple)) {
				op = triple
			}
		}
		if (op == null && this.index + 1 < this.length) {
			val double = this.current.toString() + this.offset(1).toString()
			if (this.doubleOperators.contains(double)) {
				op = double
			}
		}
		if (op == null) {
			if (!this.model.isOperator(this.current,null)) {
				return null
			}
			op = this.current.toString()
		}
		this.index += op.length
		return op
	}	

	private fun getTemplate (): String {
		val buff = arrayListOf<Char>()
		while (!this.end) {
			if (this.current == '`') {
				break
			}
			buff.add(this.current)
			this.index += 1
		}
		this.index += 1
		return buff.joinToString()
	}

	private fun getArgs (end:Char = this.parenthesisClose): ArrayList<Operand> {
		val args = arrayListOf<Operand>()
		while (true) {
			val arg = this.getExpression(null, null, "," + end)
			if (arg != null) args.add(arg)
			if (this.offset(-1) == end) break
		}
		return args
	}

	private fun getObject (pos:Pair<Int,Int>): Operand {
		val attributes = arrayListOf<Operand>()
		while (true) {
			var name:String?
			if (this.current == this.doubleQuote || this.current == '\'') {
				val char = this.current
				this.index += 1
				name = this.getString(char)
			} else {
				name = this.getValue()
			}
			if (this.current == ':') this.index += 1
			else throw Exception("attribute " + name + " without value")
			val keyValPos = this.pos()
			val value = this.getExpression(null, null, ",}") as Operand
			val attribute = Operand(keyValPos, name, OperandType.KeyVal, arrayListOf(value))
			attributes.add(attribute)
			if (this.offset(-1) == this.keyClose) break
		}
		return Operand(pos, "obj", OperandType.Obj, attributes)
	}

	private fun getBlock (): Operand {
		val blockPos = this.pos()
		val lines = arrayListOf<Operand>()
		while (true) {
			val line = this.getExpression(null, null, ";}")
			if (line != null) {
				lines.add(line)
			}
			if (this.offset(-1) == ';' && this.current == this.keyClose) {
				this.index += 1
				break
			}
			if (this.offset(-1) == this.keyClose) {
				break
			}
		}
		return Operand(blockPos, "block", OperandType.Block, lines)
	}

	private fun getControlBlock (): Operand {
		if (this.current == this.keyOpen) {
			this.index += 1
			return this.getBlock()
		} else {
			return this.getExpression(null, null, ";") as Operand
		}
	}

	private fun getReturn (pos:Pair<Int,Int>): Operand {
		val value = this.getExpression(null, null, ';') as Operand
		return Operand(pos, "return", OperandType.Return, arrayListOf(value))
	}

	private fun getTryCatchBlock (pos:Pair<Int,Int>): Operand {
		val children = arrayListOf<Operand>()
		val tryBlock = this.getControlBlock()
		children.add(tryBlock)
		if (this.nextIs("catch")) {
			val catchChildren = arrayListOf<Operand>()
			val catchPos = this.pos("catch".length)
			this.index += "catch".length
			if (this.current == this.parenthesisOpen) {
				this.index += 1
				val variable = this.getExpression(null, null, this.parenthesisClose) as Operand
				catchChildren.add(variable)
			}
			val catchBlock = this.getControlBlock()
			catchChildren.add(catchBlock)
			val catchNode = Operand(catchPos, "catch", OperandType.Catch, catchChildren)
			children.add(catchNode)
		}
		if (this.current == ';') this.index += 1
		return Operand(pos, "try", OperandType.Try, children)
	}

	private fun getThrow (pos:Pair<Int,Int>): Operand {
		val exception = this.getExpression(null, null, ';') as Operand
		return Operand(pos, "throw", OperandType.Throw, arrayListOf(exception))
	}

	private fun getIfBlock (pos:Pair<Int,Int>): Operand {
		val children = arrayListOf<Operand>()
		val condition = this.getExpression(null, null, this.parenthesisClose) as Operand
		children.add(condition)
		val block = this.getControlBlock()
		children.add(block)

		while (this.nextIs("elseif(")) {
			val elseIfPos = this.pos()
			this.index += "elseif(".length
			val condition2 = this.getExpression(null, null, this.parenthesisClose) as Operand
			val elseIfBlock = this.getControlBlock()
			val elseIfNode = Operand(elseIfPos, "elseif", OperandType.ElseIf, arrayListOf(condition2, elseIfBlock))
			children.add(elseIfNode)
		}

		if (this.nextIs("else")) {
			this.index += "else".length
			val elseBlock = this.getControlBlock()
			children.add(elseBlock)
		}
		return Operand(pos, "if", OperandType.If, children)
	}

	private fun getSwitchBlock (pos:Pair<Int,Int>): Operand {
		val children = arrayListOf<Operand>()
		val value = this.getExpression(null, null, this.parenthesisClose) as Operand
		children.add(value)
		if (this.current == this.keyOpen) this.index += 1
		var next:String = if(this.nextIs("case")) "case" else (if(this.nextIs("default:")) "default:" else "" )
		while (next == "case") {
			this.index += "case".length
			var compare: String
			if (this.current == '\'' || this.current == this.doubleQuote) {
				val char = this.current
				this.index += 1
				compare = this.getString(char)
			} else {
				compare = this.getValue()
			}
			val caseNode = Operand(this.pos(), compare, OperandType.Case)
			val block = Operand(this.pos(), "block", OperandType.Block)
			caseNode.children.add(block)
			if (this.current == ':') this.index += 1
			while (true) {
				val line = this.getExpression(null, null, ";}")
				if (line != null) {
					block.children.add(line)
				}
				if (this.nextIs("case")) {
					next = "case"
					break
				} else if (this.nextIs("default:")) {
					next = "default:"
					break
				} else if (this.current == this.keyClose || this.offset(-1) == this.keyClose) {
					next = "end"
					break
				}
			}
			children.add(caseNode)
		}

		if (next == "default:") {
			this.index += "default:".length
			val defaultNode = Operand(this.pos(), "default", OperandType.Default)
			val block = Operand(this.pos(), "block", OperandType.Block)
			defaultNode.children.add(block)
			while (true) {
				val line = this.getExpression(null, null, ";}")
				if (line != null) block.children.add(line)
				if (this.current == this.keyClose || this.offset(-1) == this.keyClose) break
			}
			children.add(defaultNode)
		}
		if (this.current == this.keyClose) this.index += 1
		return Operand(pos, "switch", OperandType.Switch, children)
	}

	private fun getWhileBlock (pos:Pair<Int,Int>): Operand {
		val condition = this.getExpression(null, null, this.parenthesisClose) as Operand
		val block = this.getControlBlock()
		return Operand(pos, "while", OperandType.While, arrayListOf(condition, block))
	}

	private fun getForBlock (pos:Pair<Int,Int>): Operand {
		val first = this.getExpression(null, null, "; ") as Operand
		if (this.offset(-1) == ';') {
			val condition = this.getExpression(null, null, ";") as Operand
			val increment = this.getExpression(null, null, this.parenthesisClose) as Operand
			val block = this.getControlBlock()
			return Operand(pos, "for", OperandType.For, arrayListOf(first, condition, increment, block))
		} else if (this.nextIs("in")) {
			this.index += 2
			// si hay espacios luego del in debe eliminarlos
			while (this.current == ' ') {
				this.index += 1
			}
			val list = this.getExpression(null, null, this.parenthesisClose) as Operand
			val block = this.getControlBlock()
			return Operand(pos, "forIn", OperandType.ForIn, arrayListOf(first, list, block))
		}
		throw Exception("expression for error")
	}

	private fun getFunctionBlock (pos:Pair<Int,Int>): Operand {
		val name = this.getValue()
		if (this.current == this.parenthesisOpen) {
			this.index += 1
		}
		val argsPos = this.pos()
		val args = this.getArgs() 
		val block = this.getBlock()
		val argsOperand = Operand(argsPos, "args", OperandType.Args, args)
		return Operand(pos, name, OperandType.Func, arrayListOf(argsOperand, block))
	}

	private fun getChildFunc (name: String, parent: Operand): Operand {
		var isArrow = false
		val pos = this.pos()
		val variableName = this.getValue(false)
		if (variableName != "") {
			// example: p => {name:p.name}
			// example: p -> {name:p.name}
			val i = variableName.length
			if ((this.offset(i) == '=' || this.offset(i) == '-') && this.offset(i + 1) == '>') {
				isArrow = true
				this.index += (variableName.length + 2) // [VARIABLE+NAME] + [=>]
			}
		} else if (this.current == this.parenthesisOpen  && this.offset(1) == this.parenthesisClose) {
			// example: ()=> {name:name}
			// example: ()-> {name:name}
			if ((this.offset(2) == '=' || this.offset(2) == '-') && this.offset(3) == '>') {
				isArrow = true
				this.index += 4 // [()=>]
			}
		} else if ((this.current == '='  || this.current == '-' )  && this.offset(1) == '>') {
			// example: => {name:name}
			// example: -> {name:name}
			isArrow = true
			this.index += 2 // [=>]
		}
		if (isArrow) {
			val variable = Operand(pos, variableName, OperandType.Var)
			val body = this.getExpression(null, null, this.parenthesisClose) as Operand
			return Operand(pos, name, OperandType.Arrow, arrayListOf(parent, variable, body))
		} else {
			if (this.current == this.parenthesisClose) {
				this.index += 1
				// Example: xxx.xxx()
				return Operand(pos, name, OperandType.ChildFunc, arrayListOf(parent))
			}
			// Example: xxx.xxx(x)
			val args = this.getArgs(this.parenthesisClose)
			args.add(0,parent)
			return Operand(pos, name, OperandType.ChildFunc, args)
		}
	}

	private fun getIndexOperand (name: String, pos:Pair<Int,Int>): Operand {
		val idx = this.getExpression(null, null, "]") as Operand
		val operand = Operand(pos, name, OperandType.Var)
		return Operand(pos, "[]", OperandType.Operator, arrayListOf(operand, idx))
	}

	private fun getEnum (value: String, pos:Pair<Int,Int>): Operand {
		if (value.contains('.') && this.model.isEnum(value)) {
			val names = value.split('.')
			val enumName = names[0]
			val enumOption = names[1]
			val enumValue = this.model.getEnumValue(enumName, enumOption)
			return Operand(pos, enumValue, OperandType.Const, null, Type.get(value))
		} else {
			val values = this.model.getEnum(value)
			val attributes = arrayListOf<Operand>()
			for (_value in values) {
				val attribute = Operand(pos, _value.first, OperandType.KeyVal, arrayListOf(Operand(pos, _value.second, OperandType.Const, null, Type.get(_value.second))))
				attributes.add(attribute)
			}
			return Operand(pos, "obj", OperandType.Obj, attributes)
		}
	}
}
