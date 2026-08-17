package net.bzkgns.maptools.entities.entity_detector;

import net.bzkgns.maptools.Maptools;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.RelativeMovement;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.util.*;

public class EntityDetector extends Entity {

    private static final EntityDataAccessor<Boolean> ENABLED =
            SynchedEntityData.defineId(EntityDetector.class, EntityDataSerializers.BOOLEAN);

    private static final EntityDataAccessor<Boolean> ENTITY_DETECTED =
            SynchedEntityData.defineId(EntityDetector.class, EntityDataSerializers.BOOLEAN);


    private final List<EntityDetectorCommand> commands = new ArrayList<>();

    private static final float DEFAULT_SIZE_X = 3.0f;
    private static final float DEFAULT_SIZE_Y = 1.0f;
    private static final float DEFAULT_SIZE_Z = 2.0f;

    private final Vector3f size = new Vector3f(DEFAULT_SIZE_X, DEFAULT_SIZE_Y, DEFAULT_SIZE_Z);

    private static final EntityDataAccessor<String> ZONE_ID =
            SynchedEntityData.defineId(EntityDetector.class, EntityDataSerializers.STRING);



    private Set<UUID> previouslyInZone = new HashSet<>();

    public EntityDetector(EntityType<EntityDetector> entityType, Level level) {
        super(entityType, level);
        this.setNoGravity(true);
        this.noPhysics = true;
    }


    public void setConfig(EntityDetectorConfig config) {
        this.setSize(config.sizeX(), config.sizeY(), config.sizeZ());
        this.setCustomName(Component.literal(config.displayName()));

        this.setZoneId(config.zoneId());

        this.setEnabled(config.enabled());
        this.setCommands(config.commands());
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
                this.getPosition(0).z
        );
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(ENABLED, true);
        builder.define(ENTITY_DETECTED, false);
        builder.define(ZONE_ID, "default");
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {

        this.setEnabled(tag.getBoolean("enabled"));

        setEntityDetected(tag.getBoolean("entity_detected"));
        if (tag.contains("size_x")) {
            setSize(tag.getFloat("size_x"), tag.getFloat("size_y"), tag.getFloat("size_z"));
        }

        this.setZoneId(tag.getString("zone_id"));
        commands.clear();

        ListTag commandsTag = tag.getList("Commands", Tag.TAG_COMPOUND);

        for (int i = 0; i < commandsTag.size(); i++) {
            CompoundTag commandTag = commandsTag.getCompound(i);

            String command = commandTag.getString("Command");

            EntityDetectorTrigger trigger;

            try {
                trigger = EntityDetectorTrigger.valueOf(
                        commandTag.getString("Trigger")
                );
            } catch (IllegalArgumentException e) {
                trigger = EntityDetectorTrigger.ON_ENTER;
            }

            commands.add(
                    new EntityDetectorCommand(command, trigger)
            );
        }
    }

    @Override
    protected void addAdditionalSaveData(@NotNull CompoundTag tag) {
        ListTag commandsTag = new ListTag();

        for (EntityDetectorCommand entityDetectorCommand : commands) {
            CompoundTag commandTag = new CompoundTag();

            commandTag.putString(
                    "Command",
                    entityDetectorCommand.getCommand()
            );

            commandTag.putString(
                    "Trigger",
                    entityDetectorCommand.getTrigger().name()
            );

            commandsTag.add(commandTag);
        }

        tag.put("Commands", commandsTag);
        tag.putBoolean("enabled", isEnabled());
        tag.putBoolean("entity_detected", isEntityDetected());
        tag.putFloat("size_x", size.x);
        tag.putFloat("size_y", size.y);
        tag.putFloat("size_z", size.z);
        tag.putString("zone_id", this.getZoneId());
    }

    public boolean isEntityDetected() { return this.entityData.get(ENTITY_DETECTED); }
    private void setEntityDetected(boolean detected) { this.entityData.set(ENTITY_DETECTED, detected); }
    public Vector3f getSize() { return new Vector3f(size); }
    public void setSize(float x, float y, float z) { this.size.set(x, y, z); }

    public boolean isEnabled() { return this.entityData.get(ENABLED); }
    public void setEnabled(boolean enabled) { this.entityData.set(ENABLED, enabled); }

    public List<EntityDetectorCommand> getCommands() {
        return Collections.unmodifiableList(commands);
    }

    public void setCommands(List<EntityDetectorCommand> commands) {
        this.commands.clear();
        this.commands.addAll(commands);
    }

    @SuppressWarnings("unused")
    public void addCommand(EntityDetectorCommand command) {
        this.commands.add(command);
    }

    @SuppressWarnings("unused")
    public void removeCommand(int index) {
        this.commands.remove(index);
    }

    public String getZoneId() { return this.entityData.get(ZONE_ID); }

    public void setZoneId(String zoneId) { this.entityData.set(ZONE_ID, zoneId); }

    private String tagInZone() { return "zone_" + getZoneId() + "_in"; }
    private String tagLast() { return "zone_" + getZoneId() + "_last"; }

    private AABB computeDetectionBox() {
        Vec3 pos = this.position();
        return new AABB(
                pos.x - size.x / 2, pos.y - size.y / 2, pos.z - size.z / 2,
                pos.x + size.x / 2, pos.y + size.y / 2, pos.z + size.z / 2
        );
    }

    @Override
    public void tick() {
        this.baseTick();
        if (!isEnabled()) return;
        if (this.level().isClientSide()) return;

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
        for (EntityDetectorCommand entityDetectorCommand : commands) {
            if (entityDetectorCommand.getTrigger() == trigger) {
                executeCommand(entityDetectorCommand,contextEntity);
            }
        }
    }

    private void executeCommand(EntityDetectorCommand entityDetectorCommand, Entity contextEntity) {
        String command = entityDetectorCommand.getCommand();
        if (command == null || command.isBlank()) return;
        MinecraftServer server = this.level().getServer();
        if (server == null) return;

        CommandSourceStack source = createSourceStack(contextEntity);
        var dispatcher = server
                .getCommands()
                .getDispatcher();
        var parseResults = dispatcher.parse(command, source);
        try {
            server.getCommands().performCommand(parseResults, command);
        } catch (Exception e) {

            Maptools.LOGGER.error("[EntityDetector] Erreur exécution commande '{}': {}", command, e.getMessage());
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
                anchor
        );
    }

    @Override
    public boolean teleportTo(@NotNull ServerLevel level, double x, double y, double z, @NotNull Set<RelativeMovement> relativeMovements, float yRot, float xRot) {
        return false;
    }

    @Override
    public void teleportRelative(double dx, double dy, double dz) {

    }

    @Override
    public void teleportTo(double x, double y, double z) {

    }

    @Override
    public void kill() {}

    @Override
    public boolean isPickable() {return true;}

    @Override
    public boolean isPushable() {return false;}

    @Override
    public boolean isCustomNameVisible() {
        return true;
    }

    @Override
    public boolean canBeCollidedWith() {return false;}

    @Override
    public boolean isInvulnerable() {return true;}

    @Override
    public boolean isIgnoringBlockTriggers() {return true;}

    @Override
    public @NotNull PushReaction getPistonPushReaction() {
        return PushReaction.IGNORE;
    }

    @Override
    protected boolean canAddPassenger(@NotNull Entity passenger) {return false;}


    @Override
    protected void addPassenger(@NotNull Entity passenger) {
        throw new IllegalStateException(
                "EntityDetector cannot have passengers"
        );
    }
}