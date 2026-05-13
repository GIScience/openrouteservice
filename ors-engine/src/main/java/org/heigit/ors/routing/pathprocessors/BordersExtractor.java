package org.heigit.ors.routing.pathprocessors;

import com.graphhopper.routing.ev.Border;
import com.graphhopper.routing.ev.Country;
import com.graphhopper.routing.ev.EnumEncodedValue;
import com.graphhopper.util.EdgeIteratorState;
import org.heigit.ors.routing.graphhopper.extensions.reader.borders.CountryBordersReader;


//TODO: the whole class seems a bit obsolete
public class BordersExtractor {
    public enum Avoid {CONTROLLED, NONE, ALL}
    private final EnumEncodedValue<Border> border;
    private final EnumEncodedValue<Country> country;
    private final int[] avoidCountries;//TODO: use set instead?

    public BordersExtractor(EnumEncodedValue<Border> border, EnumEncodedValue<Country> country, int[] avoidCountries) {
      this.border = border;
      this.country = country;
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
        Country countryValue = country.getEnum(false, edge.getFlags());
        int countryId = CountryBordersReader.getCountryIdByISOCode(countryValue.name());
        for (int i = 0; i < avoidCountries.length; i++) {
            if (countryId == avoidCountries[i]) {
                return true;
            }
        }
        return false;
    }
}
