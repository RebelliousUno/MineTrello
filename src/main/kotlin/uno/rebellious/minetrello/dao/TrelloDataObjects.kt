package uno.rebellious.minetrello.dao

import com.google.common.collect.Streams.zip
import com.jayway.jsonpath.JsonPath
import com.jayway.jsonpath.ReadContext
import net.minidev.json.JSONArray
import org.apache.logging.log4j.Level
import scala.util.parsing.json.JSON
import uno.rebellious.minetrello.MineTrello

class Board(json: String) {
    var  context = JsonPath.parse(json)

    val name: String
        get() = context.read("$.name")

    val desc: String
        get() = context.read("$.desc")
}

class TrelloList(val json: String) {
    var context = JsonPath.parse(json)

    val lists: List<String>
        get() = (context.read("\$[*].name") as JSONArray).toArray(emptyArray<String>()).toList()

    private val _listIds: List<String>
        get() = (context.read("\$[*].id") as JSONArray).toArray(emptyArray<String>()).toList()

    val listIds: List<Pair<String, String>>
        get() {
            return _listIds zip lists
        }
}

class CardList(val json: String) {
    var context = JsonPath.parse(json)

    val cards: List<String>
        get() = (context.read("\$[*].name") as JSONArray).toArray(emptyArray<String>()).toList()

    val cardIds: List<Pair<String, String>>
        get() {
            return _cardIds zip cards
        }

    private val _cardIds: List<String>
        get() = (context.read("\$[*].id") as JSONArray).toArray(emptyArray<String>()).toList()
}