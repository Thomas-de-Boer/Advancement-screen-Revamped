package thomas.advancementstracker.client;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethodStage;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * All colors of the screen. Each theme is one object.
 * The order of the values in each theme MUST match the order of the fields below.
 * Note: drawBevelPanel currently only uses frameHi, wellLo and plateHi (the light/dark top-left edge).
 * The other bevel colors are kept for when the bottom/right edge lines are switched back on.
 */
public record Theme(
        String name,
        // Surfaces
        int frame, int well, int row, int rowSelected, int tab, int tabSelected,
        int button, int buttonHover, int slotBg,
        // Bevel edges
        int frameHi, int frameLo, int wellHi, int wellLo, int plateHi, int plateLo,
        // Borders
        int frameBorder, int border,
        // Text
        int title, int label, int tabText, int tabSelectedText, int buttonText, int progressLabel, int muted,
        // Status
        int done, int progress, int none,
        // Progress bar
        int progressBg, int progressTrackRing,
        // Favorite star
        int favorite, int favoriteOutline, int favoriteEmptyFill, int favoriteEmpty,
        // Scrollbar
        int scrollbarTrack, int scrollbarThumb
) {

    public static final Theme VANILLA = new Theme(
            "Vanilla",
            0xFFC6C6C6, // frame
            0xFF262626, // well
            0xFF3D3D3D, // row
            0xFF4A4A6E, // rowSelected
            0xFF6E6E6E, // tab
            0xFFE2E2E2, // tabSelected
            0xFF6E6E6E, // button
            0xFF8B8B8B, // buttonHover
            0xFF171717, // slotBg
            0xFFFFFFFF, // frameHi
            0xFF555555, // frameLo
            0xFFFFFFFF, // wellHi
            0xFF373737, // wellLo
            0xFFA5A5A5, // plateHi
            0xFF1C1C1C, // plateLo
            0xFF000000, // frameBorder
            0xFF000000, // border
            0xFF404040, // title
            0xFFE0E0E0, // label
            0xFFFFFFFF, // tabText
            0xFF404040, // tabSelectedText
            0xFFFFFFFF, // buttonText
            0xFFFFFFFF, // progressLabel
            0xFFAAAAAA, // muted
            0xFF55FF55, // done
            0xFFFFFF55, // progress
            0xFFD0D0D0, // none
            0xFF151515, // progressBg
            0xFF3A3A3A, // progressTrackRing
            0xFFFFD700, // favorite
            0xFF806000, // favoriteOutline
            0xFF222222, // favoriteEmptyFill
            0xFF555555, // favoriteEmpty
            0xFF171717, // scrollbarTrack
            0xFF8B8B8B  // scrollbarThumb
    );

    public static final Theme LAPIS = new Theme(
            "Lapis",
            0xFF172B52, // frame
            0xFF091326, // well
            0xFF10203D, // row
            0xFF28529A, // rowSelected
            0xFF1B3970, // tab
            0xFF3D73C9, // tabSelected
            0xFF1B3970, // button
            0xFF2B4F86, // buttonHover
            0xFF070E1C, // slotBg
            0xFF34568F, // frameHi
            0xFF0E1B32, // frameLo
            0xFF29497D, // wellHi
            0xFF060C17, // wellLo
            0xFF4266A0, // plateHi
            0xFF0B162A, // plateLo
            0xFF030609, // frameBorder
            0xFF04070B, // border
            0xFFE4ECFF, // title
            0xFFD0DCF4, // label
            0xFFF4F7FF, // tabText
            0xFFFFFFFF, // tabSelectedText
            0xFFF0F5FF, // buttonText
            0xFFEAF1FF, // progressLabel
            0xFF8294B5, // muted
            0xFF5EE08A, // done
            0xFFFFC857, // progress
            0xFFC5D0E5, // none
            0xFF0D1729, // progressBg
            0xFF26477D, // progressTrackRing
            0xFFFFC83D, // favorite
            0xFF806000, // favoriteOutline
            0xFF111C30, // favoriteEmptyFill
            0xFF48628B, // favoriteEmpty
            0xFF080F1D, // scrollbarTrack
            0xFF4169A5  // scrollbarThumb
    );


    public static final Theme REDSTONE = new Theme(
            "Redstone",
            0xFF4A160E, // frame
            0xFF1C0703, // well
            0xFF350D07, // row
            0xFF9E210E, // rowSelected
            0xFF67180B, // tab
            0xFFE62008, // tabSelected
            0xFF67180B, // button
            0xFF8B2814, // buttonHover
            0xFF130503, // slotBg
            0xFF81311B, // frameHi
            0xFF2B0A05, // frameLo
            0xFF6D1D0C, // wellHi
            0xFF100402, // wellLo
            0xFF96351A, // plateHi
            0xFF210805, // plateLo
            0xFF080201, // frameBorder
            0xFF090301, // border
            0xFFFFE2D8, // title
            0xFFF0CFC6, // label
            0xFFFFF2ED, // tabText
            0xFFFFFFFF, // tabSelectedText
            0xFFFFEEE8, // buttonText
            0xFFFFE8E0, // progressLabel
            0xFFB58D83, // muted
            0xFF5EE08A, // done
            0xFFFFB52E, // progress
            0xFFDCC5BF, // none
            0xFF200804, // progressBg
            0xFF711D0D, // progressTrackRing
            0xFFFFC83D, // favorite
            0xFF806000, // favoriteOutline
            0xFF2C0D07, // favoriteEmptyFill
            0xFF71362A, // favoriteEmpty
            0xFF110402, // scrollbarTrack
            0xFFB53A1C  // scrollbarThumb
    );


    public static final Theme AMETHYST = new Theme(
            "Amethyst",
            0xFF28213A, // frame
            0xFF15121F, // well
            0xFF211A30, // row
            0xFF503B78, // rowSelected
            0xFF382B58, // tab
            0xFF9B6DFF, // tabSelected
            0xFF382B58, // button
            0xFF554078, // buttonHover
            0xFF0F0C16, // slotBg
            0xFF5B4B78, // frameHi
            0xFF1C1729, // frameLo
            0xFF45345F, // wellHi
            0xFF0D0A12, // wellLo
            0xFF625080, // plateHi
            0xFF17111F, // plateLo
            0xFF08060B, // frameBorder
            0xFF09070C, // border
            0xFFF0E9FF, // title
            0xFFE4DCF5, // label
            0xFFFFFFFF, // tabText
            0xFFFFFFFF, // tabSelectedText
            0xFFFFFFFF, // buttonText
            0xFFFFFFFF, // progressLabel
            0xFFA99DBB, // muted
            0xFF67E8A5, // done
            0xFFB58CFF, // progress
            0xFFD5CEE2, // none
            0xFF181322, // progressBg
            0xFF493667, // progressTrackRing
            0xFFFFC857, // favorite
            0xFF806000, // favoriteOutline
            0xFF21172B, // favoriteEmptyFill
            0xFF604A70, // favoriteEmpty
            0xFF100C16, // scrollbarTrack
            0xFF8066A8  // scrollbarThumb
    );

    public static final Theme RESIN = new Theme(
            "Resin",
            0xFF3B1D0B, // frame
            0xFF160B04, // well
            0xFF2A1407, // row
            0xFF8A3D0C, // rowSelected
            0xFF522508, // tab
            0xFFFC7812, // tabSelected
            0xFF522508, // button
            0xFF75400F, // buttonHover
            0xFF110703, // slotBg
            0xFF754116, // frameHi
            0xFF251105, // frameLo
            0xFF5D2C0A, // wellHi
            0xFF0D0602, // wellLo
            0xFF814919, // plateHi
            0xFF1A0B03, // plateLo
            0xFF080402, // frameBorder
            0xFF090402, // border
            0xFFFFE4C7, // title
            0xFFEBCBAA, // label
            0xFFFFF4E5, // tabText
            0xFFFFFFFF, // tabSelectedText
            0xFFFFF1DC, // buttonText
            0xFFFFEBD7, // progressLabel
            0xFFA88B70, // muted
            0xFF62D68A, // done
            0xFFE96B18, // progress
            0xFFD8C4AD, // none
            0xFF1D0D04, // progressBg
            0xFF66300B, // progressTrackRing
            0xFFFFC43D, // favorite
            0xFF8A5D0C, // favoriteOutline
            0xFF291507, // favoriteEmptyFill
            0xFF6B4928, // favoriteEmpty
            0xFF100602, // scrollbarTrack
            0xFFA75A18  // scrollbarThumb
    );


    public static final Theme EMERALD = new Theme(
            "Emerald",
            0xFF12351F, // frame
            0xFF07150C, // well
            0xFF0D2918, // row
            0xFF176B38, // rowSelected
            0xFF124A2A, // tab
            0xFF17C544, // tabSelected
            0xFF124A2A, // button
            0xFF1B6637, // buttonHover
            0xFF061009, // slotBg
            0xFF286341, // frameHi
            0xFF0A2113, // frameLo
            0xFF1C5131, // wellHi
            0xFF040C07, // wellLo
            0xFF34734D, // plateHi
            0xFF08190E, // plateLo
            0xFF030805, // frameBorder
            0xFF040906, // border
            0xFFDDFBE7, // title
            0xFFC5E8D0, // label
            0xFFF0FFF4, // tabText
            0xFFFFFFFF, // tabSelectedText
            0xFFE9FFF0, // buttonText
            0xFFE5FFEC, // progressLabel
            0xFF82A992, // muted
            0xFF5EE08A, // done
            0xFF22D85A, // progress
            0xFFC5DCCB, // none
            0xFF0A1D10, // progressBg
            0xFF1B5A32, // progressTrackRing
            0xFFFFC857, // favorite
            0xFF806000, // favoriteOutline
            0xFF102817, // favoriteEmptyFill
            0xFF39704D, // favoriteEmpty
            0xFF050D08, // scrollbarTrack
            0xFF3E9B60  // scrollbarThumb
    );



    /** All themes, in the order the button cycles through them. */
    public static final List<Theme> ALL = List.of(VANILLA, LAPIS, REDSTONE, AMETHYST, EMERALD, RESIN);

    public Theme next() {
        int index = ALL.indexOf(this);
        return ALL.get((index + 1) % ALL.size());
    }

    private static Path file() {
        return FabricLoader.getInstance().getConfigDir().resolve("advancementstracker-theme.txt");
    }

    /** Loads the saved theme. Falls back to BLUE if nothing is saved yet. */
    public static Theme load() {
        try {
            Path path = file();
            if (Files.exists(path)) {
                String saved = Files.readString(path).trim();
                for (Theme theme : ALL) {
                    if (theme.name().equals(saved)) return theme;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return VANILLA;
    }

    public void save() {
        try {
            Files.writeString(file(), name());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}