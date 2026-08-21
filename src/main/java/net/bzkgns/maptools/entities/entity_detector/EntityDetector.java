package net.bzkgns.maptools.entities.entity_detector;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nullable;

import net.minecraft.world.entity.*;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.ScoreHolder;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import net.bzkgns.maptools.Config;
import net.bzkgns.maptools.Maptools;
import net.bzkgns.maptools.data_components.ModDataComponents;
import net.bzkgns.maptools.entities.ResetableEntity;
import net.bzkgns.maptools.entity_data_serializers.ModEntityDataSerializers;
import net.bzkgns.maptools.items.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.entity.IEntityWithComplexSpawn;

public class EntityDetector extends Entity implements IEntityWithComplexSpawn, ResetableEntity {

    private static final EntityDataAccessor<Boolean> ENABLED = SynchedEntityData.defineId(EntityDetector.class,
            EntityDataSerializers.BOOLEAN);

    private static final EntityDataAccessor<Boolean> ENTITY_DETECTED = SynchedEntityData.defineId(EntityDetector.class,
            EntityDataSerializers.BOOLEAN);

    private static final EntityDataAccessor<List<EntityDetectorCommand>> DATA_COMMANDS = SynchedEntityData
            .defineId(EntityDetector.class, ModEntityDataSerializers.ENTITY_DETECTOR_COMMANDS.get());

    private static final EntityDataAccessor<Float> SIZE_X = SynchedEntityData.defineId(EntityDetector.class,
            EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> SIZE_Y = SynchedEntityData.defineId(EntityDetector.class,
            EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> SIZE_Z = SynchedEntityData.defineId(EntityDetector.class,
            EntityDataSerializers.FLOAT);

    private static final float DEFAULT_SIZE_X = 1.0f;
    private static final float DEFAULT_SIZE_Y = 1.0f;
    private static final float DEFAULT_SIZE_Z = 1.0f;

    private static final EntityDataAccessor<String> ZONE_ID = SynchedEntityData.defineId(EntityDetector.class,
            EntityDataSerializers.STRING);

    private Set<UUID> previouslyInZone = new HashSet<>();
    private final Set<UUID> alreadyEnteredZone = new HashSet<>();
    private final Set<UUID> alreadyLeavedZone = new HashSet<>();
    private final Objective objective;

    public EntityDetector(final EntityType<EntityDetector> entityType, final Level level) {
        super(entityType, level);
        this.setNoGravity(true);
        this.noPhysics = true;
        if (!level.isClientSide()) {
            var server = getServer();
            if (server != null) {
                this.objective = server.getScoreboard().getObjective("map_tools");
            } else {
                this.objective = null;
            }
        } else {
            this.objective = null;
        }
    }

    public void setConfigNoPos(final EntityDetectorConfig config) {
        this.setSize(config.sizeX(), config.sizeY(), config.sizeZ());
        this.setCustomName(Component.literal(config.displayName()));

        this.setZoneId(config.zoneId());

        this.setEnabled(config.enabled());
        this.setCommands(config.commands());
    }

    public void setConfig(final EntityDetectorConfig config) {
        this.setConfigNoPos(config);
        this.setPos(new Vec3(config.posX(), config.posY(), config.posZ()));
    }

    public EntityDetectorConfig getConfig() {
        return new EntityDetectorConfig(
                this.isEnabled(),
                this.getCommands(),
                this.getDisplayName().getString(),
                this.getZoneId(),
                this.getSize().x,
                this.getSize().y,
                this.getSize().z,
                this.getPosition(0).x,
                this.getPosition(0).y,
                this.getPosition(0).z);
    }

    public boolean isEntityDetected() {
        return this.entityData.get(ENTITY_DETECTED);
    }

    public Vector3f getSize() {
        return new Vector3f(
                this.entityData.get(SIZE_X),
                this.entityData.get(SIZE_Y),
                this.entityData.get(SIZE_Z));
    }

    public void setSize(final float x, final float y, final float z) {
        this.entityData.set(SIZE_X, x);
        this.entityData.set(SIZE_Y, y);
        this.entityData.set(SIZE_Z, z);
        this.refreshDimensions();
    }

    @Override
    public @NotNull EntityDimensions getDimensions(@NotNull Pose pose) {
        return EntityDimensions.scalable(Math.min(this.getSize().x, this.getSize().z), this.getSize().y);
    }

    public boolean isEnabled() {
        return this.entityData.get(ENABLED);
    }

    public void setEnabled(final boolean enabled) {
        this.entityData.set(ENABLED, enabled);
    }

    public List<EntityDetectorCommand> getCommands() {
        return this.entityData.get(DATA_COMMANDS);
    }

    public void setCommands(final List<EntityDetectorCommand> commands) {
        this.entityData.set(DATA_COMMANDS, List.copyOf(commands));
    }

    public String getZoneId() {
        return this.entityData.get(ZONE_ID);
    }

    public void setZoneId(final String zoneId) {
        this.entityData.set(ZONE_ID, zoneId);
    }

    @Override
    public void tick() {
        this.baseTick();
        if (this.level().isClientSide())
            return;

        if (!isEnabled())
            return;

        final AABB detectionBox = computeDetectionBox();
        final var currentEntities = this.level().getEntitiesOfClass(
                Entity.class,
                detectionBox,
                e -> e != this);

        final Set<UUID> currentIds = new HashSet<>();
        for (final Entity entity : currentEntities) {
            currentIds.add(entity.getUUID());
        }
        if (objective != null) {
            objective.getScoreboard().getOrCreatePlayerScore(ScoreHolder.forNameOnly(this.tagCount()), objective);
        }

        final ServerLevel serverLevel = (ServerLevel) this.level();

        final boolean zoneWasEmpty = previouslyInZone.isEmpty();

        for (final UUID uuid : previouslyInZone) {
            if (currentIds.contains(uuid))
                continue;

            final Entity entity = serverLevel.getEntity(uuid);
            if (entity == null)
                continue;

            final List<EntityDetectorTrigger> triggers = new ArrayList<>();

            // Cette entité vient de quitter la zone.
            if (alreadyLeavedZone.add(uuid)) {
                triggers.add(EntityDetectorTrigger.LEAVE_ONLY_ONCE);
            }

            triggers.add(EntityDetectorTrigger.ON_LEAVE);

            if (currentIds.isEmpty()) {
                triggers.add(EntityDetectorTrigger.ON_LAST_LEAVE);
            }

            entity.removeTag(tagInZone());

            executeCommands(
                    entity,
                    triggers.toArray(EntityDetectorTrigger[]::new));
        }

        boolean firstEntityEntered = false;

        for (final Entity entity : currentEntities) {
            final UUID uuid = entity.getUUID();

            final boolean wasInZone = previouslyInZone.contains(uuid);
            final boolean isEntering = !wasInZone;

            final List<EntityDetectorTrigger> triggers = new ArrayList<>();

            entity.addTag(tagInZone());

            if (isEntering) {
                if (alreadyEnteredZone.add(uuid)) {
                    triggers.add(EntityDetectorTrigger.ENTER_ONLY_ONCE);
                }

                triggers.add(EntityDetectorTrigger.ON_ENTER);

                if (zoneWasEmpty && !firstEntityEntered) {
                    triggers.add(EntityDetectorTrigger.ON_FIRST_ENTER);
                    firstEntityEntered = true;
                }

                entity.addTag(tagLast());
            } else {
                entity.removeTag(tagLast());
            }

            triggers.add(EntityDetectorTrigger.TICK_PER_ENTITY);

            executeCommands(
                    entity,
                    triggers.toArray(EntityDetectorTrigger[]::new));
        }

        if (!currentIds.isEmpty()) {
            executeCommands(this, EntityDetectorTrigger.TICK);
        }

        previouslyInZone = currentIds;
        setEntityDetected(!currentIds.isEmpty());
    }

    public void reset() {
        alreadyEnteredZone.clear();
        alreadyLeavedZone.clear();
    }

    @Override
    public boolean teleportTo(@NotNull final ServerLevel level, final double x, final double y, final double z,
            @NotNull final Set<RelativeMovement> relativeMovements, final float yRot, final float xRot) {
        return false;
    }

    @Override
    public void teleportRelative(final double dx, final double dy, final double dz) {

    }

    @Override
    public void teleportTo(final double x, final double y, final double z) {

    }

    @Override
    public void kill() {
        final var server = this.level().getServer();
        if (server == null)
            return;
        if (!Config.SHOW_WARNING_MESSAGE_KILL.getAsBoolean())
            return;
        final MutableComponent msg = Component.literal("You can't kill en MapTools entity with the ")
                .withColor(0xFFFF0000)
                .append(Component.literal("/kill").withStyle(ChatFormatting.YELLOW, ChatFormatting.UNDERLINE))
                .append(Component.literal(" command, you need to use the ")).withColor(0xFFFF0000)
                .append(Component.literal("/discard").withStyle(ChatFormatting.YELLOW, ChatFormatting.UNDERLINE))
                .append(Component.literal(" command. You can disable this message in the config or by clicking "))
                .append(Component.literal("here.").withStyle(style -> style.withColor(ChatFormatting.WHITE)
                        .applyFormat(ChatFormatting.UNDERLINE).withClickEvent(

                                new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/mt config showWarningKill false"))));
        server.getPlayerList().broadcastSystemMessage(msg, false);
    }

    @Override
    @Nullable
    public ItemStack getPickResult() {
        final ItemStack stack = new ItemStack(
                ModItems.ENTITY_DETECTOR_EDITOR.get());

        stack.set(
                ModDataComponents.ENTITY_DETECTOR_CONFIG.get(),
                new EntityDetectorConfig(
                        this.isEnabled(),
                        List.copyOf(this.getCommands()),
                        this.hasCustomName()
                                ? this.getDisplayName().getString()
                                : "Entity Detector",
                        this.getZoneId(),
                        this.getSize().x,
                        this.getSize().y,
                        this.getSize().z,
                        this.getPosition(0).x,
                        this.getPosition(0).y,
                        this.getPosition(0).z

                ));

        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        return stack;
    }

    @Override
    public boolean isPickable() {
        if (level().isClientSide) {
            final Minecraft mc = Minecraft.getInstance();
            if (mc.player == null)
                return false;
            return mc.player.getMainHandItem().is(ModItems.ENTITY_DETECTOR_EDITOR.get());
        }
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean isCustomNameVisible() {
        return false;
    }

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    @Override
    public boolean isInvulnerable() {
        return true;
    }

    @Override
    public boolean isIgnoringBlockTriggers() {
        return true;
    }

    @Override
    public @NotNull PushReaction getPistonPushReaction() {
        return PushReaction.IGNORE;
    }

    @Override
    public void writeSpawnData(final RegistryFriendlyByteBuf buf) {
        final Vector3f size = this.getSize();
        buf.writeFloat(size.x);
        buf.writeFloat(size.y);
        buf.writeFloat(size.z);
    }

    @Override
    public void readSpawnData(final RegistryFriendlyByteBuf buf) {
        final float sizeX = buf.readFloat();
        final float sizeY = buf.readFloat();
        final float sizeZ = buf.readFloat();
        setSize(sizeX, sizeY, sizeZ);
    }

    @Override
    protected void defineSynchedData(final SynchedEntityData.Builder builder) {
        builder.define(DATA_COMMANDS, new ArrayList<>());
        builder.define(ENABLED, true);
        builder.define(ENTITY_DETECTED, false);
        builder.define(ZONE_ID, "default");
        builder.define(SIZE_X, DEFAULT_SIZE_X);
        builder.define(SIZE_Y, DEFAULT_SIZE_Y);
        builder.define(SIZE_Z, DEFAULT_SIZE_Z);
    }

    @Override
    protected void readAdditionalSaveData(final CompoundTag tag) {

        this.setEnabled(tag.getBoolean("enabled"));

        setEntityDetected(tag.getBoolean("entity_detected"));
        if (tag.contains("size_x")) {
            setSize(tag.getFloat("size_x"), tag.getFloat("size_y"), tag.getFloat("size_z"));
        }

        this.setZoneId(tag.getString("zone_id"));
        final List<EntityDetectorCommand> commands = new ArrayList<>();

        final ListTag commandsTag = tag.getList("Commands", Tag.TAG_COMPOUND);

        for (int i = 0; i < commandsTag.size(); i++) {
            final CompoundTag commandTag = commandsTag.getCompound(i);

            final String command = commandTag.getString("Command");

            boolean enabled;

            try {
                enabled = commandTag.getBoolean("Enabled");
            } catch (final IllegalArgumentException e) {
                enabled = true;
            }

            EntityDetectorTrigger trigger;

            try {
                trigger = EntityDetectorTrigger.valueOf(
                        commandTag.getString("Trigger"));
            } catch (final IllegalArgumentException e) {
                trigger = EntityDetectorTrigger.ON_ENTER;
            }

            commands.add(
                    new EntityDetectorCommand(command, trigger, enabled));
        }
        this.setCommands(commands);
    }

    @Override
    protected void addAdditionalSaveData(@NotNull final CompoundTag tag) {
        final ListTag commandsTag = new ListTag();

        for (final EntityDetectorCommand entityDetectorCommand : this.getCommands()) {
            final CompoundTag commandTag = new CompoundTag();

            commandTag.putString(
                    "Command",
                    entityDetectorCommand.getCommand());

            commandTag.putString(
                    "Trigger",
                    entityDetectorCommand.getTrigger().name());

            commandTag.putBoolean("Enabled", entityDetectorCommand.isEnabled());

            commandsTag.add(commandTag);
        }

        tag.put("Commands", commandsTag);
        tag.putBoolean("enabled", isEnabled());
        tag.putBoolean("entity_detected", isEntityDetected());
        tag.putFloat("size_x", getSize().x);
        tag.putFloat("size_y", getSize().y);
        tag.putFloat("size_z", getSize().z);
        tag.putString("zone_id", this.getZoneId());
    }

    @Override
    protected boolean canAddPassenger(@NotNull final Entity passenger) {
        return false;
    }

    @Override
    protected void addPassenger(@NotNull final Entity passenger) {
        throw new IllegalStateException(
                "EntityDetector cannot have passengers");
    }

    private void setEntityDetected(final boolean detected) {
        this.entityData.set(ENTITY_DETECTED, detected);
    }

    private String tagInZone() {
        return "zone_" + getZoneId() + "_in";
    }

    private String tagLast() {
        return "zone_" + getZoneId() + "_last";
    }

    private String tagCount() {
        return "zone_" + getZoneId() + "_count";
    }

    private AABB computeDetectionBox() {
        final Vec3 pos = this.position();
        return new AABB(
                pos.x - getSize().x / 2, pos.y - getSize().y / 2 + getSize().y / 2f, pos.z - getSize().z / 2,
                pos.x + getSize().x / 2, pos.y + getSize().y / 2 + getSize().y / 2f, pos.z + getSize().z / 2);
    }

    private void executeCommands(final Entity contextEntity, final EntityDetectorTrigger... triggersArray) {
        final List<EntityDetectorTrigger> triggers = List.of(triggersArray);
        for (final EntityDetectorCommand entityDetectorCommand : this.getCommands()) {
            if (triggers.contains(entityDetectorCommand.getTrigger())) {
                if (entityDetectorCommand.isEnabled())
                    executeCommand(entityDetectorCommand, contextEntity);
            }
        }
    }

    private void executeCommand(final EntityDetectorCommand entityDetectorCommand, final Entity contextEntity) {
        final String command = entityDetectorCommand.getCommand();
        if (command == null || command.isBlank())
            return;
        final MinecraftServer server = this.level().getServer();
        if (server == null)
            return;

        final CommandSourceStack source = createSourceStack(contextEntity);
        final var dispatcher = server
                .getCommands()
                .getDispatcher();
        final var parseResults = dispatcher.parse(command, source);
        try {
            server.getCommands().performCommand(parseResults, command);
        } catch (final Exception e) {

            Maptools.LOGGER.error("[" + getDisplayName().getString() + "] Error executing command '{}': {}", command,
                    e.getMessage());
        }
    }

    private CommandSourceStack createSourceStack(final Entity contextEntity) {
        final Entity anchor = contextEntity != null ? contextEntity : this;
        final Vec3 pos = anchor.position();
        final Vec2 rot = new Vec2(anchor.getXRot(), anchor.getYRot());
        return new CommandSourceStack(
                CommandSource.NULL,
                pos,
                rot,
                (ServerLevel) this.level(),
                2,
                anchor.getName().getString(),
                anchor.getDisplayName(),
                Objects.requireNonNull(this.level().getServer()),
                anchor);
    }
}
