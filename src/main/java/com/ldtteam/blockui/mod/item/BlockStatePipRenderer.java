package com.ldtteam.blockui.mod.item;

import com.ldtteam.blockui.BOGuiGraphics;
import com.ldtteam.blockui.UiRenderMacros;
import com.ldtteam.blockui.controls.ItemIcon;
import com.ldtteam.blockui.mod.BlockUI;
import com.ldtteam.blockui.mod.item.BlockStatePipRenderer.BlockStateRenderState;
import com.ldtteam.blockui.util.SingleBlockGetter.SingleBlockNeighborhood;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.FluidRenderer;
import net.minecraft.client.renderer.block.FluidStateModelSet;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.cuboid.ItemTransform;
import net.minecraft.core.BlockPos;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.fluid.CustomFluidRenderer;
import net.neoforged.neoforge.client.model.pipeline.VertexConsumerWrapper;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import java.util.function.Predicate;

// TODO: port 21.6 this is super extra overkill perf-wise - lags when rendering more than 100 instances
// but logically is most correct version (given sanity)
// ideally we would just grab the model geometry and render it as normal textures
// and not this texture PiP non-sense that costs way to much for simple block render
public class BlockStatePipRenderer extends PictureInPictureRenderer<BlockStateRenderState>
{
    // TODO: Static instance should be fine since gui rendering is on single thread
    private static final BlockDisplayContext BLOCK_DISPLAY_CONTEXT = BlockDisplayContext.create();
    private static final SingleBlockNeighborhood NEIGHBORHOOD = new SingleBlockNeighborhood();
    private @Nullable BlockStateRenderingData lastData = null;

    @Override
    public Class<BlockStateRenderState> getRenderStateClass()
    {
        return BlockStateRenderState.class;
    }

    @Override
    protected void renderToTexture(final BlockStateRenderState renderState,
        final PoseStack poseStack,
        final SubmitNodeCollector submitNodeCollector)
    {
        final BlockStateRenderingData data = renderState.data;
        lastData = data;

        // prepare pose just like itemStack rendering would do
        // INLINE: notes for poseStack comes roughly from OversizedItemRenderer vanilla PiP

        poseStack.pushPose();
        poseStack
            .scale(BlockStateRenderState.RENDER_SIZE_F, -BlockStateRenderState.RENDER_SIZE_F, -BlockStateRenderState.RENDER_SIZE_F);

        // INLINE: itemModel()#submit(...) tranformations, we assume first layer makes sense..
        renderState.itemModel().firstLayer().itemTransform.apply(false, poseStack.last());
        poseStack.last().mulPose(renderState.itemModel().firstLayer().localTransform);

        // render block and BE
        Minecraft.getInstance().gameRenderer.lighting().setupFor(Lighting.Entry.ITEMS_FLAT);
        final int light = LightCoordsUtil.pack(15, 15);
        renderState.blockModel.submit(poseStack, submitNodeCollector, light, OverlayTexture.NO_OVERLAY, 0);

        if (renderState.blockEntityModel() != null)
        {
            try
            {
                final var state = renderState.blockEntityModel();
                final var renderer = Minecraft.getInstance().getBlockEntityRenderDispatcher().getRenderer(state);
                renderer.submit(state, poseStack, submitNodeCollector, null);
            }
            catch (final Exception e)
            {
                // well, noop then
            }
        }

        // render fluid

        final FluidState fluidState = data.blockState().getFluidState();
        if (!fluidState.isEmpty())
        {
            final FluidStateModelSet fluidModelSet = Minecraft.getInstance().getModelManager().getFluidStateModelSet();
            final FluidRenderer fluidRenderer = new FluidRenderer(fluidModelSet);

            final var customRenderer = fluidModelSet.get(fluidState).customRenderer();

            // losely based on block rendering, cuz ChunkSectionLayer stupid
            // solid + cutout pass
            submitNodeCollector
                .submitCustomGeometry(poseStack,
                    Sheets.cutoutBlockItemSheet(),
                    (pose, buffer) -> renderFluid(data,
                        fluidState,
                        fluidRenderer,
                        customRenderer,
                        layer -> layer != ChunkSectionLayer.TRANSLUCENT,
                        buffer,
                        pose));
            // translucent pass
            submitNodeCollector
                .submitCustomGeometry(poseStack,
                    Sheets.translucentBlockItemSheet(),
                    (pose, buffer) -> renderFluid(data,
                        fluidState,
                        fluidRenderer,
                        customRenderer,
                        layer -> layer == ChunkSectionLayer.TRANSLUCENT,
                        buffer,
                        pose));
        }

        poseStack.popPose();
    }

    private void renderFluid(final BlockStateRenderingData data,
        final FluidState fluidState,
        final FluidRenderer fluidRenderer,
        final @Nullable CustomFluidRenderer customRenderer,
        final Predicate<ChunkSectionLayer> targetLayer,
        final VertexConsumer buffer,
        final PoseStack.Pose pose)
    {
        final FluidRenderer.Output output =
            layer -> targetLayer.test(layer) ? new PoseTransformingVertexConsumer(buffer, pose) : NoopVertexConsumer.INSTANCE;
        NEIGHBORHOOD.blockState = data.blockState();
        NEIGHBORHOOD.blockEntity = data.blockEntity();
        if (customRenderer == null ||
            !customRenderer.renderFluid(fluidRenderer, fluidState, NEIGHBORHOOD, BlockPos.ZERO, output, data.blockState()))
        {
            fluidRenderer.tesselate(NEIGHBORHOOD, BlockPos.ZERO, output, data.blockState(), fluidState);
        }
        NEIGHBORHOOD.blockState = null;
        NEIGHBORHOOD.blockEntity = null;
    }

    @Override
    protected String getTextureLabel()
    {
        return BlockUI.resLoc("blockstate").toString();
    }

    @Override
    protected float getTranslateY(final int height, final int guiScale)
    {
        return height / 2;
    }

    @Override
    protected boolean textureIsReadyToBlit(final BlockStateRenderState renderState)
    {
        return renderState.data == lastData;
    }

    public record BlockStateRenderState(Matrix3x2f pose,
        int x0,
        int x1,
        int y0,
        int y1,
        float scale,
        BlockModelRenderState blockModel,
        ItemStackRenderState itemModel,
        @Nullable BlockEntityRenderState blockEntityModel,
        BlockStateRenderingData data,
        @Nullable ScreenRectangle bounds,
        @Nullable ScreenRectangle scissorArea) implements PictureInPictureRenderState
    {
        public static final int SCALE_FACTOR = 4;
        public static final int RENDER_SIZE_I = ItemIcon.DEFAULT_ITEMSTACK_SIZE_I * SCALE_FACTOR;
        public static final float RENDER_SIZE_F = (float) RENDER_SIZE_I;

        public static void submit(final BOGuiGraphics target, final BlockStateRenderingData data, final ItemStack itemStack)
        {
            final int x = 0, y = 0;
            final int w = RENDER_SIZE_I, h = RENDER_SIZE_I;
            UiRenderMacros.innerSubmit(target, x, y, w, h, (pose, bounds, scissors) -> {
                final BlockModelRenderState blockModel = new BlockModelRenderState();
                target.minecraft.getBlockModelResolver().update(blockModel, data.blockState(), BLOCK_DISPLAY_CONTEXT);

                final ItemStackRenderState itemModel = new ItemStackRenderState();
                // target.minecraft.getItemModelResolver()
                // .updateForLiving(itemModel, itemStack, ItemDisplayContext.GUI, Minecraft.getInstance().player);

                if (itemModel.firstLayer().itemTransform.equals(ItemTransform.NO_TRANSFORM) ||
                    data.blockState().getRenderShape() == RenderShape.INVISIBLE ||
                    true)
                {
                    // well, some items are bit dumb
                    // TODO: port 26.1
                    // we now force hard default cuboid tranformation for everything
                    // which works a bit better than before
                    // but block/cross models are still terrible
                    // solution would be to find out which things need localTransform applied
                    // then remove the itemRenderState as whole since we are only using it for transformation now
                    target.minecraft.getItemModelResolver()
                        .updateForLiving(itemModel,
                            new ItemStack(Blocks.STONE),
                            ItemDisplayContext.GUI,
                            Minecraft.getInstance().player);
                }

                BlockEntityRenderState blockEntityModel = null;
                if (data.blockEntity() != null)
                {
                    final var renderer = target.minecraft.getBlockEntityRenderDispatcher().getRenderer(data.blockEntity());
                    if (renderer != null)
                    {
                        blockEntityModel = target.getFakeLevel()
                            .useFakeLevelContext(data.blockState(), data.blockEntity(), target.minecraft.level, fakeLevel -> {
                                final BlockEntityRenderState state = renderer.createRenderState();
                                renderer.extractRenderState(data
                                    .blockEntity(), state, 0, Vec3.atCenterOf(data.blockEntity().getBlockPos()), null);
                                return state;
                            });
                    }
                }
                target.submitPictureInPictureRenderState(new BlockStateRenderState(pose,
                    x,
                    x + w,
                    y,
                    y + h,
                    1,
                    blockModel,
                    itemModel,
                    blockEntityModel,
                    data,
                    bounds,
                    scissors));
            });
        }
    }

    private static final class NoopVertexConsumer implements VertexConsumer
    {
        private static final NoopVertexConsumer INSTANCE = new NoopVertexConsumer();

        @Override
        public VertexConsumer addVertex(final float x, final float y, final float z)
        {
            return this;
        }

        @Override
        public VertexConsumer setColor(final int r, final int g, final int b, final int a)
        {
            return this;
        }

        @Override
        public VertexConsumer setColor(final int color)
        {
            return this;
        }

        @Override
        public VertexConsumer setUv(final float u, final float v)
        {
            return this;
        }

        @Override
        public VertexConsumer setUv1(final int u, final int v)
        {
            return this;
        }

        @Override
        public VertexConsumer setUv2(final int u, final int v)
        {
            return this;
        }

        @Override
        public VertexConsumer setNormal(final float x, final float y, final float z)
        {
            return this;
        }

        @Override
        public VertexConsumer setLineWidth(final float width)
        {
            return this;
        }
    }

    private static final class PoseTransformingVertexConsumer extends VertexConsumerWrapper
    {
        private final Pose pose;

        public PoseTransformingVertexConsumer(final VertexConsumer parent, final PoseStack.Pose pose)
        {
            super(parent);
            this.pose = pose;
        }

        @Override
        public VertexConsumer addVertex(final float x, final float y, final float z)
        {
            final var vec = new Vector4f(x, y, z, 1);
            pose.pose().transform(vec);
            vec.div(vec.w);
            return super.addVertex(vec.x(), vec.y(), vec.z());
        }

        @Override
        public VertexConsumer setNormal(final float x, final float y, final float z)
        {
            final var vec = new Vector3f(x, y, z);
            pose.transformNormal(x, y, z, vec);
            vec.normalize();
            return super.setNormal(vec.x(), vec.y(), vec.z());
        }
    }
}
