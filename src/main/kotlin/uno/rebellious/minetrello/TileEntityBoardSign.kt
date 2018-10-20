package uno.rebellious.minetrello

import net.minecraft.tileentity.TileEntitySign
import net.minecraft.util.text.TextComponentString

class TileEntityBoardSign : TileEntitySign() {

    fun updateText(s: String) {
        signText[0] = TextComponentString(s)
    }

}