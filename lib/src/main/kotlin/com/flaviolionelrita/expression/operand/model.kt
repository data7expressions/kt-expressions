package com.flaviolionelrita.expression.operand

import com.flaviolionelrita.expression.contract.*
import com.flaviolionelrita.h3lp.h3lp
import java.lang.reflect.Method
import kotlin.reflect.KFunction

class ModelManager : IModelManager {
   
    private val _enums: ArrayList<Pair<String, List<Pair<String, Any>>>> = arrayListOf<Pair<String, List<Pair<String, Any>>>>()
    private val _formats: ArrayList<Pair<String, Format>> = arrayListOf<Pair<String, Format>>()
    private val _constants: ArrayList<Pair<String, Any>> = arrayListOf<Pair<String, Any>>()
    private val _operators: ArrayList<Pair<String, OperatorMetadata>> = arrayListOf<Pair<String, OperatorMetadata>>()
    private val _functions: ArrayList<Pair<String, OperatorMetadata>> = arrayListOf<Pair<String, OperatorMetadata>>()

    override val enums: List<Pair<String, List<Pair<String, Any>>>> get() {
        return this._enums
    }
    override val formats: List<Pair<String, Format>> get() {
        return this._formats
    }
    override val constants: List<Pair<String, Any>> get() {
        return this._constants
    }
    override val operators: List<Pair<String, OperatorMetadata>> get() {
        return this._operators
    }
    override val functions: List<Pair<String, OperatorMetadata>> get() {
        return this._functions
    }    
    override fun addEnum(name: String, values: List<Pair<String, Any>>) { 
        this._enums.add(Pair(name, values))
    }
    override fun addEnum(name: String, values: Any) { 
        TODO("Se debe resolver el poder agregar un Enum")
    }
    override fun addConstant(key: String, value: Any) { 
        this._constants.add(Pair(key, value))
    }
    override fun addFormat(key: String, pattern: String) { 
        val format = Format(key,pattern, Regex(pattern))
        this._formats.add(Pair(key, format))
    }
    override fun addOperatorAlias(alias: String, reference: String) { 
        val operator:Pair<String, OperatorMetadata>? = this._operators.find { it.first == alias}
        if(operator != null) {
            this._operators.add(Pair(alias, operator.second))
        }        
    }
    override fun addFunctionAlias(alias: String, reference: String) { 
        val _function:Pair<String, OperatorMetadata>? = this._functions.find { it.first == alias}
        if(_function != null) {
            this._functions.add(Pair(alias, _function.second))
        } 
    }   
    override fun addOperator(sing: String, source: KFunction<*>, info: OperatorAdditionalInfo) { 
        val singInfo = this.getSing(sing)
        val metadata=OperatorMetadata(singInfo.params,false, singInfo.params.size,singInfo.returnType,
        info.doc ,info.priority,source,null)
		this._operators.add(Pair(singInfo.name,metadata))
    }
    override fun addOperator(sing: String, source: IPrototypeEvaluator, info: OperatorAdditionalInfo) { 
        val singInfo = this.getSing(sing)
        val metadata=OperatorMetadata(singInfo.params,false, singInfo.params.size,singInfo.returnType,
        info.doc ,info.priority,null,source)
		this._operators.add(Pair(singInfo.name,metadata))
    }    
    override fun addFunction(sing: String, source: KFunction<*>, info: FunctionAdditionalInfo?) { 
        val singInfo = this.getSing(sing)
        val metadata=OperatorMetadata(singInfo.params,info?.deterministic?:true, singInfo.params.size,singInfo.returnType,
        info?.doc ,-1,source,null)
		this._functions.add(Pair(singInfo.name,metadata))
    }
    override fun addFunction(sing: String, source: IPrototypeEvaluator, info: FunctionAdditionalInfo?) { 
        val singInfo = this.getSing(sing)
        val metadata=OperatorMetadata(singInfo.params,info?.deterministic?:true, singInfo.params.size,singInfo.returnType,
        info?.doc ,-1,null,source)
		this._functions.add(Pair(singInfo.name,metadata))
    }     
    override fun getConstantValue(name: String): Any { 
        val _constant = this._constants.find { it.first == name}
        if (_constant != null) {
            return _constant.second
        }
        throw Exception("enum: " + name + " not found")
    }
    override fun getEnum(name: String): List<Pair<String, Any>> { 
        val _emun = this._enums.find { it.first == name}
        if (_emun != null) {
            return _emun.second
        }
        throw Exception("enum: " + name + " not found")
    }
    override fun getEnumValue(name: String, option: String): Any { 
        val _emun = this.getEnum(name)
        val _option = _emun.find { it.first == option }
        if (_option != null) {
            return _option.second
        }
        throw Exception("enum option: " + name + "." + option + "  not found")
    }
    override fun getFormat(name: String): Format { 
        val _format = this._formats.find { it.first == name}
        if (_format != null) {
            return _format.second
        }
        throw Exception("format: " + name + " not found")
    }

    override fun getOperator(name: String, operands: Int?): OperatorMetadata { 
        val _operators = this._operators.filter { it.first == name}
        if (_operators.size == 0) {
            throw Exception("operator: " + name + " not found")
        }
        if (operands != null) {
            val _operator = _operators.find { it.second.operands == operands}
            if (_operator != null) {
                return _operator.second
            }
            throw Exception("operator: " + name + " whit " + operands + " operands not found")
        } else if (_operators.size == 1) {
            return _operators[0].second
        } else {
            val _operator = _operators.find { it.second.operands == 2}
            if (_operator != null) {
                return _operator.second
            }
        }
        throw Exception("it is necessary to determine the number of operands for the operator " + name)        
    }
    override fun getFunction(name: String): OperatorMetadata { 
        val _function = this._functions.find { it.first == name}
        if (_function != null) {
            return _function.second
        }
        throw Exception("function: " + name + " not found")
    }
    override fun priority(name: Char, cardinality: Int?): Int { 
        return this.priority(name.toString(),cardinality)
    }
    override fun priority(name: String?, cardinality: Int?): Int { 
        if(name == null) {
            return -1
        }
        val metadata = this.getOperator(name, cardinality)
        if (metadata.priority != null) {
            return metadata.priority 
        }
		return -1
    }
    override fun isEnum(name: String): Boolean { 
        val names = name.split(".")
        return this._enums.find { it.first == names[0] } != null
    }
    override fun isConstant(name: String): Boolean { 
        return this._constants.find { it.first == name } != null
    }
    override fun isOperator(name: Char, operands: Int?): Boolean {
        return this.isOperator(name.toString(), operands)
    }
    override fun isOperator(name: String, operands: Int?): Boolean { 
        if (operands != null) {
            return this._operators.find { it.first == name && it.second.operands == operands} != null
        } 
        return this._operators.find { it.first == name} != null
    }
    override fun isFunction(name: String): Boolean { 
        return this._functions.find { it.first == name} != null
    }
    private fun getSing (sing:String): Sing {
		val buffer = sing.toCharArray().toTypedArray()
		val length = buffer.size
		var index = 0
		var functionName:String     
		var prefix = ""
		var _return = ""
		var chars: ArrayList<String> = arrayListOf<String>()

		// Clear begin spaces
		while (buffer[index] == ' ') {
			index++
		}
        while (buffer[index] != '(') {
            if (buffer[index] == ' ' && buffer[index + 1] != ' ' && buffer[index + 1] != '(') {
				prefix = chars.joinToString()
				chars.clear()
			} else if (buffer[index] != ' ') {
				chars.add(buffer[index].toString())
			}
            index++
        }
		functionName = chars.joinToString()

		chars.clear()
		val params:ArrayList<Parameter> = arrayListOf<Parameter>()
		var name = ""
		var type = ""
		var _default = ""
		var hadDefault = false
		var multiple = false
        val closeParentheses:Char = ")".toCharArray()[0]
        val twoPoints = ":".toCharArray()[0]

        index++
        while (index < length) {
            if (buffer[index] == ',' || buffer[index] == closeParentheses) {
				if (hadDefault) {
					_default = chars.joinToString()
					if (type == "") {
						type = this.getTypeFromValue(_default)
					}
				} else {
					type = chars.joinToString()
				}
				if (name.startsWith("...")) {
					multiple = true
					name = name.replace("...", "")
				}
				// Add Param
				params.add(Parameter(name,if(type != "") type else "any", if (_default != "") _default else null, multiple))
				if (buffer[index] == closeParentheses) {
					break
				}
				chars.clear()
				name = ""
				type = ""
				_default = ""
				hadDefault = false
				multiple = false
			} else if (buffer[index] == twoPoints) {
				name = chars.joinToString()
				chars.clear()
			} else if (buffer[index] == '=') {
				hadDefault = true
				if (name == "") {
					name = chars.joinToString()
				} else {
					type = chars.joinToString()
				}
				chars.clear()
			} else if (buffer[index] != ' ') {
				chars.add(buffer[index].toString())
			}
            index++
        }
		chars.clear()
		var hadReturn = false
        index++
        while (index < length) {
            if (buffer[index] == twoPoints) {
				hadReturn = true
			} else if (buffer[index] != ' ') {
				chars.add(buffer[index].toString())
			}
            index++
        }
		if (hadReturn) {
			_return = chars.joinToString()
		}
		return Sing(functionName,params,if(_return != "") _return else "void", prefix == "async")
	}

    private fun getTypeFromValue (type:String) :String {
		// COMPELTAR
		return type
	}
}
