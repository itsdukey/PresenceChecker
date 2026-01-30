package com.presencechecker;

import com.google.inject.Provides;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
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
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.FriendsChatManager;
import net.runelite.api.FriendsChatMember;
import net.runelite.api.FriendsChatRank;
import net.runelite.api.WorldView;
import net.runelite.api.clan.ClanChannel;
import net.runelite.api.clan.ClanChannelMember;
import net.runelite.api.clan.ClanMember;
import net.runelite.api.clan.ClanRank;
import net.runelite.api.clan.ClanSettings;
import net.runelite.api.clan.ClanTitle;
import net.runelite.api.events.ClanMemberJoined;
import net.runelite.api.events.ClanMemberLeft;
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
        description = "Checks which Friends Chat or Clan members are currently in the vicinity",
        tags = {"presence", "clan", "check", "pvp", "scout", "fc"}
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
    private volatile List<PresenceMember> lastMissingMembers = Collections.emptyList();
    private ScheduledFuture<?> overlayTask;
    private long highlightStartTime = 0;
    private boolean isHighlighting = false;
    private final Map<String, Long> joinTimes = new HashMap<>();
    private final List<String> suspiciousDisplayList = new ArrayList<>();
    private final Map<String, Integer> suspiciousCounts = new HashMap<>();

    // --- STATIC MAP FOR FAST TITLE LOOKUP ---
    private static final Map<String, PresenceCheckerConfig.ClanRankOption> TITLE_MAP = new HashMap<>();

    static {
        for (PresenceCheckerConfig.ClanRankOption option : PresenceCheckerConfig.ClanRankOption.values()) {
            if (option == PresenceCheckerConfig.ClanRankOption.NONE) continue;
            if (option.name().startsWith("_SEC_")) continue;

            // Map clean name (e.g. "Speed-Runner") to enum
            String key = option.toString().toLowerCase();
            TITLE_MAP.put(key, option);
        }
    }

    @Getter
    @RequiredArgsConstructor
    public static class PresenceMember
    {
        private final String name;
        private final String rankName;
    }

    @Provides
    PresenceCheckerConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(PresenceCheckerConfig.class);
    }

    @Override
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

    // --- EVENT LISTENERS ---

    @Subscribe
    public void onFriendsChatMemberJoined(FriendsChatMemberJoined event)
    {
        if (config.chatMode() != PresenceCheckerConfig.ChatMode.FRIENDS_CHAT) return;
        handleJoin(event.getMember().getName(), event.getMember().getRank(), null);
    }

    @Subscribe
    public void onFriendsChatMemberLeft(FriendsChatMemberLeft event)
    {
        if (config.chatMode() != PresenceCheckerConfig.ChatMode.FRIENDS_CHAT) return;
        handleLeave(event.getMember().getName(), event.getMember().getRank(), null);
    }

    @Subscribe
    public void onClanMemberJoined(ClanMemberJoined event)
    {
        if (config.chatMode() == PresenceCheckerConfig.ChatMode.FRIENDS_CHAT) return;
        boolean isGuest = (config.chatMode() == PresenceCheckerConfig.ChatMode.GUEST_CLAN_CHAT);
        if (event.getClanChannel() == (isGuest ? client.getGuestClanChannel() : client.getClanChannel()))
        {
            handleJoin(event.getClanMember().getName(), null, event.getClanMember().getRank());
        }
    }

    @Subscribe
    public void onClanMemberLeft(ClanMemberLeft event)
    {
        if (config.chatMode() == PresenceCheckerConfig.ChatMode.FRIENDS_CHAT) return;
        boolean isGuest = (config.chatMode() == PresenceCheckerConfig.ChatMode.GUEST_CLAN_CHAT);
        if (event.getClanChannel() == (isGuest ? client.getGuestClanChannel() : client.getClanChannel()))
        {
            handleLeave(event.getClanMember().getName(), null, event.getClanMember().getRank());
        }
    }

    private void handleJoin(String rawName, FriendsChatRank fcRank, ClanRank clanRank)
    {
        if (!config.enableSuspiciousTracking()) return;
        String name = Text.standardize(rawName);

        if (isBlacklisted(name))
        {
            String msg = "BLACKLISTED PLAYER DETECTED: " + rawName;
            sendChatMessage(ColorUtil.wrapWithColorTag(msg, Color.RED));
            notifier.notify("BLACKLISTED PLAYER: " + rawName);
            addSuspiciousUser(rawName, 0);
            return;
        }

        if (shouldIgnoreSuspicious(name, fcRank, clanRank)) return;
        joinTimes.put(name, System.currentTimeMillis());
    }

    private void handleLeave(String rawName, FriendsChatRank fcRank, ClanRank clanRank)
    {
        if (!config.enableSuspiciousTracking()) return;
        String name = Text.standardize(rawName);
        if (shouldIgnoreSuspicious(name, fcRank, clanRank)) return;

        Long joinTime = joinTimes.remove(name);
        if (joinTime != null)
        {
            long durationMs = System.currentTimeMillis() - joinTime;
            long thresholdMs = config.suspiciousThreshold();
            if (durationMs <= thresholdMs)
            {
                addSuspiciousUser(rawName, durationMs);
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
            String msg = rawName + " was found suspicious! (" + durationMs + "ms)";
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

    private boolean shouldIgnoreSuspicious(String rawName, FriendsChatRank fcRank, ClanRank clanRank)
    {
        String name = Text.standardize(rawName);
        Set<String> whitelist = Text.fromCSV(config.friendlyWhitelist()).stream()
                .map(Text::standardize)
                .collect(Collectors.toSet());

        if (whitelist.contains(name)) return true;

        if (config.chatMode() == PresenceCheckerConfig.ChatMode.FRIENDS_CHAT && fcRank != null)
        {
            return shouldHideRank(fcRank);
        }
        else if (clanRank != null)
        {
            String realTitle = getRealClanRankTitle(rawName, config.chatMode() == PresenceCheckerConfig.ChatMode.GUEST_CLAN_CHAT);
            return shouldHideByClanTitle(realTitle, clanRank);
        }
        return false;
    }

    @Subscribe
    public void onCommandExecuted(CommandExecuted commandExecuted)
    {
        if (commandExecuted.getCommand().equalsIgnoreCase("absent")) checkPresence();
    }

    @Subscribe
    public void onClientTick(ClientTick event)
    {
        if (lastMissingMembers == null || lastMissingMembers.isEmpty()) return;

        if (config.chatMode() != PresenceCheckerConfig.ChatMode.FRIENDS_CHAT) return;

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
        clientThread.invokeLater(() -> {
            lastMissingMembers = scanForMissingMembers();
            if (config.autoUpdatePanel())
            {
                SwingUtilities.invokeLater(() -> panel.updateMissingList(lastMissingMembers));
            }
        });
    }

    public void checkPresence()
    {
        clientThread.invokeLater(() ->
        {
            List<PresenceMember> missingMembersList = scanForMissingMembers();
            lastMissingMembers = missingMembersList;
            highlightStartTime = System.currentTimeMillis();
            isHighlighting = true;

            SwingUtilities.invokeLater(() -> panel.updateMissingList(missingMembersList));

            if (!missingMembersList.isEmpty() && config.chatMode() == PresenceCheckerConfig.ChatMode.FRIENDS_CHAT)
            {
                int highlightColor = config.getHighlightColor().getRGB() & 0xFFFFFF;
                setMemberColor(missingMembersList, highlightColor);
            }
        });
    }

    private List<PresenceMember> scanForMissingMembers()
    {
        WorldView worldView = client.getTopLevelWorldView();
        if (worldView == null) return Collections.emptyList();

        List<String> localPlayerNames = worldView.players().stream()
                .map(p -> Text.standardize(p.getName()))
                .collect(Collectors.toList());

        String localName = client.getLocalPlayer() != null ? Text.standardize(client.getLocalPlayer().getName()) : "";
        List<PresenceMember> missing = new ArrayList<>();

        PresenceCheckerConfig.ChatMode mode = config.chatMode();

        if (mode == PresenceCheckerConfig.ChatMode.FRIENDS_CHAT)
        {
            FriendsChatManager fcm = client.getFriendsChatManager();
            if (fcm == null) return Collections.emptyList();

            for (FriendsChatMember member : fcm.getMembers())
            {
                String name = Text.standardize(member.getName());
                if (config.filterSelf() && name.equals(localName)) continue;
                if (shouldHideRank(member.getRank())) continue;
                if (!localPlayerNames.contains(name)) {
                    missing.add(new PresenceMember(member.getName(), getRankPrefix(member.getRank())));
                }
            }
        }
        else
        {
            boolean isGuest = (mode == PresenceCheckerConfig.ChatMode.GUEST_CLAN_CHAT);
            ClanChannel channel = isGuest ? client.getGuestClanChannel() : client.getClanChannel();

            if (channel == null) return Collections.emptyList();

            for (ClanChannelMember member : channel.getMembers())
            {
                String name = Text.standardize(member.getName());
                if (config.filterSelf() && name.equals(localName)) continue;

                String realTitle = getRealClanRankTitle(member.getName(), isGuest);

                if (shouldHideByClanTitle(realTitle, member.getRank())) continue;

                if (!localPlayerNames.contains(name)) {
                    String prefix = (realTitle != null) ? "[" + realTitle + "] " : getClanRankPrefix(member.getRank());
                    missing.add(new PresenceMember(member.getName(), prefix));
                }
            }
        }

        return missing;
    }

    private String getRealClanRankTitle(String playerName, boolean isGuest)
    {
        if (isGuest) return null;

        ClanSettings settings = client.getClanSettings();
        if (settings == null) return null;

        ClanMember member = settings.findMember(playerName);
        if (member == null) return null;

        ClanRank rank = member.getRank();
        ClanTitle title = settings.titleForRank(rank);

        return (title != null) ? title.getName() : null;
    }

    private boolean shouldHideByClanTitle(String title, ClanRank fallbackRank)
    {
        List<PresenceCheckerConfig.ClanRankOption> filters = Arrays.asList(
                config.filterRank1(), config.filterRank2(), config.filterRank3(), config.filterRank4(),
                config.filterRank5(), config.filterRank6(), config.filterRank7(), config.filterRank8(),
                config.filterRank9(), config.filterRank10(), config.filterRank11(), config.filterRank12(),
                config.filterRank13(), config.filterRank14(), config.filterRank15(), config.filterRank16(),
                config.filterRank17(), config.filterRank18(), config.filterRank19(), config.filterRank20(),
                config.filterRank21(), config.filterRank22(), config.filterRank23(), config.filterRank24()
        );

        PresenceCheckerConfig.ClanRankOption currentMemberOption = matchPlayerToOption(title, fallbackRank);

        if (currentMemberOption == PresenceCheckerConfig.ClanRankOption.NONE || currentMemberOption.toString().startsWith("---")) return false;

        return filters.contains(currentMemberOption);
    }

    private PresenceCheckerConfig.ClanRankOption matchPlayerToOption(String title, ClanRank fallbackRank)
    {
        if (fallbackRank == ClanRank.OWNER) return PresenceCheckerConfig.ClanRankOption.OWNER;
        if (fallbackRank == ClanRank.DEPUTY_OWNER) return PresenceCheckerConfig.ClanRankOption.DEPUTY_OWNER;
        if (fallbackRank == ClanRank.ADMINISTRATOR) return PresenceCheckerConfig.ClanRankOption.ADMINISTRATOR;
        if (fallbackRank == ClanRank.JMOD) return PresenceCheckerConfig.ClanRankOption.JMOD;
        if (fallbackRank == ClanRank.GUEST) return PresenceCheckerConfig.ClanRankOption.GUEST;

        if (title != null)
        {
            String key = title.toLowerCase();
            if (TITLE_MAP.containsKey(key)) {
                return TITLE_MAP.get(key);
            }
        }

        return PresenceCheckerConfig.ClanRankOption.NONE;
    }

    public List<PresenceMember> getMissingMembers() { return lastMissingMembers; }

    @SuppressWarnings("deprecation")
    private void setMemberColor(List<PresenceMember> members, int color)
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

    private String getRankPrefix(FriendsChatRank rank)
    {
        switch (rank) {
            case OWNER: return "[Owner] ";
            case GENERAL: return "[Gen] ";
            case CAPTAIN: return "[Capt] ";
            case LIEUTENANT: return "[Lt] ";
            case SERGEANT: return "[Sgt] ";
            case CORPORAL: return "[Corp] ";
            case RECRUIT: return "[Rec] ";
            case FRIEND: return "[Friend] ";
            default: return "";
        }
    }

    private String getClanRankPrefix(ClanRank rank)
    {
        if (rank == ClanRank.OWNER) return "[Owner] ";
        if (rank == ClanRank.DEPUTY_OWNER) return "[Dep] ";
        if (rank == ClanRank.ADMINISTRATOR) return "[Adm] ";
        if (rank == ClanRank.GUEST) return "[Guest] ";
        if (rank == ClanRank.JMOD) return "[JMod] ";
        return "[Memb] ";
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