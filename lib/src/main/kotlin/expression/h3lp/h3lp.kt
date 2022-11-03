package h3lp

open class H3lp {
    val http: HttpHelper
    val validator: Validator
    val obj: ObjectHelper 
    constructor() {
        this.http = HttpHelper()
        this.validator = Validator()
        this.obj = ObjectHelper(this.http, this.validator)
    }
}

// Singleton
// https://blog.mindorks.com/how-to-create-a-singleton-class-in-kotlin
object h3lp : H3lp()
