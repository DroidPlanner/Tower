package co.aerobotics.android.data;

import java.util.Comparator;

import co.aerobotics.android.dialogs.SearchBoundariesDialog;

/**
 * Created by michaelwootton on 10/11/17.
 */

public class NameWithId implements Comparable<NameWithId>{

    public String name;
    public String id;

    public NameWithId(String name, String id) {

        this.name = name;
        this.id = id;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int compareTo(NameWithId o) {
        return Comparators.NAME.compare(this, o);
    }


    public static class Comparators {

        public static Comparator<NameWithId> NAME = new Comparator<NameWithId>() {
            @Override
            public int compare(NameWithId o1, NameWithId o2) {
                return o1.name.toLowerCase().compareTo(o2.name.toLowerCase());
            }
        };
    }
}
