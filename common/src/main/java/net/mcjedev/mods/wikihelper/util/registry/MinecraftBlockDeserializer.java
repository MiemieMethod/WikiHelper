package net.mcjedev.mods.wikihelper.util.registry;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.loot.LootTable;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;
import java.util.function.ToIntFunction;

public class MinecraftBlockDeserializer implements IEntryDeserializer {
    @Override
    public void deserialize(Object entryValue, JsonObject entryObj) {
        var block = (Block) entryValue;

        entryObj.addProperty("description_id", block.getDescriptionId());
        entryObj.addProperty("item", block.asItem().toString());
        entryObj.addProperty("default_block_state", getStateString(block.getStateDefinition(), block.defaultBlockState().toString()));

        var stateDefObj = new JsonObject();
        block.getStateDefinition().getProperties().forEach(p -> {
            handleProperty(block, p, stateDefObj);
        });
        entryObj.add("state_definition", stateDefObj);

        var propertiesObj = new JsonObject();
        var properties = block.properties();
        var defaultMapColor = block.defaultBlockState().getMapColor(EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
        propertiesObj.addProperty("has_collision", (Boolean) getPropertiesPrivateField("hasCollision", properties));
        var defaultSoundType = block.defaultBlockState().getSoundType();
        var defaultLightEmission = block.defaultBlockState().getLightEmission();
        propertiesObj.addProperty("explosion_resistance", block.getExplosionResistance());
        propertiesObj.addProperty("destroy_time", (Float) getPropertiesPrivateField("destroyTime", properties));
        propertiesObj.addProperty("requires_correct_tool_for_drops", (Boolean) getPropertiesPrivateField("requiresCorrectToolForDrops", properties));
        var defaultIsRandomlyTicking = block.defaultBlockState().isRandomlyTicking();
        propertiesObj.addProperty("friction", block.getFriction());
        propertiesObj.addProperty("speed_factor", block.getSpeedFactor());
        propertiesObj.addProperty("jump_factor", block.getJumpFactor());
        propertiesObj.addProperty("drops", block.getLootTable().location().toString());
        propertiesObj.addProperty("can_occlude", (Boolean) getPropertiesPrivateField("canOcclude", properties));
        propertiesObj.addProperty("is_air", (Boolean) getPropertiesPrivateField("isAir", properties));
        propertiesObj.addProperty("ignited_by_lava", (Boolean) getPropertiesPrivateField("ignitedByLava", properties));
        propertiesObj.addProperty("liquid", (Boolean) getPropertiesPrivateField("liquid", properties));
        propertiesObj.addProperty("force_solid_off", (Boolean) getPropertiesPrivateField("forceSolidOff", properties));
        propertiesObj.addProperty("force_solid_on", (Boolean) getPropertiesPrivateField("forceSolidOn", properties));
        propertiesObj.addProperty("push_reaction", ((PushReaction) getPropertiesPrivateField("pushReaction", properties)).name().toLowerCase());
        propertiesObj.addProperty("spawn_terrain_particles", (Boolean) getPropertiesPrivateField("spawnTerrainParticles", properties));
        propertiesObj.addProperty("instrument", ((NoteBlockInstrument) getPropertiesPrivateField("instrument", properties)).getSerializedName());
        propertiesObj.addProperty("replaceable", (Boolean) getPropertiesPrivateField("replaceable", properties));
        // todo: isValidSpawn
        var isRedstoneConductor = (BlockBehaviour.StatePredicate) getPropertiesPrivateField("isRedstoneConductor", properties);
        var defaultIsRedstoneConductor = isRedstoneConductor.test(block.defaultBlockState(), EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
        var isSuffocating = (BlockBehaviour.StatePredicate) getPropertiesPrivateField("isSuffocating", properties);
        var defaultIsSuffocating = isSuffocating.test(block.defaultBlockState(), EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
        var isViewBlocking = (BlockBehaviour.StatePredicate) getPropertiesPrivateField("isViewBlocking", properties);
        var defaultIsViewBlocking = isViewBlocking.test(block.defaultBlockState(), EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
        var hasPostProcess = (BlockBehaviour.StatePredicate) getPropertiesPrivateField("hasPostProcess", properties);
        var defaultHasPostProcess = hasPostProcess.test(block.defaultBlockState(), EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
        var emissiveRendering = (BlockBehaviour.StatePredicate) getPropertiesPrivateField("emissiveRendering", properties);
        var defaultEmissiveRendering = emissiveRendering.test(block.defaultBlockState(), EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
        propertiesObj.addProperty("dynamic_shape", block.hasDynamicShape());
        // todo: offsetFunction
        entryObj.add("properties", propertiesObj);

        // todo: useShapeForLightOcclusion

        var defaultSolidRender = block.defaultBlockState().isSolidRender(EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
        var defaultPropagatesSkylightDown = block.defaultBlockState().propagatesSkylightDown(EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
        var defaultLegacySolid = block.defaultBlockState().isSolid();
        var defaultLightBlock = block.defaultBlockState().getLightBlock(EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
        // todo: occlusionShapes
        var defaultCollisionShape = block.defaultBlockState().getCollisionShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
        var defaultLargeCollisionShape = block.defaultBlockState().hasLargeCollisionShape();
        // todo: faceSturdy
        var defaultIsCollisionShapeFullBlock = block.defaultBlockState().isCollisionShapeFullBlock(EmptyBlockGetter.INSTANCE, BlockPos.ZERO);

        var defaultIsSignalSource = block.defaultBlockState().isSignalSource();
        var defaultSignal = block.defaultBlockState().getSignal(EmptyBlockGetter.INSTANCE, BlockPos.ZERO, Direction.UP);
        var defaultRenderShape = block.defaultBlockState().getRenderShape();
        var defaultFluidState = block.defaultBlockState().getFluidState();

        var burnOdds = (Object2IntMap<Block>) getFireBlockPrivateField("burnOdds", (FireBlock) Blocks.FIRE);
        var defaultBurnOdds = burnOdds.getInt(block.defaultBlockState());
        var igniteOdds = (Object2IntMap<Block>) getFireBlockPrivateField("igniteOdds", (FireBlock) Blocks.FIRE);
        var defaultIgniteOdds = igniteOdds.getInt(block.defaultBlockState());

        var statesObj = new JsonObject();
        block.getStateDefinition().getPossibleStates().forEach(state -> {
            var stateObj = new JsonObject();
            var stateString = getStateString(block.getStateDefinition(), state.toString());
            var isDefault = state == block.defaultBlockState();

            var propertiesObjByState = new JsonObject();

            var color = state.getMapColor(EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
            if (isDefault || color != defaultMapColor) {
                propertiesObjByState.addProperty("map_color", color.col);
            }

            var soundType = state.getSoundType();
            if (isDefault || soundType != defaultSoundType) {
                propertiesObjByState.add("sound_type", getSoundObj(soundType));
            }

            var light = state.getLightEmission();
            if (isDefault || light != defaultLightEmission) {
                propertiesObjByState.addProperty("light_emission", light);
            }

            var randomlyTicking = state.isRandomlyTicking();
            if (isDefault || randomlyTicking != defaultIsRandomlyTicking) {
                propertiesObjByState.addProperty("is_randomly_ticking", randomlyTicking);
            }

            var redstoneConductor = isRedstoneConductor.test(state, EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
            if (isDefault || redstoneConductor != defaultIsRedstoneConductor) {
                propertiesObjByState.addProperty("is_redstone_conductor", redstoneConductor);
            }

            var suffocating = isSuffocating.test(state, EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
            if (isDefault || suffocating != defaultIsSuffocating) {
                propertiesObjByState.addProperty("is_suffocating", suffocating);
            }

            var viewBlocking = isViewBlocking.test(state, EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
            if (isDefault || viewBlocking != defaultIsViewBlocking) {
                propertiesObjByState.addProperty("is_view_blocking", viewBlocking);
            }

            var postProcess = hasPostProcess.test(state, EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
            if (isDefault || postProcess != defaultHasPostProcess) {
                propertiesObjByState.addProperty("has_post_process", postProcess);
            }

            var emissive = emissiveRendering.test(state, EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
            if (isDefault || emissive != defaultEmissiveRendering) {
                propertiesObjByState.addProperty("emissive_rendering", emissive);
            }

            if (!propertiesObjByState.isEmpty()) {
                stateObj.add("properties", propertiesObjByState);
            }

            var solidRender = state.isSolidRender(EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
            if (isDefault || solidRender != defaultSolidRender) {
                stateObj.addProperty("solid_render", solidRender);
            }

            var lightBlock = state.getLightBlock(EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
            if (isDefault || lightBlock != defaultLightBlock) {
                stateObj.addProperty("light_block", lightBlock);
            }

            var legacySolid = state.isSolid();
            if (isDefault || legacySolid != defaultLegacySolid) {
                stateObj.addProperty("legacy_solid", legacySolid);
            }

            var propagatesSkylightDown = state.propagatesSkylightDown(EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
            if (isDefault || propagatesSkylightDown != defaultPropagatesSkylightDown) {
                stateObj.addProperty("propagates_skylight_down", propagatesSkylightDown);
            }

            var collisionShape = state.getCollisionShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
            if (isDefault || !collisionShape.toString().equals(defaultCollisionShape.toString())) {
                var aabbArray = new JsonArray();
                if (!collisionShape.isEmpty()) {
                    var bounds = collisionShape.bounds();
                    aabbArray.add(bounds.minX);
                    aabbArray.add(bounds.minY);
                    aabbArray.add(bounds.minZ);
                    aabbArray.add(bounds.maxX);
                    aabbArray.add(bounds.maxY);
                    aabbArray.add(bounds.maxZ);
                }
                stateObj.add("collision_shape", aabbArray);
            }

            var largeCollisionShape = state.hasLargeCollisionShape();
            if (isDefault || largeCollisionShape != defaultLargeCollisionShape) {
                stateObj.addProperty("has_large_collision_shape", largeCollisionShape);
            }

            var isCollisionShapeFullBlock = state.isCollisionShapeFullBlock(EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
            if (isDefault || isCollisionShapeFullBlock != defaultIsCollisionShapeFullBlock) {
                stateObj.addProperty("is_collision_shape_full_block", isCollisionShapeFullBlock);
            }

            var isSignalSource = state.isSignalSource();
            if (isDefault || isSignalSource != defaultIsSignalSource) {
                stateObj.addProperty("is_signal_source", isSignalSource);
            }

            var signal = state.getSignal(EmptyBlockGetter.INSTANCE, BlockPos.ZERO, Direction.UP);
            if (isDefault || signal != defaultSignal) {
                stateObj.addProperty("signal", signal);
            }

            var renderShape = state.getRenderShape();
            if (isDefault || renderShape != defaultRenderShape) {
                stateObj.addProperty("render_shape", renderShape.name().toLowerCase());
            }

            var fluidState = state.getFluidState();
            if (isDefault || fluidState != defaultFluidState) {
                var fluidObj = new JsonObject();
                fluidObj.addProperty("fluid", BuiltInRegistries.FLUID.wrapAsHolder(fluidState.getType()).getRegisteredName());
                fluidObj.addProperty("state", getStateString(fluidState.getType().getStateDefinition(), fluidState.toString()));
                stateObj.add("fluid_state", fluidObj);
            }

            var burnOdd = burnOdds.getInt(block.defaultBlockState());
            if (isDefault || burnOdd != defaultBurnOdds) {
                stateObj.addProperty("burn_odds", burnOdd);
            }

            var igniteOdd = igniteOdds.getInt(block.defaultBlockState());
            if (isDefault || igniteOdd != defaultIgniteOdds) {
                stateObj.addProperty("ignite_odds", igniteOdd);
            }

            statesObj.add(stateString, stateObj);
        });
        entryObj.add("states", statesObj);

    }

    private static @NotNull JsonObject getSoundObj(SoundType soundType) {
        var soundObj = new JsonObject();
        soundObj.addProperty("volume", soundType.getVolume());
        soundObj.addProperty("pitch", soundType.getPitch());
        soundObj.addProperty("break_sound", soundType.getBreakSound().getLocation().toString());
        soundObj.addProperty("step_sound", soundType.getStepSound().getLocation().toString());
        soundObj.addProperty("place_sound", soundType.getPlaceSound().getLocation().toString());
        soundObj.addProperty("hit_sound", soundType.getHitSound().getLocation().toString());
        soundObj.addProperty("fall_sound", soundType.getFallSound().getLocation().toString());
        return soundObj;
    }

    private static <T extends Comparable<T>> void handleProperty(Block block, Property<T> property, JsonObject inOut) {
        var propObj = new JsonObject();
        var possibleValues = new JsonArray();
        property.getPossibleValues().forEach(e -> possibleValues.add(property.getName(e)));
        propObj.add("possible_values", possibleValues);
        propObj.addProperty("default_value", property.getName(block.defaultBlockState().getValue(property)));
        inOut.add(property.getName(), propObj);
    }

    private static <O, S extends StateHolder<O, S>> String getStateString(StateDefinition<O, S> stateDefinition, String in) {
        if (stateDefinition.getProperties().isEmpty()) {
            return "default";
        } else {
            return in.substring(in.indexOf('[') + 1, in.length() - 1);
        }
    }

    private static Object getPropertiesPrivateField(String fieldName, Block.Properties properties) {
        try {
            var field = Block.Properties.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(properties);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static Object getFireBlockPrivateField(String fieldName, FireBlock block) {
        try {
            var field = FireBlock.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(block);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}