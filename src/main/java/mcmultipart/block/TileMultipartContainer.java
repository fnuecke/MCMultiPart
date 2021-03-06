package mcmultipart.block;

import java.util.Collection;
import java.util.UUID;

import mcmultipart.capabilities.MultipartCapabilityHelper;
import mcmultipart.multipart.IMultipart;
import mcmultipart.multipart.IMultipartContainer;
import mcmultipart.multipart.ISlottedPart;
import mcmultipart.multipart.Multipart;
import mcmultipart.multipart.MultipartContainer;
import mcmultipart.multipart.PartSlot;
import net.minecraft.block.BlockContainer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;

/**
 * A final class that extends {@link BlockContainer} and implements {@link IMultipartContainer}. Represents a TileEntity which can contain
 * any kind of multipart.<br/>
 * You do NOT need to extend this class for your multiparts to work. I repeat, you do NOT. You need to either extend {@link Multipart} or
 * implement {@link IMultipart}. If you only need microblock support, look into {@link BlockCoverable}.
 */
public final class TileMultipartContainer extends TileEntity implements IMultipartContainer, ITickable {

    private MultipartContainer container;

    public TileMultipartContainer(MultipartContainer container) {

        this.container = new MultipartContainer(this, container.canTurnIntoBlock(), container);
    }

    public TileMultipartContainer() {

        this.container = new MultipartContainer(this, true);
    }

    @Override
    public World getWorldIn() {

        return getWorld();
    }

    @Override
    public BlockPos getPosIn() {

        return getPos();
    }

    public MultipartContainer getPartContainer() {

        return container;
    }

    @Override
    public Collection<? extends IMultipart> getParts() {

        return container.getParts();
    }

    @Override
    public ISlottedPart getPartInSlot(PartSlot slot) {

        return container.getPartInSlot(slot);
    }

    @Override
    public boolean canAddPart(IMultipart part) {

        return container.canAddPart(part);
    }

    @Override
    public boolean canReplacePart(IMultipart oldPart, IMultipart newPart) {

        return container.canReplacePart(oldPart, newPart);
    }

    @Override
    public void addPart(IMultipart part) {

        container.addPart(part);
    }

    @Override
    public void removePart(IMultipart part) {

        container.removePart(part);
    }

    @Override
    public UUID getPartID(IMultipart part) {

        return container.getPartID(part);
    }

    @Override
    public IMultipart getPartFromID(UUID id) {

        return container.getPartFromID(id);
    }

    @Override
    public void addPart(UUID id, IMultipart part) {

        container.addPart(id, part);
    }

    @Override
    public boolean occlusionTest(IMultipart part, IMultipart... ignored) {

        return container.occlusionTest(part, ignored);
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {

        if (super.hasCapability(capability, facing)) return true;
        return MultipartCapabilityHelper.hasCapability(container, capability, facing);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {

        T impl = super.getCapability(capability, facing);
        if (impl != null) return impl;
        return MultipartCapabilityHelper.getCapability(container, capability, facing);
    }

    @Override
    public boolean hasCapability(Capability<?> capability, PartSlot slot, EnumFacing facing) {

        return container.hasCapability(capability, slot, facing);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, PartSlot slot, EnumFacing facing) {

        return container.getCapability(capability, slot, facing);
    }

    @Override
    public void update() {

        if (!getWorldIn().isRemote && getParts().isEmpty()) {
            getWorldIn().setBlockToAir(getPosIn());
            return;
        }

        for (IMultipart part : getParts())
            if (part instanceof ITickable) ((ITickable) part).update();
    }

    @Override
    public void onLoad() {

        super.onLoad();
        for (IMultipart part : getParts())
            part.onLoaded();
    }

    @Override
    public void onChunkUnload() {

        super.onChunkUnload();
        for (IMultipart part : getParts())
            part.onUnloaded();
    }

    @Override
    public void writeToNBT(NBTTagCompound compound) {

        super.writeToNBT(compound);
        container.writeToNBT(compound);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {

        super.readFromNBT(compound);
        container.readFromNBT(compound);
    }

    @Override
    public SPacketUpdateTileEntity getDescriptionPacket() {

        NBTTagCompound tag = new NBTTagCompound();
        container.writeDescription(tag);
        return new SPacketUpdateTileEntity(getPosIn(), getBlockMetadata(), tag);
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {

        container.readDescription(pkt.getNbtCompound());
    }

    @Override
    public boolean canRenderBreaking() {

        return true;
    }

    @Override
    public boolean shouldRenderInPass(int pass) {

        return true;
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {

        AxisAlignedBB bounds = null;
        for (IMultipart part : getParts()) {
            AxisAlignedBB bb = part.getRenderBoundingBox();
            if (bb != null) {
                if (bounds == null) bounds = bb;
                else bounds = bounds.union(bb);
            }
        }
        if (bounds == null) bounds = new AxisAlignedBB(0, 0, 0, 1, 1, 1);
        return bounds.offset(getPosIn().getX(), getPosIn().getY(), getPosIn().getZ());
    }

}
