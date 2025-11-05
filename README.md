# 💸 StoinkCore

> A Minecraft economy plugin powering the StoinkCraft server — where players form enterprises, grind for profits, and compete to become the richest in a player-driven marketplace.

---

## 🏗️ Features

- 🏢 **Enterprise System**
    - Players can form or join enterprises (companies)
    - Role hierarchy: `CEO` and `Employee`
    - Custom wage splits and role-based power
- 💸 **Contract Economy**
    - New contracts release daily
    - Players earn based on completion of contracts
    - Profits go to both the player and the enterprise
- 🧠 **Player-Driven Growth**
    - CEOs manage company funds and shares
    - Employees can be promoted, demoted, or fired
    - Enterprises can be overthrown if leadership is inactive
- 📊 **Enterprise Net Worth & Stonks**
    - Leaderboards based on bank balance + asset value
    - Share-based investment and risk mechanics
    - Enterprise bank is taxed if funds are not invested daily
- ⚙️ **Custom Job Sites**
  - Skyrise Building, Quarry, Farmland, Graveyard
  - Each site offers renewable resources to complete contracts
  - Sites are upgradeable to increase effeciency 


---

## 🛠️ Tech Stack

- **Language:** Java 17
- **Platform:** Spigot (1.21.8)
- **Build System:** Maven
- **Dependencies:**
    - [Vault](https://github.com/MilkBowl/Vault) (Economy API)
    - FastAyncWorldEdit
    - WorldGuard
    - DecentHolograms
    - PlaceHolderAPI
    - Citizens