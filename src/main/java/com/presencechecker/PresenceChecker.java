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
import net.runelite.client.Notifier;
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
        description = "Checks which Friends Chat members are currently in the vicinity",
        tags = {"presence", "clan", "check","pvp","scout","fc","friends chat",}
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

    @Inject
    private Notifier notifier;

    private NavigationButton navButton;
    private volatile List<FriendsChatMember> lastMissingMembers = Collections.emptyList();
    private ScheduledFuture<?> overlayTask;
    private long highlightStartTime = 0;
    private boolean isHighlighting = false;
    private final Map<String, Long> joinTimes = new HashMap<>();
    private final List<String> suspiciousDisplayList = new ArrayList<>();
    private final Map<String, Integer> suspiciousCounts = new HashMap<>();

    @Provides
    @SuppressWarnings("unused")
    PresenceCheckerConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(PresenceCheckerConfig.class);
    }

    @Override
    @SuppressWarnings("unused")
    protected void startUp()
    {
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
        suspiciousDisplayList.clear();
        suspiciousCounts.clear();
    }

    @Subscribe
    public void onFriendsChatMemberJoined(FriendsChatMemberJoined event)
    {
        if (!config.enableSuspiciousTracking()) return;

        String name = Text.standardize(event.getMember().getName());

        // Check Blacklist
        if (isBlacklisted(name))
        {
            String msg = "BLACKLISTED PLAYER DETECTED: " + event.getMember().getName();
            sendChatMessage(ColorUtil.wrapWithColorTag(msg, Color.RED));
            notifier.notify("BLACKLISTED PLAYER: " + event.getMember().getName());
            addSuspiciousUser(event.getMember().getName(), 0);
            return;
        }

        if (shouldIgnoreSuspicious(event.getMember())) return;
        joinTimes.put(name, System.currentTimeMillis());
    }

    @Subscribe
    public void onFriendsChatMemberLeft(FriendsChatMemberLeft event)
    {
        if (!config.enableSuspiciousTracking()) return;
        if (shouldIgnoreSuspicious(event.getMember())) return;

        String name = Text.standardize(event.getMember().getName());
        Long joinTime = joinTimes.remove(name);

        if (joinTime != null)
        {
            long durationMs = System.currentTimeMillis() - joinTime;
            long thresholdMs = config.suspiciousThreshold();
            if (durationMs <= thresholdMs)
            {
                addSuspiciousUser(event.getMember().getName(), durationMs);
            }
        }
    }

    private void addSuspiciousUser(String rawName, long durationMs)
    {
        String displayText = rawName + " (" + durationMs + "ms)";
        suspiciousDisplayList.add(displayText);
        SwingUtilities.invokeLater(() -> panel.updateSuspiciousList(suspiciousDisplayList));

        String standardName = Text.standardize(rawName);
        int count = suspiciousCounts.getOrDefault(standardName, 0) + 1;
        suspiciousCounts.put(standardName, count);

        int threshold = config.suspiciousWarningThreshold();
        if (threshold > 0 && count >= threshold)
        {
            String msg = "WARNING: " + rawName + " Has been flagged Suspicious";
            sendChatMessage(ColorUtil.wrapWithColorTag(msg, config.suspiciousWarningColor()));
            notifier.notify(config.suspiciousNotification(), "Suspicious Activity Detected: " + rawName);
        }
    }

    private void clearSuspiciousActivity()
    {
        suspiciousDisplayList.clear();
        suspiciousCounts.clear();
        SwingUtilities.invokeLater(() -> panel.updateSuspiciousList(suspiciousDisplayList));
    }

    private boolean isBlacklisted(String name)
    {
        Set<String> blacklist = Text.fromCSV(config.blacklistedNames()).stream()
                .map(Text::standardize)
                .collect(Collectors.toSet());
        return blacklist.contains(name);
    }

    private boolean shouldIgnoreSuspicious(FriendsChatMember member)
    {
        String name = Text.standardize(member.getName());
        Set<String> whitelist = Text.fromCSV(config.friendlyWhitelist()).stream()
                .map(Text::standardize)
                .collect(Collectors.toSet());

        if (whitelist.contains(name)) return true;

        FriendsChatRank rank = member.getRank();
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

    @Subscribe
    @SuppressWarnings("unused")
    public void onCommandExecuted(CommandExecuted commandExecuted)
    {
        if (commandExecuted.getCommand().equalsIgnoreCase("absent")) checkPresence();
    }

    @Subscribe
    public void onClientTick(ClientTick event)
    {
        if (lastMissingMembers == null || lastMissingMembers.isEmpty()) return;

        long durationMs = config.highlightDuration() * 1000L;
        long timeElapsed = System.currentTimeMillis() - highlightStartTime;

        if (timeElapsed < durationMs)
        {
            int highlightColor = config.getHighlightColor().getRGB() & 0xFFFFFF;
            setMemberColor(lastMissingMembers, highlightColor);
            isHighlighting = true;
        }
        else if (isHighlighting)
        {
            setMemberColor(lastMissingMembers, 0xFFFFFF);
            isHighlighting = false;
        }
    }

    private void backgroundScan()
    {
        clientThread.invokeLater(() -> lastMissingMembers = scanForMissingMembers());
    }

    public void checkPresence()
    {
        clientThread.invokeLater(() ->
        {
            FriendsChatManager friendsChatManager = client.getFriendsChatManager();
            if (friendsChatManager == null)
            {
                lastMissingMembers = Collections.emptyList();
                updatePanel(new ArrayList<>());
                return;
            }

            List<FriendsChatMember> missingMembersList = scanForMissingMembers();
            lastMissingMembers = missingMembersList;
            highlightStartTime = System.currentTimeMillis();
            isHighlighting = true;

            if (missingMembersList.isEmpty())
            {
                updatePanel(new ArrayList<>());
            }
            else
            {
                updatePanel(missingMembersList);
                int highlightColor = config.getHighlightColor().getRGB() & 0xFFFFFF;
                setMemberColor(missingMembersList, highlightColor);
            }
        });
    }

    private List<FriendsChatMember> scanForMissingMembers()
    {
        FriendsChatManager friendsChatManager = client.getFriendsChatManager();
        if (friendsChatManager == null) return Collections.emptyList();

        WorldView worldView = client.getTopLevelWorldView();
        if (worldView == null) return Collections.emptyList();

        List<String> localPlayerNames = worldView.players().stream()
                .map(p -> Text.standardize(p.getName()))
                .collect(Collectors.toList());

        String localName = client.getLocalPlayer() != null ? Text.standardize(client.getLocalPlayer().getName()) : "";
        List<FriendsChatMember> missing = new ArrayList<>();

        for (FriendsChatMember member : friendsChatManager.getMembers())
        {
            String ccMemberName = Text.standardize(member.getName());
            if (config.filterSelf() && ccMemberName.equals(localName)) continue;
            if (shouldHideRank(member.getRank())) continue;
            if (!localPlayerNames.contains(ccMemberName)) missing.add(member);
        }
        return missing;
    }

    @SuppressWarnings("unused")
    public int getMissingMembersCount() { return lastMissingMembers.size(); }

    @SuppressWarnings("unused")
    public List<FriendsChatMember> getMissingMembers() { return lastMissingMembers; }

    @SuppressWarnings("deprecation")
    private void setMemberColor(List<FriendsChatMember> members, int color)
    {
        Widget list = client.getWidget(ComponentID.FRIENDS_CHAT_LIST);
        if (list == null || list.getDynamicChildren() == null || list.isHidden()) return;

        Set<String> targetNames = members.stream()
                .map(m -> Text.standardize(m.getName()))
                .collect(Collectors.toSet());

        for (Widget child : list.getDynamicChildren())
        {
            String rawText = child.getText();
            String name = Text.standardize(Text.removeTags(rawText));
            if (targetNames.contains(name)) child.setTextColor(color);
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
}