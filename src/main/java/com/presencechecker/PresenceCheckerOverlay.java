package com.presencechecker;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.util.List;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.PanelComponent;

public class PresenceCheckerOverlay extends Overlay
{
    private final Client client;
    private final PresenceChecker plugin;
    private final PresenceCheckerConfig config;
    private final PanelComponent panelComponent = new PanelComponent();

    @Inject
    private PresenceCheckerOverlay(Client client, PresenceChecker plugin, PresenceCheckerConfig config)
    {
        this.client = client;
        this.plugin = plugin;
        this.config = config;
        setPosition(OverlayPosition.TOP_LEFT);
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        if (!config.enableOverlay()) return null;

        boolean inFriendsChat = client.getFriendsChatManager() != null;
        boolean inClanChat = client.getClanChannel() != null;
        boolean inGuestClan = client.getGuestClanChannel() != null;

        if (config.chatMode() == PresenceCheckerConfig.ChatMode.FRIENDS_CHAT && !inFriendsChat) return null;
        if (config.chatMode() == PresenceCheckerConfig.ChatMode.CLAN_CHAT && !inClanChat) return null;
        if (config.chatMode() == PresenceCheckerConfig.ChatMode.GUEST_CLAN_CHAT && !inGuestClan) return null;

        // FIXED: Updated to use PresenceChecker.PresenceMember
        List<PresenceChecker.PresenceMember> missingMembers = plugin.getMissingMembers();
        int missingCount = missingMembers.size();

        if (missingCount == 0) return null;

        panelComponent.getChildren().clear();
        panelComponent.getChildren().add(LineComponent.builder()
                .left("Missing Members:")
                .right(Integer.toString(missingCount))
                .rightColor(java.awt.Color.RED)
                .build());

        if (config.showOverlayNames() && missingCount <= config.overlayNamesLimit())
        {
            // FIXED: Updated loop variable type
            for (PresenceChecker.PresenceMember member : missingMembers)
            {
                panelComponent.getChildren().add(LineComponent.builder()
                        .left(member.getName())
                        .leftColor(java.awt.Color.WHITE)
                        .build());
            }
        }
        return panelComponent.render(graphics);
    }
}