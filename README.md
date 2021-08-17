# Somnia Fabric

This mod is a port to fabric of [Somnia Awoken](https://github.com/Su5eD/Somnia) by Su5eD:<br>
A Minecraft mod that simulates the level while you sleep, initially released in 2011.

## How it works
Instead of skipping the night, the level is sped up while you sleep.<br>
You can now sleep at any time, as long as you have enough fatigue, which you gain passively over time.<br>
Be careful - getting too tired gives you negative effects.

[Comment]: <> (Add image/gif etc here.)

## Install Instructions

### Installing
1. Download this mod through GitHub releases, CurseForge or Modrinth.
2. Download the [Fabric ModLoader](https://fabricmc.net/use/).
3. Download the dependencies:
    * [Fabric API](https://www.curseforge.com/minecraft/mc-mods/fabric-api)
    * [Cardinal Components API](https://www.curseforge.com/minecraft/mc-mods/cardinal-components)
4. Add the mod and its dependencies to your modded Fabric instance.
5. Run the game.

### Building From Source
1. Have JDK 16.
2. Clone this repository.
3. Navigate to the cloned source folder.
4. Open a terminal.
5. Run:
    * Windows Command Prompt: `gradlew build`
    * Bash, Powershell, Git Bash: `./gradlew build`
6. Get the built JAR inside `./build/libs` 
