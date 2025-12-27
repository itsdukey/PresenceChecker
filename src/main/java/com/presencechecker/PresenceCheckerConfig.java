package com.presencechecker;

import java.awt.Color;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup("presencechecker")
public interface PresenceCheckerConfig extends Config
{
    // --- SECTIONS ---

    @ConfigSection(
            name = "General Settings",
            description = "General configuration for messages and colors.",
            position = 0
    )
    String generalSettings = "generalSettings";

    @ConfigSection(
            name = "Chat Filter",
            description = "Options to filter which members are shown in the missing list.",
            position = 1
    )
    String chatFilter = "chatFilter";

    @ConfigSection(
            name = "Overlay Settings",
            description = "Configuration for the screen overlay HUD.",
            position = 2
    )
    String overlaySettings = "overlaySettings";

    // --- GENERAL SETTINGS ---

    @ConfigItem(
            keyName = "messageColor",
            name = "Message Color",
            description = "The color of the chat message when checking for members.",
            position = 1,
            section = generalSettings
    )
    default Color getMessageColor()
    {
        return Color.RED;
    }

    @ConfigItem(
            keyName = "showChatMessages",
            name = "Show Chat Messages",
            description = "If disabled, the missing members list will only appear in the side panel, not in the chat.",
            position = 2,
            section = generalSettings
    )
    default boolean showChatMessages()
    {
        return false;
    }

    @ConfigItem(
            keyName = "highlightColor",
            name = "Highlight Color",
            description = "The color to highlight missing members in the clan chat list.",
            position = 3,
            section = generalSettings
    )
    default Color getHighlightColor()
    {
        return new Color(128, 0, 128); // Default Purple
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
}