package mcmultipart.item;

import com.google.common.base.Predicate;

import mcmultipart.microblock.IMicroblock;
import mcmultipart.microblock.MicroblockContainer;
import mcmultipart.multipart.IMultipart;
import mcmultipart.multipart.IMultipartContainer;
import mcmultipart.multipart.MultipartHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class MicroContainerPlacementWrapper extends PartPlacementWrapper {

    public MicroContainerPlacementWrapper(ItemStack match) {

        super(match, null);
    }

    public MicroContainerPlacementWrapper(Predicate<ItemStack> match) {

        super(match, null);
    }

    @Override
    protected boolean place(World world, BlockPos pos, EnumFacing side, Vec3d hit, ItemStack stack, EntityPlayer player) {

        IMultipartContainer container = MultipartHelper.getOrConvertPartContainer(world, pos, true);
        if (container == null) return false;

        world.setBlockState(pos, Blocks.air.getDefaultState(), 0);
        if (!placeDefault(world, pos, side, hit, stack, player)) return false;
        if (world.isRemote) return true;

        IMultipartContainer newContainer = MultipartHelper.getPartContainer(world, pos);
        if (newContainer == null)
            throw new IllegalStateException("Attempted to replace a part container with a block that cannot contain parts!");

        for (IMultipart part : container.getParts())
            newContainer.addPart(container.getPartID(part), part);

        return true;
    }

    @Override
    protected boolean isValidPlacement(World world, BlockPos pos, EnumFacing side) {

        IMultipartContainer container = MultipartHelper.getOrConvertPartContainer(world, pos, false);
        if (container == null || container instanceof MicroblockContainer) return false;
        for (IMultipart part : container.getParts())
            if (!(part instanceof IMicroblock)) return false;
        return true;
    }

}
