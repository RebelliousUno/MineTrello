package uno.rebellious.minetrello.dao

import com.jayway.jsonpath.JsonPath
import com.jayway.jsonpath.ReadContext

class Board(val json: String) {
    var  context: ReadContext = JsonPath.parse(json)

    val name: String
        get() = context.read("$.name")

    val desc: String
        get() = context.read("$.desc")
}
