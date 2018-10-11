package org.spectra.cluster.util;

/**
 * Created by jg on 13.05.15.
 */
public final class Masses {
    /**
     * Private constructor since this class only holds
     * standard masses
     */
    private Masses() {

    }

    // Elements
    public static final float PROTON = 1.007276F;
    public static final float NEUTRON = 1.008665F;
    public static final float ELECTRON = 0.000549F;
    public static final float C13_DIFF = 1.0034F;

    // AAs
    public static final float CARBON_MONO        = 12.00000F;
    public static final float CARBON_AVG         = 12.01078F;
    public static final float HYDROGEN_MONO      = 01.00783F;
    public static final float HYDROGEN_AVG       = 01.00794F;
    public static final float OXYGEN_MONO        = 15.99491F;
    public static final float OXYGEN_AVG         = 15.99943F;
    public static final float NITROGEN_MONO      = 14.00304F;
    public static final float NITROGEN_AVG       = 14.00672F;
    public static final float SULFUR_MONO        = 31.97207F;
    public static final float SULFUR_AVG         = 32.06550F;
    public static final float PHOSPHORUS_MONO    = 30.97376F;
    public static final float PHOSPHORUS_AVG     = 30.97376F;

    public static final float WATER_MONO     = 2*HYDROGEN_MONO + OXYGEN_MONO;
    public static final float WATER_AVG      = 2*HYDROGEN_AVG + OXYGEN_AVG;

    public static final float AMMONIA_MONO   = 3*HYDROGEN_MONO + NITROGEN_MONO;
    public static final float AMMONIA_AVG    = 3*HYDROGEN_AVG + NITROGEN_AVG;

    // Chem compounds
    // see 10.1021/pr060573w
    public static final float MTA = 105.0248F;

    public static float getMonoisotopicMass(float precursorMz, int charge) {
        float floatCharge = (float) charge;

        return (precursorMz * floatCharge) - (floatCharge * PROTON);
    }

    public static float getMz(float mass, int charge) {
        float floatCharge = (float) charge;

        return (mass + (floatCharge * PROTON)) / floatCharge;
    }
}
