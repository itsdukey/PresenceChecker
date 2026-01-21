package com.presencechecker;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.util.List;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.FriendsChatMember;
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
        // 1. Check Config
        if (!config.enableOverlay())
        {
            return null;
        }

        // 2. Check if we are actually in a Friends Chat
        // If this is null, we are not in a chat, so we hide the overlay completely.
        if (client.getFriendsChatManager() == null)
        {
            return null;
        }

        List<FriendsChatMember> missingMembers = plugin.getMissingMembers();
        int missingCount = missingMembers.size();

        panelComponent.getChildren().clear();
        panelComponent.getChildren().add(LineComponent.builder()
                .left("Missing Members:")
                .right(Integer.toString(missingCount))
                .rightColor(missingCount > 0 ? java.awt.Color.RED : java.awt.Color.WHITE) // Hardcoded to RED
                .build());

        // Render detailed names if enabled and under limit
        if (config.showOverlayNames() && missingCount > 0 && missingCount <= config.overlayNamesLimit())
        {
            for (FriendsChatMember member : missingMembers)
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