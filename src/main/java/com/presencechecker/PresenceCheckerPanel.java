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
import javax.swing.SwingUtilities;
import javax.swing.Timer; // Added Import
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import net.runelite.api.FriendsChatMember;
import net.runelite.api.FriendsChatRank;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;

public class PresenceCheckerPanel extends PluginPanel
{
    private final JPanel listContainer = new JPanel();
    private final List<String> currentListText = new ArrayList<>();
    private final JButton copyButton = new JButton("Copy All");
    private final JButton refreshButton = new JButton("Refresh");
    private final JScrollPane scrollPane;
    private Runnable refreshAction;

    @Inject
    public PresenceCheckerPanel()
    {
        // Disable default PluginPanel scrollbar so we can use our custom one
        super(false);

        setLayout(new BorderLayout());
        setBackground(ColorScheme.DARK_GRAY_COLOR);
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // --- Header Panel ---
        JPanel headerPanel = new JPanel(new BorderLayout(0, 8));
        headerPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
        headerPanel.setBorder(new EmptyBorder(0, 0, 10, 0));

        JLabel titleLabel = new JLabel("Missing Members");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(FontManager.getRunescapeBoldFont());
        titleLabel.setHorizontalAlignment(JLabel.CENTER);

        // Button Container (Grid for even spacing)
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 8, 0));
        buttonPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);

        // Style the Refresh Button
        styleButton(refreshButton);
        refreshButton.setToolTipText("Re-check for missing members");
        refreshButton.addActionListener(e -> {
            if (refreshAction != null)
            {
                refreshAction.run();
            }
        });

        // Style the Copy Button
        styleButton(copyButton);
        copyButton.setToolTipText("Copy the list to clipboard");
        copyButton.addActionListener(e -> copyToClipboard());

        buttonPanel.add(refreshButton);
        buttonPanel.add(copyButton);

        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(buttonPanel, BorderLayout.SOUTH);

        // --- List Container ---
        listContainer.setLayout(new GridBagLayout());
        listContainer.setBackground(ColorScheme.DARK_GRAY_COLOR);

        // Wrapper Panel (North) - Forces items to top
        JPanel contentWrapper = new JPanel(new BorderLayout());
        contentWrapper.setBackground(ColorScheme.DARK_GRAY_COLOR);
        contentWrapper.add(listContainer, BorderLayout.NORTH);

        // --- Scroll Pane ---
        scrollPane = new JScrollPane(contentWrapper);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(new EmptyBorder(0, 0, 0, 0));
        scrollPane.setBackground(ColorScheme.DARK_GRAY_COLOR);

        // FIX: Force the ScrollBar to use our custom Dark UI
        scrollPane.setVerticalScrollBar(new DarkScrollBar());

        add(headerPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        // Updated default message to match new command
        addDefaultMessage("Run ::absent or Refresh.");
    }

    public void setRefreshAction(Runnable action)
    {
        this.refreshAction = action;
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

    public void updateList(List<FriendsChatMember> members)
    {
        listContainer.removeAll();
        currentListText.clear();

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.gridx = 0;
        c.gridy = 0;
        c.insets = new Insets(0, 0, 5, 0);

        if (members.isEmpty())
        {
            addDefaultMessage("No missing members.");
            copyButton.setEnabled(false);
            copyButton.setText("Copy All");
        }
        else
        {
            copyButton.setEnabled(true);
            copyButton.setText("Copy (" + members.size() + ")");

            for (FriendsChatMember member : members)
            {
                String rankPrefix = getRankPrefix(member.getRank());
                String displayText = rankPrefix + member.getName();
                currentListText.add(displayText);

                listContainer.add(createRow(displayText, !rankPrefix.isEmpty()), c);
                c.gridy++;
            }
        }

        JPanel filler = new JPanel();
        filler.setBackground(ColorScheme.DARK_GRAY_COLOR);
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        listContainer.add(filler, c);

        listContainer.revalidate();
        listContainer.repaint();
    }

    private void copyToClipboard()
    {
        if (currentListText.isEmpty()) return;

        String clipboardString = String.join("\n", currentListText);
        StringSelection stringSelection = new StringSelection(clipboardString);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringSelection, null);

        String originalText = copyButton.getText();
        copyButton.setText("Copied!");

        // FIX: Replaced Thread/Sleep with Swing Timer
        Timer timer = new Timer(2000, e -> copyButton.setText(originalText));
        timer.setRepeats(false);
        timer.start();
    }

    private void addDefaultMessage(String message)
    {
        listContainer.removeAll();
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.gridx = 0;
        c.gridy = 0;

        JLabel emptyLabel = new JLabel(message);
        emptyLabel.setForeground(Color.GRAY);
        emptyLabel.setHorizontalAlignment(JLabel.CENTER);
        emptyLabel.setBorder(new EmptyBorder(20, 0, 20, 0));

        listContainer.add(emptyLabel, c);
        listContainer.revalidate();
        listContainer.repaint();
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

            // Paint rounded thumb
            g2.fillRoundRect(thumbBounds.x, thumbBounds.y, thumbBounds.width, thumbBounds.height, 8, 8);
            g2.dispose();
        }
    }
}
