package org.heigit.ors.routing.pathprocessors;

import com.graphhopper.routing.ev.Border;
import com.graphhopper.routing.ev.Country;
import com.graphhopper.routing.ev.CountryOther;
import com.graphhopper.routing.ev.EnumEncodedValue;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.util.EdgeIteratorState;
import org.heigit.ors.routing.graphhopper.extensions.reader.borders.CountryBordersReader;


//TODO: the whole class seems a bit obsolete
public class BordersExtractor {
    public enum Avoid {CONTROLLED, NONE, ALL}
    private final EnumEncodedValue<Border> border;
    private final EnumEncodedValue<Country> country1;
    private final EnumEncodedValue<Country> country2;
    private final int[] avoidCountries;//TODO: use set instead?

    public BordersExtractor(EncodingManager encodingManager, int[] avoidCountries) {
      this.border = encodingManager.getEnumEncodedValue(Border.KEY, Border.class);
      this.country1 = encodingManager.getEnumEncodedValue(Country.KEY, Country.class);
      this.country2 = encodingManager.getEnumEncodedValue(CountryOther.KEY, Country.class);
      this.avoidCountries = avoidCountries;
    }

    public boolean isBorder(EdgeIteratorState edge) {
        Border borderType = border.getEnum(false, edge.getFlags());
        return borderType == Border.CONTROLLED || borderType == Border.OPEN;
    }

    public boolean isControlledBorder(EdgeIteratorState edge) {
        return border.getEnum(false, edge.getFlags()) == Border.CONTROLLED;
    }

    public boolean isOpenBorder(EdgeIteratorState edge) {
        return border.getEnum(false, edge.getFlags()) == Border.OPEN;
    }

    public boolean restrictedCountry(EdgeIteratorState edge)  {
        int startCountry = CountryBordersReader.getCountryIdByISOCode(country1.getEnum(false, edge.getFlags()).name());
        int endCountry = CountryBordersReader.getCountryIdByISOCode(country2.getEnum(false, edge.getFlags()).name());

        for (int i = 0; i < avoidCountries.length; i++) {
            if (startCountry == avoidCountries[i] || endCountry == avoidCountries[i]) {
                return true;
            }
        }
        return false;
    }
}
