package expression.contract
import h3lp.h3lp
import java.util.*
import java.util.ArrayDeque

class Data(val data: MutableMap<String, Any?>, val parent: Data? = null) {

    fun newData(): Data {
        return Data(mutableMapOf<String, Any?>(), this)
    }

    fun getData(variable: String): MutableMap<String, Any?>? {
        if (this.data.get(variable) != null || this.parent !is Data) {
            return this.data
        } 
        val _data = this.parent.getData(variable)
        return if (_data == null) _data else this.data
    }

    fun contains(name: String): Boolean {
        val names = name.split('.')
        var value = this.getData(names[0])
        for (n in names) {
            if (value == null || !value.containsKey(n)) {
                return false
            }
            @Suppress("UNCHECKED_CAST")
            value = value.get(n) as MutableMap<String, Any?>?
        } 
        return true
    }

    fun get(name: String): Any ? {
        val names = h3lp.obj.names(name)
        val data = this.getData(names[0])
        if (data == null) {
            return null
        }
        return h3lp.obj.getValue(data, name)
    }

    fun set(name: String, value: Any ?): Boolean {
        val names = h3lp.obj.names(name)
        val data = this.getData(names[0])
        if (data == null) {
            return false
        }
        return h3lp.obj.setValue(data, name, value)
    }

    fun init(name: String, value: Any ?) {
        this.data.set(name, value)
    }
}
data class Step(
    val name: String,
    val id: String,
    val values: MutableList<Any> = mutableListOf<Any>()
)

class Token {
    val id: String
    val stack: ArrayDeque<Any>
    var isBreak: Boolean
    val listeners: MutableList<String>
    val signals: MutableList<String>

    constructor () {
        this.id = UUID.randomUUID().toString()
        this.stack = ArrayDeque<Any>()
        this.isBreak = false
        this.listeners = mutableListOf<String>()
        this.signals = mutableListOf<String>()
    }

    fun addListener(value: String) {
        this.isBreak = true
        this.listeners.add(value)
    }

    fun clearListeners() {
        this.isBreak = false
        this.listeners.clear()
    }

    fun addSignal(value: String) {
        this.signals.add(value)
    }

    fun clearSignals() {
        this.signals.clear()
    }
}

class Context {
    val data: Data
    val token: Token
    val parent: Context?

    constructor (data: Data? = null, token: Token? = null, parent: Context? = null) {
        this.data = if (data != null) data else Data(mutableMapOf<String, Any ?>())
        this.token = if (token != null) token else Token()
        this.parent = parent
    }

    fun newContext(): Context {
        return Context(this.data.newData(), this.token, this)
    }
}
