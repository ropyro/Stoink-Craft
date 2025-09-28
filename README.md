# 💸 StoinkCore

> A Minecraft economy plugin powering the StoinkCraft server — where players form enterprises, grind for profits, and compete to become the richest in a player-driven marketplace.

---

## 🏗️ Features

- 🏢 **Enterprise System**
    - Players can form or join enterprises (companies)
    - Role hierarchy: `CEO`, `CFO`, `COO`, and `Employee`
    - Custom wage splits and role-based power

- 💸 **Dynamic Economy**
    - Market prices for items adjust weekly
    - Players earn based on current market value
    - Profits go to both the player and the enterprise

- 🧠 **Player-Driven Growth**
    - CEOs and CFOs manage company funds and shares
    - Employees can be promoted, demoted, or fired
    - Enterprises can be overthrown if leadership is inactive

- 📊 **Enterprise Net Worth**
    - Leaderboards based on bank balance + asset value
    - Share-based investment and risk mechanics

- ⚖️ **Anti-Inflation Mechanics (WIP)**
    - Crate key sinks and quarterly economy resets
    - No map resets = long-term player investment

---

## 🛠️ Tech Stack

- **Language:** Java 17
- **Platform:** Spigot/Paper API (1.20+)
- **Build System:** Maven
- **Dependencies:**
    - [Vault](https://github.com/MilkBowl/Vault) (Economy API)
    - EssentialsX (runtime, not required at compile)