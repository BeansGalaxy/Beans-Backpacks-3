# Changelog

<h2>0.11 Fabric <br> 0.11 NeoForge</h2>

- **Tooltip Menus**
  - Overhauled controls to insert/remove items
  - Right-Clicking opens a tooltip where the mouse is used to move items
  - In Lunch Boxes, Alchemy Bags, and Quivers; MWheel Scrolling selects an item to use
  - Overhauled hidden `chest` trait to work more simmilar to `bundle` traits
- **Placing Backpacks**
  - Hanging snaps to where 4 corners are supported
  - Placing upright moves to be fully supported on the block
  - Failure to place due to space happens ***much*** less often
- **Selecting Items**
  - Middle Mouse on a item in the inventory pulls matching items from Backpack to the cursor
  - Quivers anywhere in the inventory can be used by Bows; Quivers are no longer worn on the Back
  - Lunch Boxes & Alchemy Bags show the selected item when hovered or held
  - Scrolling to select an item loops once scrolled to the end of the list
- **Traits & Components**
  - Removed `beansbackpacks:equipable` component
  - Removed `beansbackpacks:placeable` component
  - Added `beansbackpacks:backpack` trait
    - Combination of `bundle`, `placeable`, & `equipable` components
  - Added `parent` field to Reference Traits
    - Takes an existing `trait_id` for the current Reference to overlay ontop of
  - Added `beansbackpacks:filter` which whitelists item's allowed onto the storage
    - Works in a Reference too
- **Bug Fixes**
  - Looking away from a block while trying to place right-clicks that block and cancels the placement
  - When opening the Smithing Screen, the Armor Stand started the wrong way
  - The eating sounds from the Lunch Box depended on the first item, not the selected item
  - Alchemy Bag particles now take the player's momentum when used
- **Miscellaneous**
  - Changed `keep_backpack_on_death` config from true/false for clarity
    - `Always`: always keeps the backpack
    - `Never`: always drops the backpack
    - `KeepInventory` (*default*): keeps/drops the backpack according the KeepInventory gamerule
  - Removed ticking methods which traits no longer used
 

<h2>0.10 Fabric <br> 0.10 NeoForge</h2><img width="204" height="92" align="right" alt="crafting-grid(1)" src="https://github.com/user-attachments/assets/9144fd7c-36b1-451a-ab49-538c6e0595ff" />

- **Burlap Sack Block**
  - Backpack-like storage block
  - Holds 16 Stacks
  - Crafted with Stripped Bamboo + String
- **Backpack Balancing**
  - Iron & Gold Backpacks now hold 9 stacks (previously 8)
  - Netherite Backpacks now hold 16 stacks (previously 12)
- **Config Features**
  - Completely removed "Features" (too much bulk)
- **Miscellaneous** 
  - Added "beansbackpacks:display" Item Component to redirect an item's model
  - Added "display" field in "beansbackpacks:references"
  - Fixed Leather Armor not rendering with any color

## 0.9.1 Fabric <br> 0.9.1 NeoForge

- **Major Shorthand Changes**
  - The Shorthand has been seperated to the stand-alone mod **["Beans' Toolbelt"](https://modrinth.com/mod/beans-tool-belt)**
  - Opening your world will send a warning message to install Beans' Toolbelt if any save data is found.
  - Beans' Backpacks will hold onto the Shorthand's data until Beans' Toolbelt is installed.
- **Controls**
  - You must now hold [Sprint] + [Use] about 0.5s to place a backpack
  - Using a empty backpack in your hand while wearing a filled one will not longer swap backpacks
  - Quickly moving items into the Backpack with CTRL will no longer move it to the backpack in your hands
- **Miscellaneous**
  - Removed all Experimental Features
  - Subtle re-texture of the Leather Backpack item to appear a simmilar color to when placed


## 0.8 Fabric <br> 0.8 NeoForge

- **Major Shorthand & Tool Belt Changes**
  - The Shorthand & the Tool Belt have been merged into the Shorthand
  - Equipping a Tool in any Shorthand Slot works like the Tool Belt
  - Removed the Secondary Shorthand Key
  - Pressing the Shorthand Key now activates the Shorthand
  - While active, scroll the mouse to select a different tool
  - Disabled Rendering of the Shorthand on the Player
  - The Shorthand can be extended through meeting specific advancements
    - Entering the End
    - Reaching Level 30
  - [Read the updated documentation](https://github.com/BeansGalaxy/Beans-Backpacks-3/wiki/Config-Options#extend_shorthand_by_advancement)
- **Config**
  - Added Client Config Option `shorthand_control` *([more](https://github.com/BeansGalaxy/Beans-Backpacks-3/wiki/Config-Options#shorthand_control))*
  - Added Common Config Option `extend_shorthand_by_advancement` *([more](https://github.com/BeansGalaxy/Beans-Backpacks-3/wiki/Config-Options#extend_shorthand_by_advancement))*
  - Removed `tool_belt_hud_visibility`
  - Removed `disable_shorthand_render`
  - Removed `tool_belt_additions`
  - Removed `allow_shorthand_weapons`
  - Removed `keep_tool_belt_on_death`
  - Renamed `tool_belt_break_items` to `shorthand_breaks_tool`
  - `shorthand_breaks_tool` is now modifiable on the Client
- **Miscellaneous**
  - Vanilla Conduit Power's mining speed increase now stacks with Haste
  - Updated the textures for Iron & Gold Backpacks
- **Bugs**
  - Right Clicking while using the Shorthand's auto equip now unequips the tool correctly to use items on your first click
  - The Creative Mode Screen now correctly clears the Shorthand Slots and no longer creates ghost items
  - If the player is loaded on a server with an invalid selected slot, that slot gets clamped

## 0.7 Fabric <br> 0.7 NeoForge

- **Major Changes**
  - The Feature "Backpacks Expirments" are no longer enabled by default
    - Disables Expirmental Items; Copper Leggings, Vial o' Experience, Bulk Bundle
- **More Utilities**
  - Compasses, including Respawn & Lodestone linked, display a hud element
  - Clocks display a hud element
  - Conduits
    - Grants *Conduit Power I* while in water or while raining
    - Equip 2 Conduits for *Conduit Power II*
  - Ominous Banners
    - Grants *Bad Omen I* for 5 minutes
    - Condumed when entering Villages or Trial Chambers
  - Each Utility has a unique model when equipped
- **Overhauled Backpack Menus**
  - Players can access their entire inventory while in the backpack screen
  - Shift, Ctrl, Q to Drop all work with these screens
  - Use Shift on Bundles in the Inventory to move items to the Backpack
- **Miscellaneous**
  - The "Copper Backpacks" Feature's resources have been seperated from the base resources

## 0.6a NeoForge

- **Hot Fix**
  - The `beansbackpacks:battery` Trait was missing client methods which caused the game to crash while trying to render item decorations

## 0.6 Fabric <br> 0.6 NeoForge

- **Added Utility Slots to Backpacks**
  - Leather, Iron & Gold Backpacks add 1 Utility Slot
  - Netherite Backpacks add 2
  - Utility Slots activate unique upgrades while the desired item is equipped
    - Rockets: While flying with an elytra, pressing jump will give you a boost
    - Totems: Upon death, the totem will save you
    - Spyglass: Pressing `B` will zoom in
    - More to come! [Check out this](https://github.com/BeansGalaxy/Beans-Backpacks-3/issues/19) to leave any suggestions
- **Config**
  - The Back Slot is now movable in the Client Config
  - Added the Copper Backpacks Feature to the Features page
- **Miscellaneous**
  - Tweaked the vanilla spyglass overlay
  - Fixed a bug causing Minecraft to crash while opening the creative inventory with trinkets installed

## 0.5 Fabric <br> 0.5 NeoForge

- **Shorthand**
  - Weapons are no longer allowed to be equiped in the shorthand
  - If a tool is not found in the toolbelt, the shorthand is searched and equipped
  - Combined the shorthand and toolbelt slots to the right side of the inventory
  - The Shorthand's max size has been reduced from 8 to 4
  - The Tool Belt's max size has been reduced from 8 to 5
  - Read more about the changes to the Shorthand [here](https://github.com/BeansGalaxy/Beans-Backpacks-3/issues/18) <---
- **Config**
  - `allow_shorthand_weapons`: allows for weapons to be equiped in the shorthand agian
  - `disable_equipable_render`: disables equiped backpacks from rendering
  - `disable_shorthand_render`: disables the shorthand from rendering
  - `do_nbt_stacking`: allows simmilar items with different nbt to stack
- **NBT Stacking**
  - Items with simmilar nbt such as renamed items or bee hives can now be stack
  - Items can only stack this way by the player in the inventory or on pick-up
  - Disabled by default. Set `do_nbt_stacking` to `true` in the common config
- **Bugs**
  - Fixed crash with [Scorchful](https://modrinth.com/mod/scorchful) mod (thanks for the help [TheDeathlyCow](https://modrinth.com/user/TheDeathlyCow)!)

## 0.4 Fabric <br> 0.4 NeoForge

- **Major Changes**
  - Hidden many "half-baked" items under the new Feature ["**Backpack Experiments**"](https://github.com/BeansGalaxy/Beans-Backpacks-3/wiki/Config-Options/_edit#backpack-experiments). Enabled by default but this will change soon.
- **Config**
  - All config now saves to sub-directory `.../config/beansbackpacks/`
  - Added Client option `tool_belt_hud_visibility` ([*read more*](https://github.com/BeansGalaxy/Beans-Backpacks-3/wiki/Config-Options/_edit#backpack-experiments))
  - Added new config page ["**Features**"](https://github.com/BeansGalaxy/Beans-Backpacks-3/wiki/Config-Options/_edit#backpack-experiments)
- **Fixes**
  - Adjusting the sizes of the Tool Belt and Shorthand now update correctly on load
  - Config on the Server was not loading
  - Adjusted how Allay hold Backpacks

## 0.3 Fabric <br> 0.3 NeoForge

- **Added Traits Config Page**
  - Check out [the wiki](https://github.com/BeansGalaxy/Beans-Backpacks-3/wiki/Config-Options#trait-registry) for help!
- **Item Rendering**
  - Holding the Lunch Box and Alchemy Bag in your hand now renders at the selected item
  - The Quiver shows the selected arrow at a glance
  - Simplified Quiver texture for readability
  - The Fullness Bar's color has been changed to gold to match the item's tooltip
  - The Fullness Bar now renders under the Damage Bar
- **Backpack Entities**
  - Backpacks wobble once again when placed onto the ground
  - Backpacks could be picked up through solid blocks
  - Using the hotbar keys on an empty Bundle Menu Slot would crash the game
- **Creative Mode**
  - Fixed the Backpack from being unusable once an item was stored
  - Fixed the Equipment equip sound from being played when switching gamemodes
- **Miscellaneous**
  - Shorthand/Toolbar HUD elements render on the same plane as the Hotbar
  - Config Pages' order is no longer shuffled each load

## 0.2 Fabric <br> 0.2 NeoForge

- **Backpack Appearance**
  - Player capes no longer clip with Backpacks
  - Adjusted the position of the Backpack while an Elytra is equipped
  - Added Client Config option elytra_model_equipment that registers items while equipped in the Chestplate alter the postition to the Elytra position
  - Tweaked Leather Backpack Entity's inner texture
- **Chester Mob**
  - Allay can be equipped with a Backpack by pressing CTRL + Right Click
  - While equipped the Allay follows the Player and stays within interaction range
  - Right Click the Allay to open the Backpack's Menu
  - Allay's will teleport to the Player while too far
  - When the Player disconnects or changes dimentions, the Allay will not move
- **Miscellaneous**
  - Fabric can now play in 1.21.1
  - Swapping the Offhand with the Shorthand active created ghost items
  - The `beansbackpacks:equipable` Component takes an optional unequip sound

## 0.1 Fabric <br> 0.1 NeoForge

- **Items**
  - Added Bulk Pouch (Unfinished)
  - Thrown Tridents return to the Shorthand Slot
- **Config**
  - Added seperate Keep Inventory Rules for the Back Slot, Tool Belt, and Shorthand
- **Compatibility**
  - Mod Menu
  - Better Combat

## 0.0 Fabric <br> 0.0 NeoForge

- **Inventories**
  - Reloading the world remembers what slots you have selected in the inventory
  - Backpacks will now display in the Smithing Screen
- **Beans Backpacks 2**
  - Entities will now be ported and converted

**Welcome to Beta!** <br>
If you have any questions about the mod so far, check out the wiki or leave them on the issue tracker

## 0.0.6 Fabric <br> 0.0.6 NeoForge

- **Entity Interactions**
  - A Backpack worn on a player can be right-clicked and opened
  - Armor Stands can equip Backpacks by using CTRL + Right Click
  - Backpacks on Armor Stands can also be opened
- **Items**
  - The Quiver's 3D Model now has a proper texture
  - Netherite Variants can now be crafted in Smithing Tables
- **Beans Backpacks 2**
  - Items will now be ported correctly when updating your world
  - Placed Backpacks will still be removed, WIP

## 0.0.5 Fabric <br> 0.0.5 NeoForge

- **Backpack Placement**
  - New keybind to instantly place backpacks without right-click
  - You can place backpacks through non-solid blocks
  - Holding CTRL now searches thorugh non-solid blocks when attempting to pickup a backpack
- **HUD Elements**
  - Shorthand is now next to the hotbar, oppisite the Offhand
  - The suggested tool from the Tool Belt now appears in the corner of the screen
  - New Config option to swap the Shorthand and Tool Belt's HUD position
- **Other Changes**
  - Cleaned up Bucket Traits right-click to be more predictable when crouching
  - Tool Bar no longer selects tools with 1 durability. This can be reverted with the config setting `tool_belt_break_items`: true

## 0.0.4 Fabric <br> 0.0.4 NeoForge

- **Implemented Config**
  - Adjust Shorthand & Tool Belt size
  - Modify Shorthand & Tool Belt item whitelists
- **Back Slot**
  - Moved above the Offhand Slot
  - New Icon
- **Tool Slots**
  - Renamed to Tool Belt
  - New Icons
- **Weapon Slot**
  - Renamed to Shorthand
  - New Icons
  - HUD element now is transparent
- **Backpacks**
  - Entities display their trims
  - Opening animation
  - Non-solid blocks will be ignored when being placed

## 0.0.3 Fabric <br> 0.0.3 NeoForge

- Implemented Tool Slots
  - Holds pickaxes, shovels, axes, hoes, shears
  - When attacking a block the most effective tool will be used
  - By default the player has 2
  - Modifible with the `beansbackpacks:player_tool_slots` attribute
- Implemented Utility Slots
  - Holds tools, swords, bows and crossbows
  - Utility is equipped with a keybind
  - Unequippable by pressing the key agian or MWheel Scroll
  - By default the player has 1
  - Modifible with the `beansbackpacks:player_weapon_slots` attribute
  - Stored Utility appears on the player's HUD
- Traits no longer store their reference location
- Organized packages and imports

## 0.0.2 Fabric <br> 0.0.2 NeoForge

- Sizes for each Trait have been capped.
- Backpack Entities now inherit it's sound from it's `placeable` component
- Simplified `equipable` component
- Added `no_gui` item model predicate
- Simplified `quiver` trait_id and improved Quiver Item's Model
- Ender Traits now work more reliably with trait specific interactions
- Equipable Components now have an optional field `sound_event` to play on equip
- Ported to NeoForge

## 0.0.1

- Data such as Items and Amounts are now stored as seperate components from static values like a backpack's size.
- Right clicking placed backpacks now have interactions and guis to access items
- The Chest Trait is fully fleshed out and can be used in any inventory by CTRL + Click
- Placed Backpacks now react to Hoppers and update Comparators

## 0.0.0

Overhauls Minecraft Inventory by building on the Bundle's mechanics to increase functionality and ease of use. Includes specified bundle mecanics such as a reliable Lunch Box to the unique Alchemy Bag.

This version is a work in progress and only released to begin hearing feedback. DO NOT begin new worlds with this version due to the volitaile state of most of the code.

This version is also not compatible with Beans Backpacks 2.0 and should not be updated from that mod.
