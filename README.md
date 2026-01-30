# Presence Checker

**Presence Checker** is a RuneLite plugin designed for Old School RuneScape to help clan leaders and event organizers track attendance and detect potential spies or scouts.

It actively monitors your **Friends Chat** or **Clan Chat** to identify which members are currently logged in but **not** in your immediate vicinity (render distance). It also features a passive "Suspicious Activity" tracker to alert you of players hopping in and out of your channel.

## Key Features

###  Attendance Tracking (Missing Members)
* **Proximity Check:** Compares the list of people in your chat channel against the players visible in your game world.
* **Live Updates:** Displays a list of "Missing Members" in the side panel and a HUD overlay on your screen.
* **Chat Highlighting:** Automatically highlights the names of missing members in purple (configurable) in the in-game chat box, making it easy to see who hasn't arrived yet.

###  Anti-Scout Detection
* **Passive Monitoring:** Automatically detects players who join your chat and leave quickly (default: under 4 seconds).
* **Suspicion Counter:** Tracks how many times a specific player has triggered the scout detection.
* **Alerts:** Sends a chat message and desktop notification when a player hits your "Warning Threshold."
* **Blacklist:** Add specific names to an "Enemy Blacklist" to receive an **instant** alert the moment they join your chat.

###  Advanced Filtering (New!)
* **Dual-Mode Support:** Fully supports both the classic **Friends Chat**  and the modern **Clan Chat** .
* **Clan Rank Filters:** Includes **24 Customizable Filter Slots** allowing you to hide specific ranks from the missing list.
    * *Supports all OSRS Clan Titles:* Military, Gems, Metals, Monsters, and more.
* **Friends Chat Filters:** Simple toggle switches to hide specific ranks (e.g., "Hide Captains").

## How to Use

### 1. Select Your Target
Go to **Config > General Settings** and select your **Target Chat**:
* **Friends Chat:** Monitors the classic friends chat.
* **Clan Chat:** Monitors the official clan channel.
* **Guest Clan Chat:** Monitors the guest clan-channel.

### 2. Check Attendance
* Open the **Presence Checker** side panel.
* Click **Refresh** or type `::absent` in-game.
* The panel will list everyone who is in the chat but **not** standing near you.

### 3. Configure Filters
To prevent clutter (e.g., you don't care if "Recruits" are missing), configure the filters:
* **For Clan Chat:** Go to `Clan Chat Filters` in settings. Use the dropdown menus to select up to 24 different ranks to ignore.
    * *Tip:* You can type in the dropdown to jump to a rank (e.g., type "G" to jump to General).
* **For Friends Chat:** Go to `Friends Chat Filters` and toggle the ranks you want to hide.

## Configuration Options

| Setting | Description |
| :--- | :--- |
| **Target Chat** | Choose between Friends Chat, Clan Chat, or Guest Clan. |
| **Highlight Color** | Color of the name highlight in the chat box for missing members. |
| **Suspicious Time** | Time (in ms) a player must stay to be considered "safe". If they leave faster than this, they are flagged as a scout. |
| **Warning Threshold** | How many times a player must be flagged before you get a notification. |
| **Friendly Whitelist** | Comma-separated list of names to NEVER flag as suspicious. |
| **Enemy Blacklist** | Comma-separated list of names to ALWAYS alert on join. |

## Commands
* `::absent` - Manually triggers a presence check and refreshes the overlay/panel.

## Support
If you encounter any bugs or have feature requests, please open an issue on the GitHub repository.
