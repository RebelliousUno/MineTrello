package uno.rebellious.minetrello.commands

import net.minecraft.command.ICommand
import net.minecraft.command.ICommandSender
import net.minecraft.server.MinecraftServer
import net.minecraft.tileentity.TileEntitySign
import net.minecraft.util.math.BlockPos
import org.apache.logging.log4j.Level
import uno.rebellious.minetrello.MineTrello
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
        MineTrello.logger?.log(Level.INFO, "Server isRemote: ${sender.entityWorld.isRemote}")

        //find sign within 5 blocks with name Trello
        val pos = sender.position
        val x = -3..3
        val y = -3..3
        val z = -3..3

        val posList = ArrayList<BlockPos>()

        x.forEach {xVal ->
            z.forEach {zVal ->
                y.forEach { yVal ->
                    posList.add(pos.add(xVal, yVal, zVal))
                }
            }
        }

        val trelloSign = posList
            .filter { sender.entityWorld.getTileEntity(it) is TileEntitySign }
            .map { sender.entityWorld.getTileEntity(it) as TileEntitySign }
            .onEach { MineTrello.logger?.log(Level.INFO, "!${it.signText[0].unformattedComponentText}!") }
            .firstOrNull { it.signText[0].unformattedComponentText.trim().equals("trello", true) }
        if (trelloSign != null) {
            val boardId = trelloSign.signText[1].unformattedText
            MineTrello.boardHandler?.findBoardFromSign(trelloSign.pos, sender.entityWorld)
            TrelloDAOImpl()
                .getBoardForId(boardId)
                .subscribe { board ->
                    MineTrello.logger?.log(Level.INFO, board.name)
                    MineTrello.logger?.log(Level.INFO, board.desc)
                }

//            trelloSign.signText[0] = TextComponentString("Hey")
//            trelloSign.markDirty()
//            val packet = trelloSign.updatePacket
//            server.playerList.players.forEach {
//                it.connection.sendPacket(packet)
//            }
        } else MineTrello.logger?.log(Level.INFO, "Entity Not Found")




//        TrelloDAOImpl().getBoards().subscribe { response ->
//            response.second.success {
//                val board = Board(it)
//                MineTrello.logger?.log(Level.INFO, board.name)
//            }
//
//        }
    }




}


