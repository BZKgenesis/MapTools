# BZK Map Tools

A mod that adds 2 entities that help map makers with player interactions.

# Installation

- Download neoforge for Minecraft 1.21.1
- Place the mod in the mods folder of your instance
- launch your instance

# Features

The entities are only visible when holding the corresponding tool, they are not interactible otherwise

The entities can't be killed by the `kill` command, you will need to use the `discard` command in order to remove them by command. (the `discard` command uses the discard method on entity, it bypasses minecraft logic on death use it with precaution on other entities, it can be use on player but I don't recommand it)

For each entity you can:
- change the Display Name for organization purposes. For example if you use the `say` command, the output will have the display name of that field.
- assign multiple commands with each command a Trigger that specify on what event the command should execute (see the trigger description section), the commands are executed from top to bottom.
- spawn it by using the corresponding tool which can be found in the MapTools creative menu.
- remove them by left clicking on the entity with the corresponding tool.
- edit them by right clicking on them with the corresponding tool.
- copy them by middle clicking on them with the corresponding tool, it will give you the same tool with enchantment glint and custom data that allows you to spawn the same entity.

There is a Gizmo indicator in the entity menu to visualize where you are looking at.


## Redstone Receiver
The redstone receiver allows you to give any block the behavior of a command blocs with a few more features.
A Redstone Receiver is bind to a block and detects if the bloc to which it is binded is powered by external blocks.

Triggers :
| Name       | Behavior                                              |
|------------|--------------------------------------------------------|
| ON_SIGNAL  | execute the command on the tick the block is powered   |
| OFF_SIGNAL | execute the command on the tick the block is unpowered |
| TICK       | execute the command every tick the block is powered    |


## Entity Detector
The entity detector allows you to detect entities in an area. The area can be any size but the interaction box is fixed.

triggers:
| Name            | Behavior                                                                                                                                  |
|-----------------|--------------------------------------------------------------------------------------------------------------------------------------------|
| ON_ENTER        | execute the command when an entity enters the area, the `@s` selector will target that entity                                              |
| ON_LEAVE        | execute the command when an entity leaves the area, the `@s` selector will target that entity                                              |
| TICK            | execute the command every tick when an entity is in the area, it will executes once NOT once per entity                                    |
| TICK_PER_ENTITY | execute the command every tick when an entity is in the area, it will executes once per entity, the `@s` selector will target that entity |
