package com.presencechecker;

import com.google.inject.Provides;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.swing.SwingUtilities;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.FriendsChatManager;
import net.runelite.api.FriendsChatMember;
import net.runelite.api.FriendsChatRank;
import net.runelite.api.WorldView;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.CommandExecuted;
import net.runelite.api.events.FriendsChatMemberJoined;
import net.runelite.api.events.FriendsChatMemberLeft;
import net.runelite.api.widgets.ComponentID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.ColorUtil;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.Text;

@PluginDescriptor(
        name = "Presence Checker",
        description = "Checks which Clan Chat members are currently in the vicinity",
        tags = {"presence", "clan", "check"}
)
public class PresenceChecker extends Plugin
{
    @Inject
    private Client client;

    @Inject
    private ClientThread clientThread;

    @Inject
    private ChatMessageManager chatMessageManager;

    @Inject
    private PresenceCheckerConfig config;

    @Inject
    private ClientToolbar clientToolbar;

    @Inject
    private PresenceCheckerPanel panel;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private PresenceCheckerOverlay overlay;

    @Inject
    private ScheduledExecutorService executor;

    private NavigationButton navButton;

    // Store the list of missing members for the Overlay to access
    private volatile List<FriendsChatMember> lastMissingMembers = Collections.emptyList();
    private ScheduledFuture<?> overlayTask;

    // Timer variables
    private long highlightStartTime = 0;
    private boolean isHighlighting = false;

    // Suspicious Activity Tracking
    private final Map<String, Long> joinTimes = new HashMap<>();
    private final List<String> suspiciousUsers = new ArrayList<>();

    @Provides
    @SuppressWarnings("unused") // Used by Guice
    PresenceCheckerConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(PresenceCheckerConfig.class);
    }

    @Override
    @SuppressWarnings("unused") // Used by RuneLite
    protected void startUp()
    {
        // Register Overlay
        overlayManager.add(overlay);

        panel.setRefreshAction(this::checkPresence);
        panel.setClearSuspiciousAction(this::clearSuspiciousActivity);

        BufferedImage icon;
        try
        {
            icon = ImageUtil.loadImageResource(getClass(), "/icon.png");
        }
        catch (Exception e)
        {
            icon = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = icon.createGraphics();
            g.setColor(new Color(255, 120, 0));
            g.fillOval(2, 2, 28, 28);
            g.setColor(Color.WHITE);
            g.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 14));
            g.drawString("PC", 6, 22);
            g.dispose();
        }

        navButton = NavigationButton.builder()
                .tooltip("Presence Checker")
                .icon(icon)
                .priority(5)
                .panel(panel)
                .build();

        clientToolbar.addNavigation(navButton);

        // Schedule the background task (every 5 seconds)
        overlayTask = executor.scheduleAtFixedRate(this::backgroundScan, 5, 5, TimeUnit.SECONDS);
    }

    @Override
    protected void shutDown()
    {
        if (overlayTask != null)
        {
            overlayTask.cancel(false);
            overlayTask = null;
        }
        overlayManager.remove(overlay);
        clientToolbar.removeNavigation(navButton);
        joinTimes.clear();
        suspiciousUsers.clear();
    }

    // --- EVENT LISTENERS FOR SUSPICIOUS ACTIVITY ---

    @Subscribe
    public void onFriendsChatMemberJoined(FriendsChatMemberJoined event)
    {
        if (!config.enableSuspiciousTracking())
        {
            return;
        }

        // Don't track if the rank is ignored immediately
        if (shouldIgnoreSuspiciousRank(event.getMember().getRank()))
        {
            return;
        }

        String name = Text.standardize(event.getMember().getName());
        joinTimes.put(name, System.currentTimeMillis());
    }

    @Subscribe
    public void onFriendsChatMemberLeft(FriendsChatMemberLeft event)
    {
        if (!config.enableSuspiciousTracking())
        {
            return;
        }

        // Double check rank in case they ranked up/down (though unlikely in seconds)
        if (shouldIgnoreSuspiciousRank(event.getMember().getRank()))
        {
            return;
        }

        String name = Text.standardize(event.getMember().getName());
        Long joinTime = joinTimes.remove(name);

        if (joinTime != null)
        {
            long durationMs = System.currentTimeMillis() - joinTime;
            long thresholdMs = config.suspiciousThreshold() * 1000L;

            if (durationMs <= thresholdMs)
            {
                addSuspiciousUser(event.getMember().getName()); // Use original name for display
            }
        }
    }

    private void addSuspiciousUser(String rawName)
    {
        // Avoid duplicates in the list
        if (!suspiciousUsers.contains(rawName))
        {
            suspiciousUsers.add(rawName);
            SwingUtilities.invokeLater(() -> panel.updateSuspiciousList(suspiciousUsers));
        }
    }

    private void clearSuspiciousActivity()
    {
        suspiciousUsers.clear();
        SwingUtilities.invokeLater(() -> panel.updateSuspiciousList(suspiciousUsers));
    }

    private boolean shouldIgnoreSuspiciousRank(FriendsChatRank rank)
    {
        switch (rank)
        {
            case OWNER: return config.susHideOwner();
            case GENERAL: return config.susHideGeneral();
            case CAPTAIN: return config.susHideCaptain();
            case LIEUTENANT: return config.susHideLieutenant();
            case SERGEANT: return config.susHideSergeant();
            case CORPORAL: return config.susHideCorporal();
            case RECRUIT: return config.susHideRecruit();
            case FRIEND: return config.susHideFriend();
            case UNRANKED: return config.susHideGuest();
            default: return config.susHideGuest();
        }
    }

    // --- END SUSPICIOUS ACTIVITY LOGIC ---

    @Subscribe
    @SuppressWarnings("unused") // Used by EventBus
    public void onCommandExecuted(CommandExecuted commandExecuted)
    {
        String command = commandExecuted.getCommand();
        if (command.equalsIgnoreCase("absent"))
        {
            checkPresence();
        }
    }

    @Subscribe
    public void onClientTick(ClientTick event)
    {
        if (lastMissingMembers == null || lastMissingMembers.isEmpty())
        {
            return;
        }

        long durationMs = config.highlightDuration() * 1000L;
        long timeElapsed = System.currentTimeMillis() - highlightStartTime;

        // If time is remaining, KEEP highlighting
        if (timeElapsed < durationMs)
        {
            int highlightColor = config.getHighlightColor().getRGB() & 0xFFFFFF;
            setMemberColor(lastMissingMembers, highlightColor);
            isHighlighting = true;
        }
        // If time expired AND we were previously highlighting, REVERT to white once
        else if (isHighlighting)
        {
            setMemberColor(lastMissingMembers, 0xFFFFFF); // Revert to White
            isHighlighting = false;
        }
    }

    private void backgroundScan()
    {
        clientThread.invokeLater(() -> {
            lastMissingMembers = scanForMissingMembers();
        });
    }

    public void checkPresence()
    {
        clientThread.invokeLater(() ->
        {
            FriendsChatManager friendsChatManager = client.getFriendsChatManager();
            if (friendsChatManager == null)
            {
                String msg = "You are not currently in a Clan Chat.";
                if (config.showChatMessages())
                {
                    sendChatMessage(ColorUtil.wrapWithColorTag(msg, config.getMessageColor()));
                }
                lastMissingMembers = Collections.emptyList();
                updatePanel(new ArrayList<>());
                return;
            }

            // 1. Calculate
            List<FriendsChatMember> missingMembersList = scanForMissingMembers();

            // 2. Update Overlay data
            lastMissingMembers = missingMembersList;

            // 3. Reset timer
            highlightStartTime = System.currentTimeMillis();
            isHighlighting = true;

            // 4. Prepare Chat Output
            List<String> missingMembersChat = new ArrayList<>();
            Color msgColor = config.getMessageColor();
            for (FriendsChatMember member : missingMembersList)
            {
                String rankStr = getRankString(member.getRank());
                String nameStr = ColorUtil.wrapWithColorTag(member.getName(), msgColor);
                missingMembersChat.add(rankStr + nameStr);
            }

            // 5. Report results (Chat & Panel)
            if (missingMembersChat.isEmpty())
            {
                if (config.showChatMessages())
                {
                    String msg = "All visible Clan Chat members are currently around you.";
                    sendChatMessage(ColorUtil.wrapWithColorTag(msg, msgColor));
                }
                updatePanel(new ArrayList<>()); // Clear panel
            }
            else
            {
                if (config.showChatMessages())
                {
                    String header = ColorUtil.wrapWithColorTag("Members not in vicinity (" + missingMembersChat.size() + "): ", msgColor);
                    sendChatMessage(header + String.join(", ", missingMembersChat));
                }

                updatePanel(missingMembersList);

                // Apply color immediately (ClientTick will pick it up from here)
                int highlightColor = config.getHighlightColor().getRGB() & 0xFFFFFF;
                setMemberColor(missingMembersList, highlightColor);
            }
        });
    }

    private List<FriendsChatMember> scanForMissingMembers()
    {
        FriendsChatManager friendsChatManager = client.getFriendsChatManager();
        if (friendsChatManager == null)
        {
            return Collections.emptyList();
        }

        WorldView worldView = client.getTopLevelWorldView();
        if (worldView == null)
        {
            return Collections.emptyList();
        }

        List<String> localPlayerNames = worldView.players().stream()
                .map(p -> Text.standardize(p.getName()))
                .collect(Collectors.toList());

        String localName = client.getLocalPlayer() != null ? Text.standardize(client.getLocalPlayer().getName()) : "";
        List<FriendsChatMember> missing = new ArrayList<>();

        for (FriendsChatMember member : friendsChatManager.getMembers())
        {
            String ccMemberName = Text.standardize(member.getName());

            if (config.filterSelf() && ccMemberName.equals(localName))
            {
                continue;
            }

            if (shouldHideRank(member.getRank()))
            {
                continue;
            }

            if (!localPlayerNames.contains(ccMemberName))
            {
                missing.add(member);
            }
        }
        return missing;
    }

    @SuppressWarnings("unused")
    public int getMissingMembersCount()
    {
        return lastMissingMembers.size();
    }

    @SuppressWarnings("unused")
    public List<FriendsChatMember> getMissingMembers()
    {
        return lastMissingMembers;
    }

    // Refactored method to apply ANY color (Highlight or White)
    @SuppressWarnings("deprecation")
    private void setMemberColor(List<FriendsChatMember> members, int color)
    {
        Widget list = client.getWidget(ComponentID.FRIENDS_CHAT_LIST);

        // Visibility check to save resources
        if (list == null || list.getDynamicChildren() == null || list.isHidden())
        {
            return;
        }

        Set<String> targetNames = members.stream()
                .map(m -> Text.standardize(m.getName()))
                .collect(Collectors.toSet());

        for (Widget child : list.getDynamicChildren())
        {
            String rawText = child.getText();
            String name = Text.standardize(Text.removeTags(rawText));

            if (targetNames.contains(name))
            {
                child.setTextColor(color);
            }
        }
    }

    private boolean shouldHideRank(FriendsChatRank rank)
    {
        switch (rank)
        {
            case OWNER: return config.hideOwner();
            case GENERAL: return config.hideGeneral();
            case CAPTAIN: return config.hideCaptain();
            case LIEUTENANT: return config.hideLieutenant();
            case SERGEANT: return config.hideSergeant();
            case CORPORAL: return config.hideCorporal();
            case RECRUIT: return config.hideRecruit();
            case FRIEND: return config.hideFriend();
            case UNRANKED: return config.hideGuest();
            default: return config.hideGuest();
        }
    }

    private void updatePanel(List<FriendsChatMember> missingMembers)
    {
        SwingUtilities.invokeLater(() -> panel.updateMissingList(missingMembers));
    }

    private void sendChatMessage(String message)
    {
        chatMessageManager.queue(
                QueuedMessage.builder()
                        .type(ChatMessageType.CONSOLE)
                        .runeLiteFormattedMessage(message)
                        .build());
    }

    private String getRankString(FriendsChatRank rank)
    {
        switch (rank)
        {
            case OWNER: return "<col=ffff00>[Owner]</col> ";
            case GENERAL: return "<col=ffca00>[Gen]</col> ";
            case CAPTAIN: return "<col=ff9b00>[Capt]</col> ";
            case LIEUTENANT: return "<col=ff6f00>[Lt]</col> ";
            case SERGEANT: return "<col=ff4000>[Sgt]</col> ";
            case CORPORAL: return "<col=ff1500>[Corp]</col> ";
            case RECRUIT: return "<col=880000>[Rec]</col> ";
            case FRIEND: return "<col=004400>[Friend]</col> ";
            default: return "";
        }
    }
}