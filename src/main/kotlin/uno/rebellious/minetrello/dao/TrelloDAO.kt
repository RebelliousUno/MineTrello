package uno.rebellious.minetrello.dao

import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.result.Result
import io.reactivex.Single


interface TrelloDAO {
    fun getBoards(): Single<Pair<Response, Result<String, FuelError>>>
    fun getBoardForId(id: String): Single<Board>
}