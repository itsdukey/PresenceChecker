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
    // --- HELPERS & ENUMS ---

    @ConfigItem(
            keyName = "showPanelTutorial",
            name = "Show Tutorial",
            description = "Whether to show the help tutorial in the panel.",
            hidden = true
    )
    default boolean showPanelTutorial() { return true; }

    enum ChatMode
    {
        FRIENDS_CHAT,
        CLAN_CHAT,
        GUEST_CLAN_CHAT
    }

    enum ClanRankOption
    {
        NONE("None"),

        // --- SYSTEM RANKS ---
        _SEC_SYSTEM("--- System Ranks ---"),
        OWNER("Owner"), DEPUTY_OWNER("Deputy Owner"), ADMINISTRATOR("Administrator"),
        JMOD("JMod"), GUEST("Guest"),

        // --- ARMY RANKS 1 ---
        _SEC_ARMY1("--- Army Ranks 1 ---"),
        DOGSBODY("Dogsbody"), MINION("Minion"), RECRUIT("Recruit"), PAWN("Pawn"),
        PRIVATE("Private"), CORPORAL("Corporal"), NOVICE("Novice"), SERGEANT("Sergeant"), CADET("Cadet"),

        // --- ARMY RANKS 2 ---
        _SEC_ARMY2("--- Army Ranks 2 ---"),
        PAGE("Page"), NOBLE("Noble"), ADEPT("Adept"), LEGIONNAIRE("Legionnaire"),
        LIEUTENANT("Lieutenant"), PROSELYTE("Proselyte"), CAPTAIN("Captain"),
        MAJOR("Major"), GENERAL("General"), MASTER("Master"),

        // --- ARMY RANKS 3 ---
        _SEC_ARMY3("--- Army Ranks 3 ---"),
        OFFICER("Officer"), COMMANDER("Commander"), COLONEL("Colonel"),
        BRIGADIER("Brigadier"), ADMIRAL("Admiral"), MARSHAL("Marshal"),

        // --- GEMSTONES ---
        _SEC_GEMS("--- Gemstones ---"),
        OPAL("Opal"), JADE("Jade"), RED_TOPAZ("Red Topaz"), SAPPHIRE("Sapphire"),
        EMERALD("Emerald"), RUBY("Ruby"), DIAMOND("Diamond"), DRAGONSTONE("Dragonstone"),
        ONYX("Onyx"), ZENYTE("Zenyte"),

        // --- NON-HUMAN ---
        _SEC_NONHUMAN("--- Non-human ---"),
        KITTEN("Kitten"), BOB("Bob"), WILY("Wily"), HELLCAT("Hellcat"), SKULLED("Skulled"),
        GOBLIN("Goblin"), BEAST("Beast"), IMP("Imp"), GNOME_CHILD("Gnome Child"),
        GNOME_ELDER("Gnome Elder"), SHORT_GREEN_GUY("Short Green Guy"),

        // --- REGIONS ---
        _SEC_REGIONS("--- Regions ---"),
        MISTHALINIAN("Misthalinian"), KARAMJAN("Karamjan"), ASGARNIAN("Asgarnian"),
        KHARIDIAN("Kharidian"), MORYTANIAN("Morytanian"), WILD("Wild"), KANDARIN("Kandarin"),
        FREMENNIK("Fremennik"), TIRANNIAN("Tirannian"),

        // --- RELIGIONS ---
        _SEC_RELIGIONS("--- Religions ---"),
        BRASSICAN("Brassican"), SARADOMINIST("Saradominist"), GUTHIXIAN("Guthixian"),
        ZAMORAKIAN("Zamorakian"), SERENIST("Serenist"), BANDOSIAN("Bandosian"),
        ZAROSIAN("Zarosian"), ARMADYLEAN("Armadylean"), XERICIAN("Xerician"),

        // --- RUNE SYMBOLS ---
        _SEC_RUNES("--- Rune Symbols ---"),
        AIR("Air"), MIND("Mind"), WATER("Water"), EARTH("Earth"), FIRE("Fire"),
        BODY("Body"), COSMIC("Cosmic"), CHAOS("Chaos"), NATURE("Nature"), LAW("Law"),
        DEATH("Death"), ASTRAL("Astral"), BLOOD("Blood"), SOUL("Soul"), WRATH("Wrath"),

        // --- TREES ---
        _SEC_TREES("--- Trees ---"),
        DISEASED("Diseased"), PINE("Pine"), WINTUMBER("Wintumber"), OAK("Oak"),
        WILLOW("Willow"), MAPLE("Maple"), YEW("Yew"), BLISTERWOOD("Blisterwood"), MAGIC("Magic"),

        // --- SKILLS ---
        _SEC_SKILLS("--- Skills ---"),
        ATTACKER("Attacker"), ENFORCER("Enforcer"), DEFENDER("Defender"), RANGER("Ranger"),
        PRIEST("Priest"), MAGICIAN("Magician"), RUNECRAFTER("Runecrafter"), MEDIC("Medic"),
        ATHLETE("Athlete"), HERBOLOGIST("Herbologist"), THIEF("Thief"), CRAFTER("Crafter"),
        FLETCHER("Fletcher"), MINER("Miner"), SMITH("Smith"), FISHER("Fisher"), COOK("Cook"),
        FIREMAKER("Firemaker"), LUMBERJACK("Lumberjack"), SLAYER("Slayer"), FARMER("Farmer"),
        CONSTRUCTOR("Constructor"), HUNTER("Hunter"), SKILLER("Skiller"), COMPETITOR("Competitor"),

        // --- CAPES ---
        _SEC_CAPES("--- Capes ---"),
        HOLY("Holy"), UNHOLY("Unholy"), NATURAL("Natural"), SAGE("Sage"), DESTROYER("Destroyer"),
        MEDIATOR("Mediator"), LEGEND("Legend"), MYTH("Myth"), TZTOK("TzTok"), TZKAL("TzKal"), MAXED("Maxed"),

        // --- SKILLING-FOCUSED ---
        _SEC_SKILLING("--- Skilling-focused ---"),
        ANCHOR("Anchor"), APOTHECARY("Apothecary"), MERCHANT("Merchant"),
        FEEDER("Feeder"), HARPOON("Harpoon"), CARRY("Carry"),

        // --- COMBAT-FOCUSED ---
        _SEC_COMBAT("--- Combat-focused ---"),
        ARCHER("Archer"), BATTLEMAGE("Battlemage"), ARTILLERY("Artillery"), INFANTRY("Infantry"),
        SMITER("Smiter"), LOOTER("Looter"), SAVIOUR("Saviour"), SNIPER("Sniper"),
        CRUSADER("Crusader"), SPELLCASTER("Spellcaster"),

        // --- MISCELLANEOUS 1 ---
        _SEC_MISC1("--- Miscellaneous 1 ---"),
        MENTOR("Mentor"), PREFECT("Prefect"), LEADER("Leader"), SUPERVISOR("Supervisor"),
        SUPERIOR("Superior"), EXECUTIVE("Executive"), SENATOR("Senator"), MONARCH("Monarch"),
        SCAVENGER("Scavenger"), LABOURER("Labourer"), WORKER("Worker"), FORAGER("Forager"),
        HOARDER("Hoarder"), PROSPECTOR("Prospector"), GATHERER("Gatherer"), COLLECTOR("Collector"),
        BRONZE("Bronze"), IRON("Iron"), STEEL("Steel"), GOLD("Gold"), MITHRIL("Mithril"),
        ADAMANT("Adamant"), RUNE("Rune"), DRAGON("Dragon"), PROTECTOR("Protector"),
        BULWARK("Bulwark"), JUSTICIAR("Justiciar"), SENTRY("Sentry"), GUARDIAN("Guardian"),
        WARDEN("Warden"), VANGUARD("Vanguard"), TEMPLAR("Templar"), SQUIRE("Squire"),
        DUELLIST("Duellist"), STRIKER("Striker"), NINJA("Ninja"), INQUISITOR("Inquisitor"),
        EXPERT("Expert"), KNIGHT("Knight"), PALADIN("Paladin"), GOON("Goon"), BRAWLER("Brawler"),
        BRUISER("Bruiser"), SCOURGE("Scourge"), FIGHTER("Fighter"), WARRIOR("Warrior"),
        BARBARIAN("Barbarian"), BERSERKER("Berserker"), STAFF("Staff"), CREW("Crew"),
        HELPER("Helper"), MODERATOR("Moderator"), SHERIFF("Sheriff"),

        // --- MISCELLANEOUS 2 ---
        _SEC_MISC2("--- Miscellaneous 2 ---"),
        RED("Red"), ORANGE("Orange"), YELLOW("Yellow"), GREEN("Green"), BLUE("Blue"),
        PURPLE("Purple"), PINK("Pink"), GREY("Grey"), WIZARD("Wizard"), TRICKSTER("Trickster"),
        ILLUSIONIST("Illusionist"), SUMMONER("Summoner"), NECROMANCER("Necromancer"),
        WARLOCK("Warlock"), WITCH("Witch"), SEER("Seer"), ASSASSIN("Assassin"),
        CUTPURSE("Cutpurse"), BANDIT("Bandit"), SCOUT("Scout"), BURGLAR("Burglar"),
        ROGUE("Rogue"), SMUGGLER("Smuggler"), BRIGAND("Brigand"), ORACLE("Oracle"),
        PURE("Pure"), CHAMPION("Champion"), EPIC("Epic"), MYSTIC("Mystic"), HERO("Hero"),
        TRIALIST("Trialist"), DEFILER("Defiler"), SCHOLAR("Scholar"), COUNCILLOR("Councillor"),
        RECRUITER("Recruiter"), LEARNER("Learner"), SCRIBE("Scribe"), ASSISTANT("Assistant"),
        TEACHER("Teacher"), COORDINATOR("Coordinator"), WALKER("Walker"),
        SPEED_RUNNER("Speed-Runner"), WANDERER("Wanderer"), PILGRIM("Pilgrim"),
        VAGRANT("Vagrant"), RECORD_CHASER("Record-chaser"), RACER("Racer"), STRIDER("Strider"),
        DOCTOR("Doctor"), NURSE("Nurse"), DRUID("Druid"), HEALER("Healer"), ZEALOT("Zealot"),
        CLERIC("Cleric"), SHAMAN("Shaman"), THERAPIST("Therapist"), GAMER("Gamer"),
        ADVENTURER("Adventurer"), EXPLORER("Explorer"), ACHIEVER("Achiever"), QUESTER("Quester"),
        RAIDER("Raider"), COMPLETIONIST("Completionist"), ELITE("Elite"),
        FIRESTARTER("Firestarter"), SPECIALIST("Specialist"), BURNT("Burnt"),
        PYROMANCER("Pyromancer"), PRODIGY("Prodigy"), IGNITOR("Ignitor"),
        ARTISAN("Artisan"), LEGACY("Legacy"),

        // --- SUIT SYMBOLS (Added these back as they are common) ---
        SPADE("Spade"), CLUB("Club"), HEART("Heart"), DIAMOND_SUIT("Diamond"), BOLT("Bolt"), PROBOSCIS("Proboscis"), SMILEY("Smiley");

        private final String title;

        ClanRankOption(String title)
        {
            this.title = title;
        }

        @Override
        public String toString()
        {
            return title;
        }
    }

    // --- SECTIONS ---

    @ConfigSection(
            name = "General Settings",
            description = "General configuration for highlighting.",
            position = 0,
            closedByDefault = false
    )
    String generalSettings = "generalSettings";

    @ConfigSection(
            name = "Friends Chat Filters",
            description = "Filters for the green Friends Chat channel.",
            position = 1,
            closedByDefault = true
    )
    String friendsChatFilter = "friendsChatFilter";

    @ConfigSection(
            name = "Clan Chat Filters",
            description = "Filters for the orange Clan Chat channel (24 Custom Slots).",
            position = 2,
            closedByDefault = true
    )
    String clanChatFilter = "clanChatFilter";

    @ConfigSection(
            name = "Overlay Settings",
            description = "Configuration for the screen overlay HUD.",
            position = 3,
            closedByDefault = true
    )
    String overlaySettings = "overlaySettings";

    @ConfigSection(
            name = "Suspicious Activity",
            description = "Tracker for players quickly joining and leaving.",
            position = 4,
            closedByDefault = true
    )
    String suspiciousSettings = "suspiciousSettings";

    // --- GENERAL SETTINGS ---

    @ConfigItem(
            keyName = "chatMode",
            name = "Target Chat",
            description = "Which chat channel to monitor for presence and suspicious activity.",
            position = 0,
            section = generalSettings
    )
    default ChatMode chatMode()
    {
        return ChatMode.FRIENDS_CHAT;
    }

    @ConfigItem(
            keyName = "highlightColor",
            name = "Highlight Color",
            description = "The color to highlight missing members in the clan chat list.",
            position = 1,
            section = generalSettings
    )
    default Color getHighlightColor()
    {
        return new Color(128, 0, 128);
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
        return 10;
    }

    @ConfigItem(
            keyName = "autoUpdatePanel",
            name = "Auto-Update Panel",
            description = "If enabled, the side panel updates automatically. If disabled, it only updates on Refresh/::absent.",
            position = 3,
            section = generalSettings
    )
    default boolean autoUpdatePanel()
    {
        return false;
    }

    // --- FRIENDS CHAT FILTERS ---

    @ConfigItem(keyName = "filterSelf", name = "Exclude Self", description = "Exclude yourself.", position = 0, section = friendsChatFilter)
    default boolean filterSelf() { return true; }

    @ConfigItem(keyName = "hideOwner", name = "Hide Owners", description = "Hide Owners in Friends Chat.", position = 1, section = friendsChatFilter)
    default boolean hideOwner() { return false; }

    @ConfigItem(keyName = "hideGeneral", name = "Hide Generals", description = "Hide Generals in Friends Chat.", position = 2, section = friendsChatFilter)
    default boolean hideGeneral() { return false; }

    @ConfigItem(keyName = "hideCaptain", name = "Hide Captains", description = "Hide Captains in Friends Chat.", position = 3, section = friendsChatFilter)
    default boolean hideCaptain() { return false; }

    @ConfigItem(keyName = "hideLieutenant", name = "Hide Lieutenants", description = "Hide Lieutenants in Friends Chat.", position = 4, section = friendsChatFilter)
    default boolean hideLieutenant() { return false; }

    @ConfigItem(keyName = "hideSergeant", name = "Hide Sergeants", description = "Hide Sergeants in Friends Chat.", position = 5, section = friendsChatFilter)
    default boolean hideSergeant() { return false; }

    @ConfigItem(keyName = "hideCorporal", name = "Hide Corporals", description = "Hide Corporals in Friends Chat.", position = 6, section = friendsChatFilter)
    default boolean hideCorporal() { return false; }

    @ConfigItem(keyName = "hideRecruit", name = "Hide Recruits", description = "Hide Recruits in Friends Chat.", position = 7, section = friendsChatFilter)
    default boolean hideRecruit() { return false; }

    @ConfigItem(keyName = "hideFriend", name = "Hide Friends", description = "Hide Friends in Friends Chat.", position = 8, section = friendsChatFilter)
    default boolean hideFriend() { return false; }

    @ConfigItem(keyName = "hideGuest", name = "Hide Guests", description = "Hide Guests in Friends Chat.", position = 9, section = friendsChatFilter)
    default boolean hideGuest() { return false; }

    // --- CLAN CHAT FILTERS (24 DROPDOWNS) ---

    @ConfigItem(keyName = "filterRank1", name = "Filter Rank 1", description = "Select a rank to hide.", position = 1, section = clanChatFilter)
    default ClanRankOption filterRank1() { return ClanRankOption.NONE; }

    @ConfigItem(keyName = "filterRank2", name = "Filter Rank 2", description = "Select a rank to hide.", position = 2, section = clanChatFilter)
    default ClanRankOption filterRank2() { return ClanRankOption.NONE; }

    @ConfigItem(keyName = "filterRank3", name = "Filter Rank 3", description = "Select a rank to hide.", position = 3, section = clanChatFilter)
    default ClanRankOption filterRank3() { return ClanRankOption.NONE; }

    @ConfigItem(keyName = "filterRank4", name = "Filter Rank 4", description = "Select a rank to hide.", position = 4, section = clanChatFilter)
    default ClanRankOption filterRank4() { return ClanRankOption.NONE; }

    @ConfigItem(keyName = "filterRank5", name = "Filter Rank 5", description = "Select a rank to hide.", position = 5, section = clanChatFilter)
    default ClanRankOption filterRank5() { return ClanRankOption.NONE; }

    @ConfigItem(keyName = "filterRank6", name = "Filter Rank 6", description = "Select a rank to hide.", position = 6, section = clanChatFilter)
    default ClanRankOption filterRank6() { return ClanRankOption.NONE; }

    @ConfigItem(keyName = "filterRank7", name = "Filter Rank 7", description = "Select a rank to hide.", position = 7, section = clanChatFilter)
    default ClanRankOption filterRank7() { return ClanRankOption.NONE; }

    @ConfigItem(keyName = "filterRank8", name = "Filter Rank 8", description = "Select a rank to hide.", position = 8, section = clanChatFilter)
    default ClanRankOption filterRank8() { return ClanRankOption.NONE; }

    @ConfigItem(keyName = "filterRank9", name = "Filter Rank 9", description = "Select a rank to hide.", position = 9, section = clanChatFilter)
    default ClanRankOption filterRank9() { return ClanRankOption.NONE; }

    @ConfigItem(keyName = "filterRank10", name = "Filter Rank 10", description = "Select a rank to hide.", position = 10, section = clanChatFilter)
    default ClanRankOption filterRank10() { return ClanRankOption.NONE; }

    @ConfigItem(keyName = "filterRank11", name = "Filter Rank 11", description = "Select a rank to hide.", position = 11, section = clanChatFilter)
    default ClanRankOption filterRank11() { return ClanRankOption.NONE; }

    @ConfigItem(keyName = "filterRank12", name = "Filter Rank 12", description = "Select a rank to hide.", position = 12, section = clanChatFilter)
    default ClanRankOption filterRank12() { return ClanRankOption.NONE; }

    @ConfigItem(keyName = "filterRank13", name = "Filter Rank 13", description = "Select a rank to hide.", position = 13, section = clanChatFilter)
    default ClanRankOption filterRank13() { return ClanRankOption.NONE; }

    @ConfigItem(keyName = "filterRank14", name = "Filter Rank 14", description = "Select a rank to hide.", position = 14, section = clanChatFilter)
    default ClanRankOption filterRank14() { return ClanRankOption.NONE; }

    @ConfigItem(keyName = "filterRank15", name = "Filter Rank 15", description = "Select a rank to hide.", position = 15, section = clanChatFilter)
    default ClanRankOption filterRank15() { return ClanRankOption.NONE; }

    @ConfigItem(keyName = "filterRank16", name = "Filter Rank 16", description = "Select a rank to hide.", position = 16, section = clanChatFilter)
    default ClanRankOption filterRank16() { return ClanRankOption.NONE; }

    @ConfigItem(keyName = "filterRank17", name = "Filter Rank 17", description = "Select a rank to hide.", position = 17, section = clanChatFilter)
    default ClanRankOption filterRank17() { return ClanRankOption.NONE; }

    @ConfigItem(keyName = "filterRank18", name = "Filter Rank 18", description = "Select a rank to hide.", position = 18, section = clanChatFilter)
    default ClanRankOption filterRank18() { return ClanRankOption.NONE; }

    @ConfigItem(keyName = "filterRank19", name = "Filter Rank 19", description = "Select a rank to hide.", position = 19, section = clanChatFilter)
    default ClanRankOption filterRank19() { return ClanRankOption.NONE; }

    @ConfigItem(keyName = "filterRank20", name = "Filter Rank 20", description = "Select a rank to hide.", position = 20, section = clanChatFilter)
    default ClanRankOption filterRank20() { return ClanRankOption.NONE; }

    @ConfigItem(keyName = "filterRank21", name = "Filter Rank 21", description = "Select a rank to hide.", position = 21, section = clanChatFilter)
    default ClanRankOption filterRank21() { return ClanRankOption.NONE; }

    @ConfigItem(keyName = "filterRank22", name = "Filter Rank 22", description = "Select a rank to hide.", position = 22, section = clanChatFilter)
    default ClanRankOption filterRank22() { return ClanRankOption.NONE; }

    @ConfigItem(keyName = "filterRank23", name = "Filter Rank 23", description = "Select a rank to hide.", position = 23, section = clanChatFilter)
    default ClanRankOption filterRank23() { return ClanRankOption.NONE; }

    @ConfigItem(keyName = "filterRank24", name = "Filter Rank 24", description = "Select a rank to hide.", position = 24, section = clanChatFilter)
    default ClanRankOption filterRank24() { return ClanRankOption.NONE; }

    // --- OVERLAY SETTINGS ---
    @ConfigItem(keyName = "enableOverlay", name = "Enable Overlay", description = "Show HUD.", position = 0, section = overlaySettings)
    default boolean enableOverlay() { return true; }
    @ConfigItem(keyName = "showOverlayNames", name = "Show Names", description = "List names.", position = 1, section = overlaySettings)
    default boolean showOverlayNames() { return true; }
    @ConfigItem(keyName = "overlayNamesLimit", name = "Names Limit", description = "Max names.", position = 2, section = overlaySettings)
    default int overlayNamesLimit() { return 5; }

    // --- SUSPICIOUS SETTINGS ---
    @ConfigItem(keyName = "enableSuspiciousTracking", name = "Enable Tracking", description = "Tracking on/off.", position = 0, section = suspiciousSettings)
    default boolean enableSuspiciousTracking() { return true; }
    @Range(min = 100, max = 10000)
    @ConfigItem(keyName = "suspiciousThreshold", name = "Suspicious Time (ms)", description = "Time threshold.", position = 1, section = suspiciousSettings)
    default int suspiciousThreshold() { return 4000; }
    @ConfigItem(keyName = "friendlyWhitelist", name = "Friendly Whitelist", description = "Names to never flag.", position = 14, section = suspiciousSettings)
    default String friendlyWhitelist() { return ""; }
    @ConfigItem(keyName = "blacklistedNames", name = "Enemy Blacklist", description = "Instant alert list.", position = 15, section = suspiciousSettings)
    default String blacklistedNames() { return ""; }
    @ConfigItem(keyName = "suspiciousWarningThreshold", name = "Warning Threshold", description = "Events before warning.", position = 11, section = suspiciousSettings)
    default int suspiciousWarningThreshold() { return 3; }
    @ConfigItem(keyName = "suspiciousWarningColor", name = "Warning Color", description = "Chat message color.", position = 12, section = suspiciousSettings)
    default Color suspiciousWarningColor() { return Color.RED; }
    @ConfigItem(keyName = "suspiciousNotification", name = "Suspicious Alert", description = "Notification type.", position = 13, section = suspiciousSettings)
    default Notification suspiciousNotification() { return Notification.OFF; }

    // Suspicious Toggles
    @ConfigItem(keyName = "susHideOwner", name = "Hide Owners", description = "Ignore Owners.", position = 2, section = suspiciousSettings)
    default boolean susHideOwner() { return true; }
    @ConfigItem(keyName = "susHideGeneral", name = "Hide Generals", description = "Ignore Generals.", position = 3, section = suspiciousSettings)
    default boolean susHideGeneral() { return true; }
    @ConfigItem(keyName = "susHideCaptain", name = "Hide Captains", description = "Ignore Captains.", position = 4, section = suspiciousSettings)
    default boolean susHideCaptain() { return true; }
    @ConfigItem(keyName = "susHideLieutenant", name = "Hide Lieutenants", description = "Ignore Lieutenants.", position = 5, section = suspiciousSettings)
    default boolean susHideLieutenant() { return true; }
    @ConfigItem(keyName = "susHideSergeant", name = "Hide Sergeants", description = "Ignore Sergeants.", position = 6, section = suspiciousSettings)
    default boolean susHideSergeant() { return true; }
    @ConfigItem(keyName = "susHideCorporal", name = "Hide Corporals", description = "Ignore Corporals.", position = 7, section = suspiciousSettings)
    default boolean susHideCorporal() { return false; }
    @ConfigItem(keyName = "susHideRecruit", name = "Hide Recruits", description = "Ignore Recruits.", position = 8, section = suspiciousSettings)
    default boolean susHideRecruit() { return false; }
    @ConfigItem(keyName = "susHideFriend", name = "Hide Friends", description = "Ignore Friends.", position = 9, section = suspiciousSettings)
    default boolean susHideFriend() { return false; }
    @ConfigItem(keyName = "susHideGuest", name = "Hide Guests", description = "Ignore Guests.", position = 10, section = suspiciousSettings)
    default boolean susHideGuest() { return false; }
}