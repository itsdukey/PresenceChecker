package com.presencechecker;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import net.runelite.api.FriendsChatMember;
import net.runelite.api.FriendsChatRank;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;

public class PresenceCheckerPanel extends PluginPanel
{
    // --- Main Layout Container ---
    private final JPanel contentPanel = new JPanel();
    private final ConfigManager configManager;
    private final PresenceCheckerConfig config;

    // --- Tutorial Container ---
    // This panel persists and swaps between "Expanded" and "Collapsed" views
    private final JPanel tutorialContainer = new JPanel(new BorderLayout());

    // --- Missing Members Components ---
    private final JPanel missingListContainer = new JPanel();
    private final List<String> currentMissingText = new ArrayList<>();
    private final JButton copyMissingButton = new JButton("Copy All");
    private final JButton clearMissingButton = new JButton("Clear Names");
    private final JButton refreshButton = new JButton("Refresh");

    // --- Suspicious Activity Components ---
    private final JPanel suspiciousListContainer = new JPanel();
    private final List<String> currentSuspiciousText = new ArrayList<>();
    private final JButton copySuspiciousButton = new JButton("Copy Names");
    private final JButton clearSuspiciousButton = new JButton("Clear Names");

    private final JScrollPane scrollPane;
    private Runnable refreshAction;
    private Runnable clearSuspiciousAction;

    @Inject
    public PresenceCheckerPanel(ConfigManager configManager, PresenceCheckerConfig config)
    {
        // Disable default PluginPanel scrollbar so we can use our custom one
        super(false);
        this.configManager = configManager;
        this.config = config;

        setLayout(new BorderLayout());
        setBackground(ColorScheme.DARK_GRAY_COLOR);
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // Use GridBagLayout for the main content to strictly control vertical stacking
        contentPanel.setLayout(new GridBagLayout());
        contentPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.gridx = 0;
        c.insets = new Insets(0, 0, 0, 0);

        // =================================================================
        // SECTION 0: TUTORIAL (Always Added, Content Varies)
        // =================================================================
        c.gridy = 0;
        c.insets = new Insets(0, 0, 15, 0);

        tutorialContainer.setBackground(ColorScheme.DARK_GRAY_COLOR);
        contentPanel.add(tutorialContainer, c);

        // Load the initial state (Expanded or Collapsed)
        rebuildTutorialPanel();

        // =================================================================
        // SECTION 1: SUSPICIOUS ACTIVITY
        // =================================================================

        // 1. Header
        c.gridy = 1;
        c.insets = new Insets(0, 0, 0, 0);
        contentPanel.add(createSuspiciousHeader(), c);

        // 2. List Container
        suspiciousListContainer.setLayout(new GridBagLayout());
        suspiciousListContainer.setBackground(ColorScheme.DARK_GRAY_COLOR);

        c.gridy = 2;
        // Add some bottom padding between this list and the divider
        c.insets = new Insets(0, 0, 15, 0);
        contentPanel.add(suspiciousListContainer, c);

        // =================================================================
        // SECTION 2: DIVIDER
        // =================================================================
        JPanel divider = new JPanel(new BorderLayout());
        divider.setBackground(ColorScheme.DARK_GRAY_COLOR);
        JPanel line = new JPanel();
        line.setBackground(ColorScheme.MEDIUM_GRAY_COLOR);
        line.setPreferredSize(new Dimension(0, 1));
        divider.add(line, BorderLayout.CENTER);

        c.gridy = 3;
        c.insets = new Insets(0, 0, 15, 0); // Padding below line
        contentPanel.add(divider, c);

        // =================================================================
        // SECTION 3: MISSING MEMBERS
        // =================================================================

        // 1. Header
        c.gridy = 4;
        c.insets = new Insets(0, 0, 0, 0);
        contentPanel.add(createMissingHeader(), c);

        // 2. List Container
        missingListContainer.setLayout(new GridBagLayout());
        missingListContainer.setBackground(ColorScheme.DARK_GRAY_COLOR);

        c.gridy = 5;
        contentPanel.add(missingListContainer, c);

        // =================================================================
        // SECTION 4: INVISIBLE FILLER
        // =================================================================
        JPanel filler = new JPanel();
        filler.setBackground(ColorScheme.DARK_GRAY_COLOR);

        c.gridy = 6;
        c.weighty = 1; // IMPORTANT: Consumes extra vertical space
        c.fill = GridBagConstraints.BOTH;
        contentPanel.add(filler, c);

        // =================================================================
        // SCROLL PANE SETUP
        // =================================================================
        scrollPane = new JScrollPane(contentPanel);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(new EmptyBorder(0, 0, 0, 0));
        scrollPane.setBackground(ColorScheme.DARK_GRAY_COLOR);
        scrollPane.setVerticalScrollBar(new DarkScrollBar());

        add(scrollPane, BorderLayout.CENTER);

        // Initialize Defaults
        addDefaultMessage(suspiciousListContainer, "No recent suspicious activity.");
        addDefaultMessage(missingListContainer, "Run ::absent or Refresh.");

        updateButtonsState();
    }

    /**
     * Rebuilds the tutorial panel based on the current config state.
     * If true: Shows full guide (Expanded).
     * If false: Shows header only (Collapsed).
     */
    private void rebuildTutorialPanel()
    {
        tutorialContainer.removeAll();
        boolean isExpanded = config.showPanelTutorial();

        if (isExpanded)
        {
            tutorialContainer.add(createExpandedTutorial());
        }
        else
        {
            tutorialContainer.add(createCollapsedTutorial());
        }

        tutorialContainer.revalidate();
        tutorialContainer.repaint();
    }

    private JPanel createExpandedTutorial()
    {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        panel.setBorder(new LineBorder(ColorScheme.MEDIUM_GRAY_COLOR));

        // --- Header (Title + Minimize Button) ---
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        header.setBorder(new EmptyBorder(5, 8, 5, 5));

        JLabel title = new JLabel("Plugin Guide");
        title.setFont(FontManager.getRunescapeBoldFont());
        title.setForeground(Color.WHITE);

        JButton minimizeBtn = createIconButton("-", "Minimize guide");
        minimizeBtn.addActionListener(e -> {
            configManager.setConfiguration("presencechecker", "showPanelTutorial", false);
            rebuildTutorialPanel();
        });

        header.add(title, BorderLayout.CENTER);
        header.add(minimizeBtn, BorderLayout.EAST);

        // --- Body Content ---
        JPanel body = new JPanel(new GridBagLayout());
        body.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        body.setBorder(new EmptyBorder(0, 8, 8, 8));

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.gridx = 0;
        c.gridy = 0;
        c.insets = new Insets(0, 0, 8, 0);

        // Intro
        body.add(createHelpTextHTML(
                "<b>Welcome!</b><br>" +
                        "This tool helps you track Friends Chat attendance and detect potential spies."
        ), c);

        c.gridy++;
        // Section 1: Presence
        body.add(createHelpTextHTML(
                "<br><b>1. Missing Members (Presence)</b><br>" +
                        "Identifies Friends Chat Members who are logged in but <u>not nearby</u>.<br><br>" +
                        "• <b>How:</b> Click 'Refresh' below or type <code>::absent</code> in gamechat.<br>" +
                        "<div style='margin-top: 4px'>• <b>Use Case:</b> Verifying everyone has arrived at a raid lobby, drop party, or boss mass.</div>"
        ), c);

        c.gridy++;
        // Section 2: Anti-Scout
        body.add(createHelpTextHTML(
                "<br><b>2. Suspicious Activity (Anti-Scout)</b><br>" +
                        "Automatically detects players who join and leave the chat very quickly.<br><br>" +
                        "• <b>How:</b> It runs passively. Names appear in the list above.<br>" +
                        "<div style='margin-top: 4px'>• <b>Use Case:</b> Spotting enemy scouts hopping in and out of friends chats to find mass worlds and disrupt events.</div>"
        ), c);

        c.gridy++;
        // Section 3: Configuration
        body.add(createHelpTextHTML(
                "<br><b>3. Configuration</b><br>" +
                        "Open the Plugin Settings, search 'presence checker', and click the gear icon to 'edit plugin configuration' to adjust:<br>" +
                        "<div style='margin-top: 6px'>• Adjust the Suspicion Timer (in milliseconds).</div>" +
                        "• Hide specific ranks you don't want showing up in the detection lists (e.g. Generals).<br>" +
                        "• Change highlight colors."

        ), c);

        panel.add(header, BorderLayout.NORTH);
        panel.add(body, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createCollapsedTutorial()
    {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        panel.setBorder(new LineBorder(ColorScheme.MEDIUM_GRAY_COLOR));

        // --- Header Only (Title + Expand Button) ---
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        header.setBorder(new EmptyBorder(5, 8, 5, 5));

        JLabel title = new JLabel("Plugin Guide");
        title.setFont(FontManager.getRunescapeBoldFont());
        title.setForeground(Color.GRAY); // Dimmed color when collapsed

        JButton expandBtn = createIconButton("+", "Expand guide");
        expandBtn.addActionListener(e -> {
            configManager.setConfiguration("presencechecker", "showPanelTutorial", true);
            rebuildTutorialPanel();
        });

        header.add(title, BorderLayout.CENTER);
        header.add(expandBtn, BorderLayout.EAST);

        panel.add(header, BorderLayout.CENTER);
        return panel;
    }

    private JButton createIconButton(String text, String tooltip)
    {
        JButton btn = new JButton(text);
        btn.setPreferredSize(new Dimension(20, 20));
        btn.setFont(FontManager.getRunescapeSmallFont());
        btn.setForeground(Color.LIGHT_GRAY);
        btn.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        btn.setBorder(null);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setToolTipText(tooltip);

        btn.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseEntered(MouseEvent e)
            {
                btn.setForeground(Color.WHITE);
            }
            @Override
            public void mouseExited(MouseEvent e)
            {
                btn.setForeground(Color.LIGHT_GRAY);
            }
        });
        return btn;
    }

    private JLabel createHelpTextHTML(String htmlBody)
    {
        // Width at 150px to prevent cutoff
        JLabel label = new JLabel("<html><body style='width: 150px'>" + htmlBody + "</body></html>");
        label.setFont(FontManager.getRunescapeSmallFont());
        label.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
        return label;
    }

    private JPanel createSuspiciousHeader()
    {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(ColorScheme.DARK_GRAY_COLOR);
        panel.setBorder(new EmptyBorder(0, 0, 5, 0));

        JLabel title = new JLabel("Suspicious Activity");
        title.setForeground(Color.ORANGE);
        title.setFont(FontManager.getRunescapeBoldFont());
        title.setHorizontalAlignment(JLabel.CENTER);

        JPanel btnPanel = new JPanel(new GridLayout(1, 2, 8, 0));
        btnPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);

        styleButton(copySuspiciousButton);
        copySuspiciousButton.addActionListener(e -> copyToClipboard(currentSuspiciousText, copySuspiciousButton));

        styleButton(clearSuspiciousButton);
        clearSuspiciousButton.addActionListener(e -> {
            if (clearSuspiciousAction != null) clearSuspiciousAction.run();
        });

        btnPanel.add(copySuspiciousButton);
        btnPanel.add(clearSuspiciousButton);

        panel.add(title, BorderLayout.NORTH);
        panel.add(btnPanel, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createMissingHeader()
    {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(ColorScheme.DARK_GRAY_COLOR);
        panel.setBorder(new EmptyBorder(0, 0, 5, 0));

        JLabel title = new JLabel("Missing Members");
        title.setForeground(Color.WHITE);
        title.setFont(FontManager.getRunescapeBoldFont());
        title.setHorizontalAlignment(JLabel.CENTER);

        JPanel btnContainer = new JPanel(new BorderLayout(0, 4));
        btnContainer.setBackground(ColorScheme.DARK_GRAY_COLOR);

        styleButton(refreshButton);
        refreshButton.setToolTipText("Re-check for missing members");
        refreshButton.addActionListener(e -> {
            if (refreshAction != null) refreshAction.run();
        });

        JPanel subBtnPanel = new JPanel(new GridLayout(1, 2, 4, 0));
        subBtnPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);

        styleButton(copyMissingButton);
        copyMissingButton.addActionListener(e -> copyToClipboard(currentMissingText, copyMissingButton));

        styleButton(clearMissingButton);
        clearMissingButton.addActionListener(e -> clearMissingList());

        subBtnPanel.add(copyMissingButton);
        subBtnPanel.add(clearMissingButton);

        btnContainer.add(refreshButton, BorderLayout.NORTH);
        btnContainer.add(subBtnPanel, BorderLayout.CENTER);

        panel.add(title, BorderLayout.NORTH);
        panel.add(btnContainer, BorderLayout.SOUTH);
        return panel;
    }

    public void setRefreshAction(Runnable action)
    {
        this.refreshAction = action;
    }

    public void setClearSuspiciousAction(Runnable action)
    {
        this.clearSuspiciousAction = action;
    }

    private void styleButton(JButton btn)
    {
        btn.setFocusable(false);
        btn.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        btn.setForeground(Color.WHITE);
        btn.setFont(FontManager.getRunescapeSmallFont());
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(6, 6, 6, 6));
    }

    private void updateButtonsState()
    {
        boolean hasMissing = !currentMissingText.isEmpty();
        copyMissingButton.setEnabled(hasMissing);
        clearMissingButton.setEnabled(hasMissing);
        if (!hasMissing) copyMissingButton.setText("Copy All");

        boolean hasSuspicious = !currentSuspiciousText.isEmpty();
        copySuspiciousButton.setEnabled(hasSuspicious);
        clearSuspiciousButton.setEnabled(hasSuspicious);
        if (!hasSuspicious) copySuspiciousButton.setText("Copy Names");
    }

    // --- LOGIC: CLEAR MISSING LIST ---
    private void clearMissingList()
    {
        missingListContainer.removeAll();
        currentMissingText.clear();
        addDefaultMessage(missingListContainer, "Run ::absent or Refresh.");
        updateButtonsState();
        revalidateContainer(missingListContainer);
    }

    // --- UPDATE MISSING LIST ---
    public void updateMissingList(List<FriendsChatMember> members)
    {
        missingListContainer.removeAll();
        currentMissingText.clear();

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.gridx = 0;
        c.gridy = 0;
        c.insets = new Insets(0, 0, 5, 0);

        if (members.isEmpty())
        {
            addDefaultMessage(missingListContainer, "No missing members.");
        }
        else
        {
            copyMissingButton.setText("Copy (" + members.size() + ")");
            for (FriendsChatMember member : members)
            {
                String rankPrefix = getRankPrefix(member.getRank());
                String displayText = rankPrefix + member.getName();
                currentMissingText.add(displayText);

                missingListContainer.add(createRow(displayText, !rankPrefix.isEmpty()), c);
                c.gridy++;
            }
        }
        updateButtonsState();
        revalidateContainer(missingListContainer);
    }

    // --- UPDATE SUSPICIOUS LIST ---
    public void updateSuspiciousList(List<String> names)
    {
        suspiciousListContainer.removeAll();
        currentSuspiciousText.clear();
        currentSuspiciousText.addAll(names);

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.gridx = 0;
        c.gridy = 0;
        c.insets = new Insets(0, 0, 5, 0);

        if (names.isEmpty())
        {
            addDefaultMessage(suspiciousListContainer, "No recent suspicious activity.");
        }
        else
        {
            copySuspiciousButton.setText("Copy (" + names.size() + ")");
            for (String name : names)
            {
                suspiciousListContainer.add(createRow(name, false), c);
                c.gridy++;
            }
        }
        updateButtonsState();
        revalidateContainer(suspiciousListContainer);
    }

    private void revalidateContainer(JPanel panel)
    {
        panel.revalidate();
        panel.repaint();
    }

    private void copyToClipboard(List<String> textList, JButton button)
    {
        if (textList.isEmpty()) return;

        String clipboardString = String.join("\n", textList);
        StringSelection stringSelection = new StringSelection(clipboardString);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringSelection, null);

        String originalText = button.getText();
        button.setText("Copied!");

        Timer timer = new Timer(2000, e -> button.setText(originalText));
        timer.setRepeats(false);
        timer.start();
    }

    private void addDefaultMessage(JPanel container, String message)
    {
        container.removeAll();
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.gridx = 0;
        c.gridy = 0;

        JLabel emptyLabel = new JLabel(message);
        emptyLabel.setForeground(Color.GRAY);
        emptyLabel.setHorizontalAlignment(JLabel.CENTER);
        emptyLabel.setBorder(new EmptyBorder(10, 0, 10, 0));

        container.add(emptyLabel, c);
    }

    private JPanel createRow(String text, boolean hasRank)
    {
        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        row.setBorder(new EmptyBorder(8, 8, 8, 8));

        JLabel label = new JLabel(text);
        label.setForeground(Color.WHITE);
        label.setFont(FontManager.getRunescapeSmallFont());

        if (hasRank)
        {
            label.setForeground(Color.ORANGE);
        }

        row.add(label, BorderLayout.WEST);

        row.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseEntered(MouseEvent e)
            {
                row.setBackground(ColorScheme.DARKER_GRAY_HOVER_COLOR);
            }

            @Override
            public void mouseExited(MouseEvent e)
            {
                row.setBackground(ColorScheme.DARKER_GRAY_COLOR);
            }
        });

        return row;
    }

    private String getRankPrefix(FriendsChatRank rank)
    {
        if (rank == null) return "";
        switch (rank)
        {
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

    // --- Custom Dark ScrollBar Implementation ---
    private static class DarkScrollBar extends JScrollBar
    {
        public DarkScrollBar()
        {
            setUI(new DarkScrollBarUI());
            setPreferredSize(new Dimension(8, 0));
            setForeground(ColorScheme.MEDIUM_GRAY_COLOR);
            setBackground(ColorScheme.DARK_GRAY_COLOR);
        }

        @Override
        public void updateUI()
        {
            setUI(new DarkScrollBarUI());
        }
    }

    private static class DarkScrollBarUI extends BasicScrollBarUI
    {
        private static final Color TRACK_COLOR = ColorScheme.DARK_GRAY_COLOR;
        private static final Color THUMB_COLOR = ColorScheme.MEDIUM_GRAY_COLOR;
        private static final Color THUMB_HOVER_COLOR = ColorScheme.LIGHT_GRAY_COLOR;

        @Override
        protected void configureScrollBarColors()
        {
            this.thumbColor = THUMB_COLOR;
            this.trackColor = TRACK_COLOR;
        }

        @Override
        protected JButton createDecreaseButton(int orientation)
        {
            return createZeroButton();
        }

        @Override
        protected JButton createIncreaseButton(int orientation)
        {
            return createZeroButton();
        }

        private JButton createZeroButton()
        {
            JButton btn = new JButton();
            btn.setPreferredSize(new Dimension(0, 0));
            btn.setMinimumSize(new Dimension(0, 0));
            btn.setMaximumSize(new Dimension(0, 0));
            return btn;
        }

        @Override
        protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds)
        {
            g.setColor(TRACK_COLOR);
            g.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
        }

        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds)
        {
            if (thumbBounds.isEmpty() || !scrollbar.isEnabled())
            {
                return;
            }

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (isDragging)
            {
                g2.setColor(THUMB_HOVER_COLOR);
            }
            else
            {
                g2.setColor(THUMB_COLOR);
            }

            g2.fillRoundRect(thumbBounds.x, thumbBounds.y, thumbBounds.width, thumbBounds.height, 8, 8);
            g2.dispose();
        }
    }
}