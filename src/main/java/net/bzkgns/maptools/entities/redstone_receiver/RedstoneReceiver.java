package net.bzkgns.maptools.entities.redstone_receiver;

import com.mojang.authlib.minecraft.client.MinecraftClient;
import net.bzkgns.maptools.Config;
import net.bzkgns.maptools.Maptools;
import net.bzkgns.maptools.ModEntityDataSerializers;
import net.bzkgns.maptools.data_components.ModDataComponents;
import net.bzkgns.maptools.items.ModItems;
import net.bzkgns.maptools.network.S2C.SyncRedstoneReceiverPayload;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentType;
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
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.RelativeMovement;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.entity.IEntityWithComplexSpawn;
import net.neoforged.neoforge.network.PacketDistributor;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class RedstoneReceiver extends Entity {

    private static final EntityDataAccessor<List<RedstoneReceiverCommand>> DATA_COMMANDS = SynchedEntityData
            .defineId(RedstoneReceiver.class, ModEntityDataSerializers.REDSTONE_RECEIVER_COMMANDS.get());
    private static final EntityDataAccessor<Boolean> POWERED = SynchedEntityData.defineId(
            RedstoneReceiver.class,
            EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> ENABLED = SynchedEntityData.defineId(
            RedstoneReceiver.class,
            EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> XRAY_VISIBLE = SynchedEntityData.defineId(
            RedstoneReceiver.class,
            EntityDataSerializers.BOOLEAN);

    private int successCount = 0;

    public RedstoneReceiver(EntityType<? extends RedstoneReceiver> entityType, Level level) {
        super(entityType, level);
        this.setCustomName(Component.literal("Redstone Receiver"));
    }

    public void setConfig(RedstoneReceiverConfig config) {
        this.setCustomName(Component.literal(config.displayName()));
        this.setEnabled(config.enabled());
        this.setCommands(config.commands());
        this.setXrayVisible(config.xrayVisible());
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_COMMANDS, new ArrayList<>());
        builder.define(POWERED, false);
        builder.define(ENABLED, true);
        builder.define(XRAY_VISIBLE, true);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        List<RedstoneReceiverCommand> commands = new ArrayList<>();

        ListTag commandsTag = tag.getList("Commands", Tag.TAG_COMPOUND);

        for (int i = 0; i < commandsTag.size(); i++) {
            CompoundTag commandTag = commandsTag.getCompound(i);

            String command = commandTag.getString("Command");

            RedstoneReceiverTrigger trigger;

            try {
                trigger = RedstoneReceiverTrigger.valueOf(
                        commandTag.getString("Trigger"));
            } catch (IllegalArgumentException e) {
                trigger = RedstoneReceiverTrigger.ON_SIGNAL;
            }

            commands.add(
                    new RedstoneReceiverCommand(command, trigger));
        }
        this.setCommands(commands);

        setXrayVisible(tag.getBoolean("XrayVisible"));

        successCount = tag.getInt("SuccessCount");
        setPowered(tag.getBoolean("Powered"));
    }

    @Override
    protected void addAdditionalSaveData(@NotNull CompoundTag tag) {
        ListTag commandsTag = new ListTag();

        for (RedstoneReceiverCommand receiverCommand : getCommands()) {
            CompoundTag commandTag = new CompoundTag();

            commandTag.putString(
                    "Command",
                    receiverCommand.getCommand());

            commandTag.putString(
                    "Trigger",
                    receiverCommand.getTrigger().name());

            commandsTag.add(commandTag);
        }

        tag.put("Commands", commandsTag);
        tag.putInt("SuccessCount", successCount);
        tag.putBoolean("Powered", isPowered());
        tag.putBoolean("XrayVisible", isXrayVisible());
    }

    @Override
    public void tick() {
        super.tick();
        if (!isEnabled())
            return;
        if (level().isClientSide())
            return;

        BlockPos pos = blockPosition();

        int redstonePower = level().getBestNeighborSignal(pos);

        if (redstonePower > 0) {
            if (!this.isPowered()) {
                this.setPowered(true);
                executeCommands(RedstoneReceiverTrigger.ON_SIGNAL);
            }
            executeCommands(RedstoneReceiverTrigger.PULSE);
        } else {
            if (this.isPowered()) {
                this.setPowered(false);
                executeCommands(RedstoneReceiverTrigger.OFF_SIGNAL);
            }
        }
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
    public boolean shouldBeSaved() {
        return true;
    }

    public Boolean isEnabled() {
        return this.entityData.get(ENABLED);
    }

    public List<RedstoneReceiverCommand> getCommands() {
        return this.entityData.get(DATA_COMMANDS);
    }

    public void setCommands(List<RedstoneReceiverCommand> commands) {
        this.entityData.set(DATA_COMMANDS, List.copyOf(commands));
    }

    public Boolean isXrayVisible() {
        return this.entityData.get(XRAY_VISIBLE);
    }

    public boolean isPowered() {
        return this.entityData.get(POWERED);
    }

    public void setEnabled(Boolean enabled) {
        this.entityData.set(ENABLED, enabled);
    }

    public void setXrayVisible(Boolean visible) {
        this.entityData.set(XRAY_VISIBLE, visible);
    }

    private void setPowered(Boolean powered) {
        this.entityData.set(POWERED, powered);
    }

    private void executeCommands(RedstoneReceiverTrigger trigger) {
        for (RedstoneReceiverCommand receiverCommand : getCommands()) {
            if (receiverCommand.getTrigger() == trigger) {
                executeCommand(receiverCommand);
            }
        }
    }

    private void executeCommand(RedstoneReceiverCommand receiverCommand) {
        String command = receiverCommand.getCommand();

        if (command.isBlank()) {
            return;
        }

        CommandSourceStack source = this.createCommandSourceStack()
                .withPermission(2)
                .withCallback((success, result) -> {

                    if (success) {
                        ++this.successCount;
                    }
                });
        var server = this.getServer();
        if (server == null)
            return;
        var dispatcher = server
                .getCommands()
                .getDispatcher();
        var parseResults = dispatcher.parse(command, source);

        this.getServer()
                .getCommands()
                .performCommand(parseResults, command);

    }

    @Override
    @Nullable
    public ItemStack getPickResult() {
        ItemStack stack = new ItemStack(
                ModItems.REDSTONE_RECEIVER_EDITOR.get());

        stack.set(
                ModDataComponents.REDSTONE_RECEIVER_CONFIG.get(),
                new RedstoneReceiverConfig(
                        this.isEnabled(),
                        List.copyOf(this.getCommands()),
                        this.hasCustomName()
                                ? this.getCustomName().getString()
                                : "Redstone Receiver",
                        this.isXrayVisible()));

        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        return stack;
    }

    @Override
    public boolean isPickable() {
        if (level().isClientSide) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null)
                return false;
            return mc.player.getMainHandItem().is(ModItems.REDSTONE_RECEIVER_EDITOR.get());
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
                "RedstoneReceiver cannot have passengers");
    }
}
