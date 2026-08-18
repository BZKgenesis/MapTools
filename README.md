# BZK Map Tools

A mod that adds entities to help map makers with player interactions.

# Installation

- Install NeoForge for Minecraft 1.21.1
- Place the mod in the `mods` folder of your Minecraft instance.
- Launch your instance.

# Features

The entities are only visible when holding their corresponding tool. They cannot be interacted with otherwise.

The entities cannot be killed by the `kill` command. You will need to use the `discard` command to remove them by command. The `discard` command directly calls the `discard` method on the entity, bypassing Minecraft's normal death logic. *Use it with caution on other entities*, as it can be use on players, although this is not recommand.

For each entity, you can:
- Change the Display Name for organizational purposes. For example, when using the `say` command, the output will use the display name of that field.
- Assign multiple commands, each wth a Trigger that specifies when the command should be executed (see the Trigger description section). Commands are executed from top to bottom.
- Spawn it using the corresponding tool, which can be found in the MapTools creative menu.
- Remove it by left-clicking the entity with the corresponding tool.
- Edit it by right-clicking the entity with the corresponding tool.
- Copy it by middle-clicking the entity with the corresponding tool. This gives you the same tool with an enchantment glint and custom data, allowing you to spawn an identical entity.
- Use the Gizmo indicator in the top left corner of the entity's menu to visualize where you are looking.



## Redstone Receiver
The **Redstone Receiver** allows you to give any block the behavior of a command block, with a few additional features.

A Redstone Receiver is bound to a block and detects whether the bloc it is bound to is powered by an external redstone signal.


Triggers

| Name         | Behavior                                              |
|--------------|--------------------------------------------------------|
| `ON_SIGNAL`  | Executes the command on the tick when the block becomes powered.   |
| `OFF_SIGNAL` | Executes the command on the tick when the block becomes unpowered. |
| `TICK`       | Executes the command every tick while the block is powered    |


## Entity Detector
The **Entity Detector** allows you to detect entities whithin an area. The area can be any size, while the interaction box remains fixed.

Triggers

| Name            | Behavior                                                                                                                                  |
|-----------------|--------------------------------------------------------------------------------------------------------------------------------------------|
| ON_ENTER        | Executes the command when an entity enters the area. The `@s` selector targets that entity.                                              |
| ON_LEAVE        | Executes the command when an entity leaves the area. The `@s` selector targets that entity.                                              |
| TICK            | Executes the command every tick while at least one entity is in the area. The command is executed once per tick regardless of the number of entities in the area.|
| TICK_PER_ENTITY | Executes the command every tick for each entity in the area. The `@s` selector targets the corresponding entity. |
