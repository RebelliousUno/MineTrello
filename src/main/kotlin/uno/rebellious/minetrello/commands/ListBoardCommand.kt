package uno.rebellious.minetrello.commands

import com.github.kittinunf.result.failure
import com.github.kittinunf.result.success
import net.minecraft.block.BlockSign
import net.minecraft.command.ICommand
import net.minecraft.command.ICommandSender
import net.minecraft.server.MinecraftServer
import net.minecraft.tileentity.TileEntitySign
import net.minecraft.util.math.BlockPos
import org.apache.logging.log4j.Level
import uno.rebellious.minetrello.MineTrello
import uno.rebellious.minetrello.dao.Board
import uno.rebellious.minetrello.dao.TrelloDAOImpl

class ListBoardCommand: ICommand {
    private val _aliases = emptyList<String>().toMutableList()

    init {
        _aliases.add("boards")
    }

    override fun getAliases(): MutableList<String> {
        return this._aliases
    }

    override fun getUsage(sender: ICommandSender): String {
        return "boards"
    }

    override fun getName(): String {
        return "boards"
    }

    override fun getTabCompletions(
        server: MinecraftServer,
        sender: ICommandSender,
        args: Array<String>,
        targetPos: BlockPos?
    ): List<String> {
        return emptyList()
    }

    override fun compareTo(other: ICommand): Int {
        return other.name.compareTo(name)
    }

    override fun checkPermission(server: MinecraftServer, sender: ICommandSender): Boolean {
        return true
    }

    override fun isUsernameIndex(args: Array<String>, index: Int): Boolean {
        return false
    }

    override fun execute(server: MinecraftServer, sender: ICommandSender, args: Array<String>) {
        MineTrello.logger?.log(Level.INFO, "${sender.displayName}")

        //find sign within 5 blocks with name Trello
        val pos = sender.position
        val x = -3..3
        val y = -3..3
        val z = -3..3

        x.forEach {xVal ->
            z.forEach {zVal ->
                y.forEach { yVal ->
                    val newPos =pos.add(xVal, yVal, zVal)
                    val block = sender.entityWorld.getBlockState(newPos).block
                    if (block is BlockSign) {
                        val signTile = sender.entityWorld.getTileEntity(newPos)
                        if (signTile is TileEntitySign) {
                            MineTrello.logger?.log(Level.INFO, signTile.signText[0].formattedText)
                        }
                    }
                }
            }
        }
        TrelloDAOImpl().getBoards().subscribe { response ->
            response.second.success {
                val board = Board(it)
                MineTrello.logger?.log(Level.INFO, board.name)
            }

        }
    }


}
