package com.presencechecker;

import java.awt.Color;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Notification;
import net.runelite.client.config.Range;

@ConfigGroup("presencechecker")
public interface PresenceCheckerConfig extends Config
{
    // --- SECTIONS ---

    @ConfigSection(
            name = "General Settings",
            description = "General configuration for highlighting.",
            position = 0,
            closedByDefault = true
    )
    String generalSettings = "generalSettings";

    @ConfigSection(
            name = "Chat Filter",
            description = "Options to filter which members are shown in the missing list.",
            position = 1,
            closedByDefault = true
    )
    String chatFilter = "chatFilter";

    @ConfigSection(
            name = "Overlay Settings",
            description = "Configuration for the screen overlay HUD.",
            position = 2,
            closedByDefault = true
    )
    String overlaySettings = "overlaySettings";

    @ConfigSection(
            name = "Suspicious Activity",
            description = "Tracker for players quickly joining and leaving.",
            position = 3,
            closedByDefault = true
    )
    String suspiciousSettings = "suspiciousSettings";

    // --- HIDDEN CONFIGS ---

    @ConfigItem(
            keyName = "showPanelTutorial",
            name = "Show Tutorial",
            description = "Whether to show the help tutorial in the panel.",
            hidden = true
    )
    default boolean showPanelTutorial() { return true; }

    // --- GENERAL SETTINGS ---

    @ConfigItem(
            keyName = "highlightColor",
            name = "Highlight Color",
            description = "The color to highlight missing members in the clan chat list.",
            position = 1,
            section = generalSettings
    )
    default Color getHighlightColor()
    {
        return new Color(128, 0, 128); // Default Purple
    }

    @ConfigItem(
            keyName = "highlightDuration",
            name = "Highlight Duration",
            description = "How many seconds to keep names highlighted before letting them revert (0 to disable).",
            position = 2,
            section = generalSettings
    )
    default int highlightDuration()
    {
        return 10; // Default 10 seconds
    }

    // --- CHAT FILTER SECTION ---

    @ConfigItem(
            keyName = "filterSelf",
            name = "Exclude Self",
            description = "If enabled, do not list yourself in the missing members list.",
            position = 0,
            section = chatFilter
    )
    default boolean filterSelf()
    {
        return true;
    }

    @ConfigItem(
            keyName = "hideOwner",
            name = "Hide Owners",
            description = "Do not show missing Owners in the list.",
            position = 1,
            section = chatFilter
    )
    default boolean hideOwner() { return false; }

    @ConfigItem(
            keyName = "hideGeneral",
            name = "Hide Generals",
            description = "Do not show missing Generals in the list.",
            position = 2,
            section = chatFilter
    )
    default boolean hideGeneral() { return false; }

    @ConfigItem(
            keyName = "hideCaptain",
            name = "Hide Captains",
            description = "Do not show missing Captains in the list.",
            position = 3,
            section = chatFilter
    )
    default boolean hideCaptain() { return false; }

    @ConfigItem(
            keyName = "hideLieutenant",
            name = "Hide Lieutenants",
            description = "Do not show missing Lieutenants in the list.",
            position = 4,
            section = chatFilter
    )
    default boolean hideLieutenant() { return false; }

    @ConfigItem(
            keyName = "hideSergeant",
            name = "Hide Sergeants",
            description = "Do not show missing Sergeants in the list.",
            position = 5,
            section = chatFilter
    )
    default boolean hideSergeant() { return false; }

    @ConfigItem(
            keyName = "hideCorporal",
            name = "Hide Corporals",
            description = "Do not show missing Corporals in the list.",
            position = 6,
            section = chatFilter
    )
    default boolean hideCorporal() { return false; }

    @ConfigItem(
            keyName = "hideRecruit",
            name = "Hide Recruits",
            description = "Do not show missing Recruits in the list.",
            position = 7,
            section = chatFilter
    )
    default boolean hideRecruit() { return false; }

    @ConfigItem(
            keyName = "hideFriend",
            name = "Hide Friends",
            description = "Do not show missing Friends in the list.",
            position = 8,
            section = chatFilter
    )
    default boolean hideFriend() { return false; }

    @ConfigItem(
            keyName = "hideGuest",
            name = "Hide Guests",
            description = "Do not show missing Guests (Unranked) in the list.",
            position = 9,
            section = chatFilter
    )
    default boolean hideGuest() { return false; }

    // --- OVERLAY SETTINGS SECTION ---

    @ConfigItem(
            keyName = "enableOverlay",
            name = "Enable Overlay",
            description = "Show a HUD overlay on screen with the count of missing members.",
            position = 0,
            section = overlaySettings
    )
    default boolean enableOverlay()
    {
        return true;
    }

    @ConfigItem(
            keyName = "showOverlayNames",
            name = "Show Names in Overlay",
            description = "List the names of missing members in the overlay if the count is low.",
            position = 1,
            section = overlaySettings
    )
    default boolean showOverlayNames() { return true; }

    @ConfigItem(
            keyName = "overlayNamesLimit",
            name = "Overlay Names Limit",
            description = "Maximum number of names to show in the overlay before switching to count only.",
            position = 2,
            section = overlaySettings
    )
    default int overlayNamesLimit() { return 5; }

    // --- SUSPICIOUS ACTIVITY SECTION ---

    @ConfigItem(
            keyName = "enableSuspiciousTracking",
            name = "Enable Tracking",
            description = "Turn the suspicious activity tracking on or off.",
            position = 0,
            section = suspiciousSettings
    )
    default boolean enableSuspiciousTracking() { return true; }

    @Range(
            min = 100,
            max = 10000
    )
    @ConfigItem(
            keyName = "suspiciousThreshold",
            name = "Suspicious Time (ms)",
            description = "If a user joins and leaves within this many milliseconds, they are flagged.",
            position = 1,
            section = suspiciousSettings
    )
    default int suspiciousThreshold() { return 4000; }

    @ConfigItem(
            keyName = "susHideOwner",
            name = "Hide Owners",
            description = "Do not flag Owners as suspicious.",
            position = 2,
            section = suspiciousSettings
    )
    default boolean susHideOwner() { return true; }

    @ConfigItem(
            keyName = "susHideGeneral",
            name = "Hide Generals",
            description = "Do not flag Generals as suspicious.",
            position = 3,
            section = suspiciousSettings
    )
    default boolean susHideGeneral() { return true; }

    @ConfigItem(
            keyName = "susHideCaptain",
            name = "Hide Captains",
            description = "Do not flag Captains as suspicious.",
            position = 4,
            section = suspiciousSettings
    )
    default boolean susHideCaptain() { return true; }

    @ConfigItem(
            keyName = "susHideLieutenant",
            name = "Hide Lieutenants",
            description = "Do not flag Lieutenants as suspicious.",
            position = 5,
            section = suspiciousSettings
    )
    default boolean susHideLieutenant() { return true; }

    @ConfigItem(
            keyName = "susHideSergeant",
            name = "Hide Sergeants",
            description = "Do not flag Sergeants as suspicious.",
            position = 6,
            section = suspiciousSettings
    )
    default boolean susHideSergeant() { return true; }

    @ConfigItem(
            keyName = "susHideCorporal",
            name = "Hide Corporals",
            description = "Do not flag Corporals as suspicious.",
            position = 7,
            section = suspiciousSettings
    )
    default boolean susHideCorporal() { return false; }

    @ConfigItem(
            keyName = "susHideRecruit",
            name = "Hide Recruits",
            description = "Do not flag Recruits as suspicious.",
            position = 8,
            section = suspiciousSettings
    )
    default boolean susHideRecruit() { return false; }

    @ConfigItem(
            keyName = "susHideFriend",
            name = "Hide Friends",
            description = "Do not flag Friends as suspicious.",
            position = 9,
            section = suspiciousSettings
    )
    default boolean susHideFriend() { return false; }

    @ConfigItem(
            keyName = "susHideGuest",
            name = "Hide Guests",
            description = "Do not flag Guests (Unranked) as suspicious.",
            position = 10,
            section = suspiciousSettings
    )
    default boolean susHideGuest() { return false; }

    @ConfigItem(
            keyName = "suspiciousWarningThreshold",
            name = "Warning Threshold",
            description = "Number of times a user must be flagged before sending a chat warning (0 to disable).",
            position = 11,
            section = suspiciousSettings
    )
    default int suspiciousWarningThreshold() { return 3; }

    @ConfigItem(
            keyName = "suspiciousWarningColor",
            name = "Warning Message Color",
            description = "Color of the chat warning when a user hits the suspicious threshold.",
            position = 12,
            section = suspiciousSettings
    )
    default Color suspiciousWarningColor() { return Color.RED; }

    // --- NOTIFICATION OPTION ---
    @ConfigItem(
            keyName = "suspiciousNotification",
            name = "Suspicious Alert",
            description = "Customize the notification (Tray, Sound, Flash) for suspicious activity.",
            position = 13,
            section = suspiciousSettings
    )
    default Notification suspiciousNotification()
    {
        return Notification.OFF;
    }

    // --- WHITELIST ---
    @ConfigItem(
            keyName = "friendlyWhitelist",
            name = "Friendly Whitelist",
            description = "Enter names to NEVER flag as suspicious (comma or newline separated).",
            position = 14,
            section = suspiciousSettings
    )
    default String friendlyWhitelist()
    {
        return "";
    }

    // --- BLACKLIST ---
    @ConfigItem(
            keyName = "blacklistedNames",
            name = "Enemy Blacklist",
            description = "Enter names to INSTANTLY alert on join (comma or newline separated).",
            position = 15,
            section = suspiciousSettings
    )
    default String blacklistedNames()
    {
        return "";
    }
}