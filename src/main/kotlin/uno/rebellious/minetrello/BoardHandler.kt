package uno.rebellious.minetrello

import io.reactivex.Observable
import net.minecraft.init.Blocks
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import org.apache.logging.log4j.Level
import java.util.concurrent.TimeUnit

class BoardHandler {
    val ticker = Observable.interval(1, TimeUnit.MINUTES)
    var boards = ArrayList<TrelloBoard>()

    init {
        ticker.subscribe {
            updateBoards()
        }
    }

    fun updateBoards() {
        MineTrello.logger?.log(Level.INFO, "Update Boards")
    }

    fun isValidBoard(board: List<BlockPos?>, world: World): Boolean {
        val notNullPos = board.filterNotNull()
        return (notNullPos.size == 4) && isValidBoard(notNullPos[0], notNullPos[1], notNullPos[2], notNullPos[3], world)
    }

    private fun isValidBoard(
        position1: BlockPos,
        position2: BlockPos,
        position3: BlockPos,
        position4: BlockPos,
        world: World
    ): Boolean {
        val minX = listOf(position1.x, position2.x, position3.x, position4.x).min()!!
        val maxX= listOf(position1.x, position2.x, position3.x, position4.x).max()!!
        val minY= listOf(position1.y, position2.y, position3.y, position4.y).min()!!
        val maxY= listOf(position1.y, position2.y, position3.y, position4.y).max()!!
        val minZ= listOf(position1.z, position2.z, position3.z, position4.z).min()!!
        val maxZ= listOf(position1.z, position2.z, position3.z, position4.z).max()!!
        val validBlocks = listOf(Blocks.STONE, Blocks.REDSTONE_BLOCK)
        (minX..maxX).forEach {x ->
            (minY..maxY).forEach {y->
                (minZ..maxZ).forEach {z ->
                    val blockPos = BlockPos(x,y,z)
                    if (!validBlocks.contains(world.getBlockState(blockPos).block)) {
                        return false
                    }
                }
            }
        }
        return true
    }

    fun isValidBoard(block: BlockPos, world: World, facing: EnumFacing): Pair<Boolean, List<BlockPos?>> {
        val firstCol = isValidBoardCol(block, world)
        if (firstCol.first) {
            val firstRow = isValidBoardBottomRow(block, world, facing)
            val endOfRowBlock = firstRow.second
            if (firstRow.first && endOfRowBlock != null) {
                val lastCol = isValidBoardCol(endOfRowBlock, world)
                if (lastCol.first) {
                    return Pair(true, listOf(block, firstCol.second, firstRow.second, lastCol.second))
                }
            }
        }
        return Pair(false, emptyList())
    }

    fun isValidBoardBottomRow(block: BlockPos, world: World, facing: EnumFacing): Pair<Boolean, BlockPos?> {
        val blockToTheSide = block.offset(facing)
        val theBlock = world.getBlockState(blockToTheSide).block
        return if (theBlock == Blocks.REDSTONE_BLOCK) {
            Pair(true, blockToTheSide)
        } else {
            if (theBlock == Blocks.STONE)
                isValidBoardBottomRow(blockToTheSide, world, facing)
            else
                Pair(false, null)
        }
    }

    fun isValidBoardCol(block: BlockPos, world: World): Pair<Boolean, BlockPos?> {
        //Go up until meet Redstone, stone, Air
        val blockAbove = block.up()
        val theBlock = world.getBlockState(blockAbove).block
        return if (theBlock == Blocks.REDSTONE_BLOCK) {
            Pair(true, blockAbove)
        } else {
            if (theBlock == Blocks.STONE)
                isValidBoardCol(blockAbove, world)
            else
                Pair(false, null)
        }
    }

    fun findBoardFromSign(signPos: BlockPos, world: World) {
        //Check if Sign already exists
        val sign = boards.firstOrNull { it.signPos.equals(signPos) }
        if (sign == null) {
            // Sign Not currently existing as a board

            var signNeighbours = EnumFacing.HORIZONTALS
            signNeighbours
                .filter {
                    world.getBlockState(signPos.offset(it)).block.equals(Blocks.REDSTONE_BLOCK) //Is the block in that facing a redstone block
                }
                .map { Pair(it, isValidBoard(signPos.offset(it), world, it)) }
                .forEach {
                    //Corners of the board
                    if (isValidBoard(it.second.second, world))
                        MineTrello.logger?.log(Level.INFO, it.second)
                    else
                        MineTrello.logger?.log(Level.INFO, "Not a Valid Board :(")
                }
        } else {
            //Sign already exists as a board
            //Verify it is still a valid board
        }
        //Check around sign for Redstone block

    }
}

class TrelloBoard(val signPos: BlockPos, val trelloBoard: Pair<BlockPos, BlockPos>, val boardId: String) {



    fun updateTrelloBoard() {
        //Get the trello data
        //Update signs on the board

    }



}
