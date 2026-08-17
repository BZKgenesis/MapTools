package net.bzkgns.maptools.entities.entity_detector;

import net.bzkgns.maptools.Config;
import net.bzkgns.maptools.Maptools;
import net.bzkgns.maptools.ModEntityDataSerializers;
import net.bzkgns.maptools.data_components.ModDataComponents;
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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.RelativeMovement;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.entity.IEntityWithComplexSpawn;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.util.*;

import javax.annotation.Nullable;

public class EntityDetector extends Entity implements IEntityWithComplexSpawn {

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

    public EntityDetector(EntityType<EntityDetector> entityType, Level level) {
        super(entityType, level);
        this.setNoGravity(true);
        this.noPhysics = true;
    }

    public void setConfigNoPos(EntityDetectorConfig config) {
        this.setSize(config.sizeX(), config.sizeY(), config.sizeZ());
        this.setCustomName(Component.literal(config.displayName()));

        this.setZoneId(config.zoneId());

        this.setEnabled(config.enabled());
        this.setCommands(config.commands());
    }

    public void setConfig(EntityDetectorConfig config) {
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

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_COMMANDS, new ArrayList<>());
        builder.define(ENABLED, true);
        builder.define(ENTITY_DETECTED, false);
        builder.define(ZONE_ID, "default");
        builder.define(SIZE_X, DEFAULT_SIZE_X);
        builder.define(SIZE_Y, DEFAULT_SIZE_Y);
        builder.define(SIZE_Z, DEFAULT_SIZE_Z);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {

        this.setEnabled(tag.getBoolean("enabled"));

        setEntityDetected(tag.getBoolean("entity_detected"));
        if (tag.contains("size_x")) {
            setSize(tag.getFloat("size_x"), tag.getFloat("size_y"), tag.getFloat("size_z"));
        }

        this.setZoneId(tag.getString("zone_id"));
        List<EntityDetectorCommand> commands = new ArrayList<>();

        ListTag commandsTag = tag.getList("Commands", Tag.TAG_COMPOUND);

        for (int i = 0; i < commandsTag.size(); i++) {
            CompoundTag commandTag = commandsTag.getCompound(i);

            String command = commandTag.getString("Command");

            EntityDetectorTrigger trigger;

            try {
                trigger = EntityDetectorTrigger.valueOf(
                        commandTag.getString("Trigger"));
            } catch (IllegalArgumentException e) {
                trigger = EntityDetectorTrigger.ON_ENTER;
            }

            commands.add(
                    new EntityDetectorCommand(command, trigger));
        }
        this.setCommands(commands);
    }

    @Override
    protected void addAdditionalSaveData(@NotNull CompoundTag tag) {
        ListTag commandsTag = new ListTag();

        for (EntityDetectorCommand entityDetectorCommand : this.getCommands()) {
            CompoundTag commandTag = new CompoundTag();

            commandTag.putString(
                    "Command",
                    entityDetectorCommand.getCommand());

            commandTag.putString(
                    "Trigger",
                    entityDetectorCommand.getTrigger().name());

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

    public boolean isEntityDetected() {
        return this.entityData.get(ENTITY_DETECTED);
    }

    private void setEntityDetected(boolean detected) {
        this.entityData.set(ENTITY_DETECTED, detected);
    }

    public Vector3f getSize() {
        return new Vector3f(
                this.entityData.get(SIZE_X),
                this.entityData.get(SIZE_Y),
                this.entityData.get(SIZE_Z));
    }

    public void setSize(float x, float y, float z) {
        this.entityData.set(SIZE_X, x);
        this.entityData.set(SIZE_Y, y);
        this.entityData.set(SIZE_Z, z);
    }

    public boolean isEnabled() {
        return this.entityData.get(ENABLED);
    }

    public void setEnabled(boolean enabled) {
        this.entityData.set(ENABLED, enabled);
    }

    public List<EntityDetectorCommand> getCommands() {
        return this.entityData.get(DATA_COMMANDS);
    }

    public void setCommands(List<EntityDetectorCommand> commands) {
        this.entityData.set(DATA_COMMANDS, List.copyOf(commands));
    }

    public String getZoneId() {
        return this.entityData.get(ZONE_ID);
    }

    public void setZoneId(String zoneId) {
        this.entityData.set(ZONE_ID, zoneId);
    }

    private String tagInZone() {
        return "zone_" + getZoneId() + "_in";
    }

    private String tagLast() {
        return "zone_" + getZoneId() + "_last";
    }

    private AABB computeDetectionBox() {
        Vec3 pos = this.position();
        return new AABB(
                pos.x - getSize().x / 2, pos.y - getSize().y / 2 + .5f, pos.z - getSize().z / 2,
                pos.x + getSize().x / 2, pos.y + getSize().y / 2 + .5f, pos.z + getSize().z / 2);
    }

    @Override
    public void tick() {
        this.baseTick();
        if (!isEnabled())
            return;
        if (this.level().isClientSide())
            return;

        AABB detectionBox = computeDetectionBox();
        var currentEntities = this.level().getEntitiesOfClass(Entity.class, detectionBox, e -> e != this);

        Set<UUID> currentIds = new HashSet<>();
        for (Entity entity : currentEntities) {
            currentIds.add(entity.getUUID());
        }

        for (UUID uuid : previouslyInZone) {
            if (!currentIds.contains(uuid)) {
                Entity leaving = ((ServerLevel) this.level()).getEntity(uuid);
                if (leaving != null) {
                    leaving.removeTag(tagInZone());
                    executeCommands(EntityDetectorTrigger.ON_LEAVE, leaving);
                }
            }
        }

        boolean someoneEntered = false;
        for (Entity entity : currentEntities) {
            entity.addTag(tagInZone());
            if (!previouslyInZone.contains(entity.getUUID())) {
                someoneEntered = true;

                entity.addTag(tagLast());
                executeCommands(EntityDetectorTrigger.ON_ENTER, entity);
            } else {
                entity.removeTag(tagLast());
            }
        }

        previouslyInZone = currentIds;
        setEntityDetected(!currentIds.isEmpty());

        executeCommands(EntityDetectorTrigger.TICK, this);
    }

    private void executeCommands(EntityDetectorTrigger trigger, Entity contextEntity) {
        for (EntityDetectorCommand entityDetectorCommand : this.getCommands()) {
            if (entityDetectorCommand.getTrigger() == trigger) {
                executeCommand(entityDetectorCommand, contextEntity);
            }
        }
    }

    private void executeCommand(EntityDetectorCommand entityDetectorCommand, Entity contextEntity) {
        String command = entityDetectorCommand.getCommand();
        if (command == null || command.isBlank())
            return;
        MinecraftServer server = this.level().getServer();
        if (server == null)
            return;

        CommandSourceStack source = createSourceStack(contextEntity);
        var dispatcher = server
                .getCommands()
                .getDispatcher();
        var parseResults = dispatcher.parse(command, source);
        try {
            server.getCommands().performCommand(parseResults, command);
        } catch (Exception e) {

            Maptools.LOGGER.error("[" + getDisplayName().getString() + "] Error executing command '{}': {}", command,
                    e.getMessage());
        }
    }

    private CommandSourceStack createSourceStack(Entity contextEntity) {
        Entity anchor = contextEntity != null ? contextEntity : this;
        Vec3 pos = anchor.position();
        Vec2 rot = new Vec2(anchor.getXRot(), anchor.getYRot());
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

    @Override
    public boolean teleportTo(@NotNull ServerLevel level, double x, double y, double z,
            @NotNull Set<RelativeMovement> relativeMovements, float yRot, float xRot) {
        return false;
    }

    @Override
    public void teleportRelative(double dx, double dy, double dz) {

    }

    @Override
    public void teleportTo(double x, double y, double z) {

    }

    @Override
    public void kill() {
        var server = this.level().getServer();
        if (server == null)
            return;
        if (!Config.SHOW_WARNING_MESSAGE_KILL.getAsBoolean())
            return;
        MutableComponent msg = Component.literal("You can't kill en MapTools entity with the ").withColor(0xFFFF0000)
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
        ItemStack stack = new ItemStack(
                ModItems.ENTITY_DETECTOR_EDITOR.get());

        stack.set(
                ModDataComponents.ENTITY_DETECTOR_CONFIG.get(),
                new EntityDetectorConfig(
                        this.isEnabled(),
                        List.copyOf(this.getCommands()),
                        this.hasCustomName()
                                ? this.getCustomName().getString()
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
            Minecraft mc = Minecraft.getInstance();
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
        return true;
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
    protected boolean canAddPassenger(@NotNull Entity passenger) {
        return false;
    }

    @Override
    protected void addPassenger(@NotNull Entity passenger) {
        throw new IllegalStateException(
                "EntityDetector cannot have passengers");
    }

    @Override
    public void writeSpawnData(RegistryFriendlyByteBuf buf) {
        Vector3f size = this.getSize();
        buf.writeFloat(size.x);
        buf.writeFloat(size.y);
        buf.writeFloat(size.z);
    }

    @Override
    public void readSpawnData(RegistryFriendlyByteBuf buf) {
        float sizeX = buf.readFloat();
        float sizeY = buf.readFloat();
        float sizeZ = buf.readFloat();
        setSize(sizeX, sizeY, sizeZ);
    }
}
