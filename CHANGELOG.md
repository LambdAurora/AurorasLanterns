# Aurora's Lanterns Changelog

## 1.0.0

- The first release of Aurora's Lanterns!
- Added Amethyst Lanterns.
  - Amethyst Lanterns prevent the spawning of hostile mobs in a large radius (32 blocks).
  - Amethyst Lanterns close to a Zombie Villager who's being cured will fasten the recovery.
  - Various hostile mobs are scared of the Amethyst Lanterns.
- Added Redstone Lanterns.
  - Redstone Lanterns work like a Redstone Torch but don't connect the same.
- Added Wall Lanterns.
  - Any registered lantern block will have a wall variant.
  - Wall Lanterns can be placed on a full block, a wall, or a fence.
- Added a "Beware- Bonk!" advancement.

### 1.0.1

- Added better integration of Adorn's Candlelit Lanterns.
- Fixed some cases where block state IDs would not be able to sync back up.

### 1.0.2

- Improved support of oxidizing and waxable lanterns.
- Improved wall lanterns on walls that do not have the post part.

### 1.0.3

- Fixed broken support of waxable lanterns.

### 1.0.4

- Fixed runtime generation of the `#auroraslanterns:wall_lanterns` tag, which is required for proper mining of the wall lanterns.

### 1.0.5

- Fixed again the runtime generation of the `#auroraslanterns:wall_lanterns` tag, which is required for proper mining.
  - The last attempt at fixing this used a faulty injection point, the new injection point is much more reliable.

### 1.0.6

- Updated Ukrainian translations ([#1](https://github.com/LambdAurora/AurorasLanterns/pull/1)).

## 1.1.0

- Ported to Minecraft 1.21.1.
- Improved stability of Aurora's Decorations backwards compatibility.

### 1.1.1

- Improved support of oxidizing and waxable lanterns.
- Improved wall lanterns on walls that do not have the post part.
- Fixed missing recipes for the Amethyst and Redstone Lanterns.
- Fixed missing `#auroraslanterns:wall_lanterns` block tag.

### 1.1.2

- Fixed broken support of waxable lanterns.

### 1.1.3

- Fixed runtime generation of the `#auroraslanterns:wall_lanterns` tag, which is required for proper mining of the wall lanterns.

### 1.1.4

- Fixed wall lanterns not being in the pickaxe mineable tag due to a bad path.
- Updated [Yumi Minecraft Libraries: Foundation].

### 1.1.5

- Fixed again the runtime generation of the `#auroraslanterns:wall_lanterns` tag, which is required for proper mining.
  - The last attempt at fixing this used a faulty injection point, the new injection point is much more reliable.

### 1.1.6

- Updated Ukrainian translations ([#1](https://github.com/LambdAurora/AurorasLanterns/pull/1)).

### 1.1.7

- Fixed critical synchronization issues with wall lanterns.
- Updated [Yumi Minecraft Libraries: Foundation].

## 1.2.0

- Ported to Minecraft 1.21.5.

### 1.2.1

- Improved support of oxidizing and waxable lanterns.
- Improved wall lanterns on walls that do not have the post part.
- Fixed missing recipes for the Amethyst and Redstone Lanterns.
- Fixed missing `#auroraslanterns:wall_lanterns` block tag.

### 1.2.2

- Fixed broken support of waxable lanterns.

### 1.2.3

- Fixed runtime generation of the `#auroraslanterns:wall_lanterns` tag, which is required for proper mining of the wall lanterns.

### 1.2.4

- Fixed wall lanterns not being in the pickaxe mineable tag due to a bad path.
- Updated [Yumi Minecraft Libraries: Foundation].

### 1.2.5

- Fixed again the runtime generation of the `#auroraslanterns:wall_lanterns` tag, which is required for proper mining.
  - The last attempt at fixing this used a faulty injection point, the new injection point is much more reliable.

### 1.2.6

- Updated Ukrainian translations ([#1](https://github.com/LambdAurora/AurorasLanterns/pull/1)).

### 1.2.8

- Fixed critical synchronization issues with wall lanterns.
- Updated [Yumi Minecraft Libraries: Foundation].

## 1.3.0

- Ported to Minecraft 1.21.8.

### 1.3.1

- Fixed runtime generation of the `#auroraslanterns:wall_lanterns` tag, which is required for proper mining of the wall lanterns.

### 1.3.2

- Fixed wall lanterns not being in the pickaxe mineable tag due to a bad path.
- Updated [Yumi Minecraft Libraries: Foundation].

### 1.3.3

- Fixed again the runtime generation of the `#auroraslanterns:wall_lanterns` tag, which is required for proper mining.
  - The last attempt at fixing this used a faulty injection point, the new injection point is much more reliable.

### 1.3.4

- Updated Ukrainian translations ([#1](https://github.com/LambdAurora/AurorasLanterns/pull/1)).

### 1.3.5

- Fixed critical synchronization issues with wall lanterns.
- Updated [Yumi Minecraft Libraries: Foundation].

## 1.4.0

- Ported to Minecraft 1.21.10.
  - Copper Lanterns get their own wall variant, and the non-waxed will oxidize the same as regular copper lanterns.

## 2.0.0

- Ported to Minecraft 26.1.
- Added chandeliers.
  - Have iron and copper variants.
  - The copper variants can oxidized and be waxed.
  - A chandelier can have up to 4 holders.
  - Each holder can be given a candle.
  - Candle colors can be mismatched.
  - Aurora's Decorations chandeliers will be upgraded to candles due to high differences.
- Updated [Yumi Minecraft Libraries: Foundation].

### 2.0.1

- Added NeoForge support. Requires [Forgified Fabric API].
  - NeoForge support will be limited to versions that has [Forgified Fabric API] available.
- Updated Korean translations ([#8](https://github.com/LambdAurora/AurorasLanterns/pull/8)).

## 2.1.0

- Ported to Minecraft 26.2.
- Updated [Yumi Minecraft Libraries: Foundation].

### 2.1.1

- Updated Korean translations ([#8](https://github.com/LambdAurora/AurorasLanterns/pull/8)).

[Yumi Minecraft Libraries: Foundation]: https://github.com/YumiProject/yumi-minecraft-foundation-library "Yumi Minecraft Foundation Library page"
[Forgified Fabric API]: https://modrinth.com/mod/forgified-fabric-api
