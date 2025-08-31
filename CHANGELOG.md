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
