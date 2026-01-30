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
import net.runelite.client.config.ConfigManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;

public class PresenceCheckerPanel extends PluginPanel
{
    private final JPanel contentPanel = new JPanel();
    private final ConfigManager configManager;
    private final PresenceCheckerConfig config;
    private final JPanel tutorialContainer = new JPanel(new BorderLayout());
    private final JPanel missingListContainer = new JPanel();
    private final List<String> currentMissingText = new ArrayList<>();
    private final JButton copyMissingButton = new JButton("Copy All");
    private final JButton clearMissingButton = new JButton("Clear Names");
    private final JButton refreshButton = new JButton("Refresh");
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
        super(false);
        this.configManager = configManager;
        this.config = config;

        setLayout(new BorderLayout());
        setBackground(ColorScheme.DARK_GRAY_COLOR);
        setBorder(new EmptyBorder(10, 10, 10, 10));

        contentPanel.setLayout(new GridBagLayout());
        contentPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.gridx = 0;
        c.insets = new Insets(0, 0, 0, 0);

        c.gridy = 0;
        c.insets = new Insets(0, 0, 15, 0);
        tutorialContainer.setBackground(ColorScheme.DARK_GRAY_COLOR);
        contentPanel.add(tutorialContainer, c);
        rebuildTutorialPanel();

        c.gridy = 1;
        c.insets = new Insets(0, 0, 0, 0);
        contentPanel.add(createSuspiciousHeader(), c);

        suspiciousListContainer.setLayout(new GridBagLayout());
        suspiciousListContainer.setBackground(ColorScheme.DARK_GRAY_COLOR);

        c.gridy = 2;
        c.insets = new Insets(0, 0, 15, 0);
        contentPanel.add(suspiciousListContainer, c);

        JPanel divider = new JPanel(new BorderLayout());
        divider.setBackground(ColorScheme.DARK_GRAY_COLOR);
        JPanel line = new JPanel();
        line.setBackground(ColorScheme.MEDIUM_GRAY_COLOR);
        line.setPreferredSize(new Dimension(0, 1));
        divider.add(line, BorderLayout.CENTER);

        c.gridy = 3;
        c.insets = new Insets(0, 0, 15, 0);
        contentPanel.add(divider, c);

        c.gridy = 4;
        c.insets = new Insets(0, 0, 0, 0);
        contentPanel.add(createMissingHeader(), c);

        missingListContainer.setLayout(new GridBagLayout());
        missingListContainer.setBackground(ColorScheme.DARK_GRAY_COLOR);

        c.gridy = 5;
        contentPanel.add(missingListContainer, c);

        JPanel filler = new JPanel();
        filler.setBackground(ColorScheme.DARK_GRAY_COLOR);

        c.gridy = 6;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        contentPanel.add(filler, c);

        scrollPane = new JScrollPane(contentPanel);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(new EmptyBorder(0, 0, 0, 0));
        scrollPane.setBackground(ColorScheme.DARK_GRAY_COLOR);
        scrollPane.setVerticalScrollBar(new DarkScrollBar());

        add(scrollPane, BorderLayout.CENTER);

        addDefaultMessage(suspiciousListContainer, "No recent suspicious activity.");
        addDefaultMessage(missingListContainer, "Run ::absent or Refresh.");
        updateButtonsState();
    }

    private void rebuildTutorialPanel()
    {
        tutorialContainer.removeAll();
        if (config.showPanelTutorial()) tutorialContainer.add(createExpandedTutorial());
        else tutorialContainer.add(createCollapsedTutorial());
        tutorialContainer.revalidate();
        tutorialContainer.repaint();
    }

    private JPanel createExpandedTutorial()
    {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        panel.setBorder(new LineBorder(ColorScheme.MEDIUM_GRAY_COLOR));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        header.setBorder(new EmptyBorder(5, 8, 5, 5));

        // Listener to minimize when header is clicked
        MouseAdapter minimizeAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                configManager.setConfiguration("presencechecker", "showPanelTutorial", false);
                rebuildTutorialPanel();
            }
            @Override
            public void mouseEntered(MouseEvent e) {
                header.setBackground(ColorScheme.DARKER_GRAY_HOVER_COLOR);
                panel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                header.setBackground(ColorScheme.DARKER_GRAY_COLOR);
                panel.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }
        };

        header.addMouseListener(minimizeAdapter);

        JLabel title = new JLabel("Plugin Guide");
        title.setFont(FontManager.getRunescapeBoldFont());
        title.setForeground(Color.WHITE);
        title.addMouseListener(minimizeAdapter);

        JButton minimizeBtn = createIconButton("-", "Minimize guide");
        minimizeBtn.addActionListener(e -> {
            configManager.setConfiguration("presencechecker", "showPanelTutorial", false);
            rebuildTutorialPanel();
        });

        header.add(title, BorderLayout.CENTER);
        header.add(minimizeBtn, BorderLayout.EAST);

        JPanel body = new JPanel(new GridBagLayout());
        body.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        body.setBorder(new EmptyBorder(0, 8, 8, 8));

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.gridx = 0;
        c.gridy = 0;
        c.insets = new Insets(0, 0, 8, 0);

        // --- INTRO ---
        body.add(createHelpTextHTML("<b>Welcome!</b><br>This tool tracks attendance and detects potential spies in your channel."), c);

        // --- SECTION 1: MODES ---
        c.gridy++;
        body.add(createHelpTextHTML("<br><b>1. Chat Selection</b><br>Go to Config > General Settings > <b>Target Chat</b> to switch between:<br>• Friends Chat (Green)<br>• Clan Chat (Orange)<br>• Guest Clan Chat"), c);

        // --- SECTION 2: PRESENCE ---
        c.gridy++;
        body.add(createHelpTextHTML("<br><b>2. Missing Members</b><br>Identifies members in chat but <u>not nearby</u>.<br><br><b>Controls:</b><br>• <b>Refresh:</b> Manually check.<br>• <b>::absent:</b> Chat command.<br>• <b>Auto-Update:</b> (Config) Toggle to refresh this panel live every 5s."), c);

        // --- SECTION 3: CLAN FILTERS ---
        c.gridy++;
        body.add(createHelpTextHTML("<br><b>3. Clan Chat Filters</b><br>To hide specific ranks in Clan mode:<br><div style='margin-top: 4px'>• Go to <b>Config > Clan Chat Filters</b>.</div>• Use the <b>24 Dropdown Slots</b> to select ranks to hide (e.g. Recruit, Goblin).<br>• <b>Tip:</b> Click a dropdown and type the first letter to jump to a rank."), c);

        // --- SECTION 4: FC FILTERS ---
        c.gridy++;
        body.add(createHelpTextHTML("<br><b>4. Friends Chat Filters</b><br>To hide ranks in Friends Chat mode:<br><div style='margin-top: 4px'>• Go to <b>Config > Friends Chat Filters</b>.</div>• Use the toggle boxes (e.g. 'Hide Captains')."), c);

        // --- SECTION 5: ANTI-SCOUT ---
        c.gridy++;
        body.add(createHelpTextHTML("<br><b>5. Anti-Scout</b><br>Detects players joining/leaving quickly.<br>• <b>Suspicious Time:</b> Max time (ms) allowed.<br>• <b>Blacklist:</b> Names to INSTANTLY alert on."), c);

        panel.add(header, BorderLayout.NORTH);
        panel.add(body, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createCollapsedTutorial()
    {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        panel.setBorder(new LineBorder(ColorScheme.MEDIUM_GRAY_COLOR));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        header.setBorder(new EmptyBorder(5, 8, 5, 5));

        // Listener to expand when header is clicked
        MouseAdapter expandAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                configManager.setConfiguration("presencechecker", "showPanelTutorial", true);
                rebuildTutorialPanel();
            }
            @Override
            public void mouseEntered(MouseEvent e) {
                header.setBackground(ColorScheme.DARKER_GRAY_HOVER_COLOR);
                panel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                header.setBackground(ColorScheme.DARKER_GRAY_COLOR);
                panel.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }
        };

        header.addMouseListener(expandAdapter);

        JLabel title = new JLabel("Plugin Guide");
        title.setFont(FontManager.getRunescapeBoldFont());
        title.setForeground(Color.GRAY);
        title.addMouseListener(expandAdapter);

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
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setForeground(Color.WHITE); }
            public void mouseExited(MouseEvent e) { btn.setForeground(Color.LIGHT_GRAY); }
        });
        return btn;
    }

    private JLabel createHelpTextHTML(String htmlBody)
    {
        // Reverted to 150px to fit perfectly in the side panel
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
        clearSuspiciousButton.addActionListener(e -> { if (clearSuspiciousAction != null) clearSuspiciousAction.run(); });
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
        refreshButton.addActionListener(e -> { if (refreshAction != null) refreshAction.run(); });
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

    public void setRefreshAction(Runnable action) { this.refreshAction = action; }
    public void setClearSuspiciousAction(Runnable action) { this.clearSuspiciousAction = action; }

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
        copyMissingButton.setText(hasMissing ? "Copy (" + currentMissingText.size() + ")" : "Copy All");

        boolean hasSuspicious = !currentSuspiciousText.isEmpty();
        copySuspiciousButton.setEnabled(hasSuspicious);
        clearSuspiciousButton.setEnabled(hasSuspicious);
        copySuspiciousButton.setText(hasSuspicious ? "Copy (" + currentSuspiciousText.size() + ")" : "Copy Names");
    }

    private void clearMissingList()
    {
        missingListContainer.removeAll();
        currentMissingText.clear();
        addDefaultMessage(missingListContainer, "Run ::absent or Refresh.");
        updateButtonsState();
        revalidateContainer(missingListContainer);
    }

    public void updateMissingList(List<PresenceChecker.PresenceMember> members)
    {
        missingListContainer.removeAll();
        currentMissingText.clear();
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.gridx = 0;
        c.gridy = 0;
        c.insets = new Insets(0, 0, 5, 0);

        if (members.isEmpty()) addDefaultMessage(missingListContainer, "No missing members.");
        else
        {
            for (PresenceChecker.PresenceMember member : members)
            {
                String rankPrefix = member.getRankName();
                String displayText = rankPrefix + member.getName();
                currentMissingText.add(displayText);
                // Highlight rank if not empty
                missingListContainer.add(createRow(displayText, !rankPrefix.isEmpty()), c);
                c.gridy++;
            }
        }
        updateButtonsState();
        revalidateContainer(missingListContainer);
    }

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

        if (names.isEmpty()) addDefaultMessage(suspiciousListContainer, "No recent suspicious activity.");
        else
        {
            for (String name : names)
            {
                suspiciousListContainer.add(createRow(name, false), c);
                c.gridy++;
            }
        }
        updateButtonsState();
        revalidateContainer(suspiciousListContainer);
    }

    private void revalidateContainer(JPanel panel) { panel.revalidate(); panel.repaint(); }

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
        if (hasRank) label.setForeground(Color.ORANGE);
        row.add(label, BorderLayout.WEST);
        row.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { row.setBackground(ColorScheme.DARKER_GRAY_HOVER_COLOR); }
            public void mouseExited(MouseEvent e) { row.setBackground(ColorScheme.DARKER_GRAY_COLOR); }
        });
        return row;
    }

    private static class DarkScrollBar extends JScrollBar
    {
        public DarkScrollBar() { setUI(new DarkScrollBarUI()); setPreferredSize(new Dimension(8, 0)); setForeground(ColorScheme.MEDIUM_GRAY_COLOR); setBackground(ColorScheme.DARK_GRAY_COLOR); }
        public void updateUI() { setUI(new DarkScrollBarUI()); }
    }

    private static class DarkScrollBarUI extends BasicScrollBarUI
    {
        private static final Color TRACK_COLOR = ColorScheme.DARK_GRAY_COLOR;
        private static final Color THUMB_COLOR = ColorScheme.MEDIUM_GRAY_COLOR;
        private static final Color THUMB_HOVER_COLOR = ColorScheme.LIGHT_GRAY_COLOR;
        protected void configureScrollBarColors() { this.thumbColor = THUMB_COLOR; this.trackColor = TRACK_COLOR; }
        protected JButton createDecreaseButton(int orientation) { return createZeroButton(); }
        protected JButton createIncreaseButton(int orientation) { return createZeroButton(); }
        private JButton createZeroButton() { JButton btn = new JButton(); btn.setPreferredSize(new Dimension(0, 0)); return btn; }
        protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) { g.setColor(TRACK_COLOR); g.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height); }
        protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) { if (thumbBounds.isEmpty() || !scrollbar.isEnabled()) return; Graphics2D g2 = (Graphics2D) g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); g2.setColor(isDragging ? THUMB_HOVER_COLOR : THUMB_COLOR); g2.fillRoundRect(thumbBounds.x, thumbBounds.y, thumbBounds.width, thumbBounds.height, 8, 8); g2.dispose(); }
    }
}