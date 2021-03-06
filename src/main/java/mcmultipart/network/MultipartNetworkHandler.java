package mcmultipart.network;

import mcmultipart.MCMultiPartMod;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.management.PlayerManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class MultipartNetworkHandler {

    public static final SimpleNetworkWrapper wrapper = NetworkRegistry.INSTANCE.newSimpleChannel(MCMultiPartMod.MODID);

    public static void init() {

        wrapper.registerMessage(MessageMultipartChange.class, MessageMultipartChange.class, 0, Side.CLIENT);
        wrapper.registerMessage(MessageWrappedPartPlacement.class, MessageWrappedPartPlacement.class, 1, Side.SERVER);
    }

    public static void sendToAllWatching(IMessage message, World world, BlockPos pos) {

        PlayerManager manager = ((WorldServer) world).getPlayerChunkManager();
        for (EntityPlayer player : world.playerEntities)
            if (manager.isPlayerWatchingChunk((EntityPlayerMP) player, pos.getX() >> 4, pos.getZ() >> 4))
                wrapper.sendTo(message, (EntityPlayerMP) player);
    }

    public static void sendToServer(IMessage message) {

        wrapper.sendToServer(message);
    }

}
