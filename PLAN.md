# Admin Items GUI Implementation Plan

## Overview
Create a simple admin GUI to spawn all custom StoinkCraft items for testing and administration purposes.

## Items to Include (from StoinkItemRegistry)
1. **Boosters** (3 tiers)
   - Small Booster (1.5x, 30s)
   - Medium Booster (2.0x, 45s)
   - Large Booster (3.0x, 60s)

2. **Mine Bombs** (3 tiers)
   - Small Mine Bomb (3 radius)
   - Medium Mine Bomb (5 radius)
   - Large Mine Bomb (7 radius)

3. **Farmland Items**
   - Fertilizer Bomb

4. **Graveyard Items**
   - Soul Voucher (configurable amount)
   - Graveyard Hound Spawn Egg

## Implementation Steps

### 1. Create AdminItemsGUI Class
**File:** `src/main/java/com/stoinkcraft/items/admin/AdminItemsGUI.java`

- Use InvUI library (same pattern as EnterpriseGUI)
- 3-row GUI (27 slots) displaying all items
- Each item clickable to receive 1 item (left-click) or stack of 64 (shift-click)
- Admin permission check: `stoinkcraft.admin`
- Layout:
  ```
  # # # # # # # # #
  # B b B M m M F #
  # S H # # # # # #
  ```
  Where: B=boosters, M=mine bombs, F=fertilizer, S=soul voucher, H=hound

### 2. Create AdminItemsCMD Command
**File:** `src/main/java/com/stoinkcraft/items/admin/AdminItemsCMD.java`

- Command: `/stoinkitems` or `/sitems`
- Permission: `stoinkcraft.admin`
- Opens the AdminItemsGUI for the player

### 3. Register Command
**File:** `src/main/java/com/stoinkcraft/StoinkCore.java` (or plugin.yml)

- Register the command in onEnable()
- Add to plugin.yml with permission

## Technical Details

### GUI Structure
- Use `Gui.normal()` with structure pattern
- SimpleItem for filler slots
- AbstractItem for each custom item with click handling
- Click handlers:
  - Left-click: Give 1 item
  - Shift-click: Give 64 items
  - For Soul Voucher: Show amount selector or give with default amount (100 souls)

### Special Handling
- Soul Voucher needs amount parameter - use default of 100 souls, or add sub-menu for custom amounts
- Items given directly to player inventory with overflow dropping at feet

## Files to Create
1. `src/main/java/com/stoinkcraft/items/admin/AdminItemsGUI.java`
2. `src/main/java/com/stoinkcraft/items/admin/AdminItemsCMD.java`

## Files to Modify
1. `src/main/java/com/stoinkcraft/StoinkCore.java` - Register command
2. `src/main/resources/plugin.yml` - Add command definition
