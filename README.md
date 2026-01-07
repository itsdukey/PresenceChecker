# Presence Checker

**Presence Checker** is a RuneLite plugin designed for Old School RuneScape Clan Chat leaders, event organizers, and PK teams. It allows you to instantly "take attendance" by identifying which Clan Chat members are currently logged in but are **not** within your immediate vicinity (render distance).

it includes a **Suspicious Activity Tracker** to monitor for potential "scouts" or players hopping in and out of your channel rapidly.

![Plugin Icon](src/main/resources/iconfull.png)

## Features

### 🔍 Missing Members (Attendance)
* **Instant Comparison:** Compares your current Clan Chat list against the players physically visible on your screen.
* **Visual Indicators:**
    * **Side Panel:** Lists all missing members with their rank (e.g., `[Gen] PlayerName`).
    * **In-Game Overlay:** Displays a HUD with the count of missing members and a list of names (if the count is low).
    * **Widget Highlighting:** Changes the text color of missing members directly in the native Clan Chat list (default: Purple) for a set duration.
* **Clipboard Export:** "Copy All" button to easily paste the missing list into Discord or spreadsheets.

### ⚠️ Suspicious Activity Tracker (New!)
* **Anti-Scout Monitoring:** Automatically detects players who join the Clan Chat and leave shortly after.
* **Configurable Threshold:** Define what counts as "suspicious" by setting a time limit (1–10 seconds).
* **Dedicated UI:** Appears at the top of the side panel for immediate visibility.
* **History:** Keeps a running list of suspicious names until you manually clear them.

## Usage

### The Side Panel
The plugin panel is divided into two sections:
1.  **Suspicious Activity (Top):** Shows names of players who joined and left rapidly.
    * *Copy Names:* Copies the list to your clipboard.
    * *Clear Names:* Resets the list.
2.  **Missing Members (Bottom):** Shows members logged in but not nearby.
    * *Refresh:* Manually triggers a new scan.
    * *Copy All:* Copies the list to your clipboard.
    * *Clear Names:* Clears the current visual list.

### Chat Commands
* `::absent` - Manually triggers a presence check. If enabled in config, this will also output the missing members list to your local chat box.

## Configuration

The plugin is highly configurable via the RuneLite settings menu:

### General Settings
* **Message Color:** Color of the chat message output.
* **Show Chat Messages:** Toggle whether missing members are printed to the game chat box.
* **Highlight Color:** The color used to highlight missing members in the native Clan Chat interface.
* **Highlight Duration:** How long (in seconds) the native interface highlight lasts.

### Chat Filter
* **Exclude Self:** Don't list your own player as missing.
* **Rank Filters:** Toggles to hide specific ranks (Owner, General, Captain, Lieutenant, Sergeant, Corporal, Recruit, Friend, Guest).

### Overlay Settings
* **Enable Overlay:** Toggle the on-screen HUD.
* **Show Names in Overlay:** Show specific names if the missing count is low.
* **Overlay Names Limit:** Maximum number of names to show on the overlay before switching to a simple count.

### Suspicious Activity
* **Enable Tracking:** Toggle the join/leave monitoring feature on or off.
* **Suspicious Time (Sec):** The time window (1-10 seconds) for a user to join and leave to be flagged as suspicious.
