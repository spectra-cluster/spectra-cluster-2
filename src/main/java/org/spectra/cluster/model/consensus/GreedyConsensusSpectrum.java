package org.spectra.cluster.model.consensus;

import org.spectra.cluster.model.spectra.BinaryPeak;
import org.spectra.cluster.model.spectra.BinarySpectrum;
import org.spectra.cluster.model.spectra.IBinarySpectrum;

import java.util.*;
import java.util.stream.Stream;

/**
 * This is a greedy version of the FrankEtAlConsensusSpectrumBuilder. It only supports the addition of spectra but not their removal.
 * Thereby, the original peaks do not have to be kept. This implementation of {@link IConsensusSpectrumBuilder} contains in the
 * allPeaksInCluster property all the peaks of the spectra that belongs to the cluster.
 *
 * allPeaksInCluster: Contains all the peaks of the {@link IBinarySpectrum} that belong to the Cluster. All peaks of the spectra that belongs to
 * the Cluster needs to be keep to accurately compute the ConsensusPeaks each time is needed.
 *
 * consensusPeaks: This is the Array of @{@link BinaryPeak} of a clean @{@link GreedyConsensusSpectrum}.
 *
 * isDirty: This variable is used to notice the algorithm each time the consensusPeaks are generated from the allPeaksInCluster. This variable is important because
 * the class only will update the consensusPeaks when is needed.
 *
 * @author Johannes Griss
 */
public class GreedyConsensusSpectrum implements IConsensusSpectrumBuilder {
    /**
     * Peaks to keep per 100 m/z during noise filtering
     */
    public static final int DEFAULT_PEAKS_TO_KEEP = 5;
    /**
     * Sliding window size to use when applying the noise filter
     */
    public static final int NOISE_FILTER_INCREMENT = 100;

    public static final int MIN_PEAKS_TO_KEEP = 50;

    //Id of the GreedyConsensusSpectrum
    private final String id;

    // The peaks of the GreedyConsensusSpectrum
    private BinaryPeak[]  consensusPeaks;

    // All peaks in the Cluster
    private BinaryConsensusPeak[] allPeaksInCluster = new BinaryConsensusPeak[0];

    private boolean isDirty = true;

    // Number of the spectra in the Cluster.
    private int nSpectra;

    // Mz of the consensus spectra.
    private int averagePrecursorMz;

    // Charge of the consensus spectra.
    private int averageCharge;

    private long sumPrecursorMz;
    private int sumCharge;

    private final int minPeaksToKeep;
    private final int peaksPerWindowToKeep;
    private final int windowSize;


    /**
     * Generate a new GreedyConsensusSpectrum
     * @param id The id to use
     * @param minPeaksToKeep The minimum number of peaks that should always be retained.
     * @param peaksPerWindowToKeep The minimum number of peaks to keep within the m/z window size.
     * @param windowSize The m/z window size for the peak filter to use.
     */
    public GreedyConsensusSpectrum(String id, int minPeaksToKeep, int peaksPerWindowToKeep, int windowSize) {
        this.id = id;
        this.minPeaksToKeep = minPeaksToKeep;
        this.peaksPerWindowToKeep = peaksPerWindowToKeep;
        this.windowSize = windowSize;
    }

    public GreedyConsensusSpectrum(String id) {
        this(id, MIN_PEAKS_TO_KEEP, DEFAULT_PEAKS_TO_KEEP, NOISE_FILTER_INCREMENT);
    }

    /**
     * Create a new consensus spectrum builder with a random id and the default values.
     */
    public GreedyConsensusSpectrum() {
        this(UUID.randomUUID().toString());
    }

    /**
     * This function will add to the allPeaksInCluster, the peaks from the newSpectra.
     *
     * Any clustering process will compute the similarity between spectra and try to add the similar spectra to the {@link GreedyConsensusSpectrum}.
     * This method only add the peaks of the spetra to allPeaksInCluster and declare the Consensus Spectrum as Dirty. The algorithm loop the list of {@link IBinarySpectrum} and
     * add the {@link BinaryPeak} o the allPeaksInCluster property.
     *
     * @param newSpectra List of Spectra to be added to the {@link GreedyConsensusSpectrum}
     */
    @Override
    public void addSpectra(IBinarySpectrum... newSpectra) {
        if (newSpectra.length < 1)
            return;

        for (IBinarySpectrum spectrum : newSpectra) {

            allPeaksInCluster = addPeaksToConsensus(allPeaksInCluster, spectrum.getPeaks());

            sumCharge += spectrum.getPrecursorCharge();
            sumPrecursorMz += spectrum.getPrecursorMz();

            nSpectra++;
        }

        // update properties charge, precursor m/z and precursor intensity
        updateProperties();

        setIsDirty(true);
    }

    @Override
    public void addConsensusSpectrum(IConsensusSpectrumBuilder consensusSpectrumToAdd) {
        if (consensusSpectrumToAdd == null || consensusSpectrumToAdd.getSpectraCount() < 1)
            return;

        // add the peaks like in a "normal" spectrum - the peak count's are preserved
        allPeaksInCluster = addPeaksToConsensus(allPeaksInCluster, consensusSpectrumToAdd.getPeaks());

        // update the general properties
        sumCharge += consensusSpectrumToAdd.getSummedCharge();
        sumPrecursorMz += consensusSpectrumToAdd.getSummedPrecursorMz();
        nSpectra += consensusSpectrumToAdd.getSpectraCount();

        // update properties charge, precursor m/z and precursor intensity
        updateProperties();

        setIsDirty(true);
    }

    /**
     * This function updates the actual consensus spectrum
     * based on all peaks currently stored in it.
     */
    protected void updateConsensusSpectrum() {
        if (isDirty()) {
            // update the actual consensus spectrum
            generateConsensusSpectrum();
            setIsDirty(false);
        }
    }

    /**
     * Adds the passed peaks to the spectrum.
     *
     * @param existingPeaks The already existing peaks
     * @param peaksToAdd New peaks to add. These may be BinaryPeak or BinaryConsensusPeak objects.
     */
    protected static BinaryConsensusPeak[] addPeaksToConsensus(BinaryConsensusPeak[] existingPeaks, BinaryPeak[] peaksToAdd) {
        if (peaksToAdd.length < 1) {
            return existingPeaks;
        }

        // iterate over all peaks
        int indexAllPeaks = 0;
        int indexToAdd = 0;

        List<BinaryConsensusPeak> newPeaks = new ArrayList<>(20);

        while (indexToAdd < peaksToAdd.length) {
            int newMz = peaksToAdd[indexToAdd].getMz();

            while (indexAllPeaks < existingPeaks.length) {
                int existingMz = existingPeaks[indexAllPeaks].getMz();

                if (existingMz < newMz) {
                    indexAllPeaks++;
                    continue;
                }

                // it's a new peak
                if (newMz < existingMz) {
                    if (peaksToAdd[indexToAdd] instanceof BinaryConsensusPeak)
                        newPeaks.add(new BinaryConsensusPeak((BinaryConsensusPeak) peaksToAdd[indexToAdd]));
                    else
                        newPeaks.add(new BinaryConsensusPeak(peaksToAdd[indexToAdd]));

                    indexToAdd++;
                    break;
                }

                // it's the same peak so adapt it
                int newCount = existingPeaks[indexAllPeaks].getCount();
                long newIntensity = existingPeaks[indexAllPeaks].getIntensity() * existingPeaks[indexAllPeaks].getCount();

                if (peaksToAdd[indexToAdd] instanceof BinaryConsensusPeak) {
                    newCount += ((BinaryConsensusPeak) peaksToAdd[indexToAdd]).getCount();
                    newIntensity += (long) peaksToAdd[indexToAdd].getIntensity() * ((BinaryConsensusPeak) peaksToAdd[indexToAdd]).getCount();
                } else {
                    newCount++;
                    newIntensity += peaksToAdd[indexToAdd].getIntensity();
                }

                // store the average intensity to prevent an overflow
                newIntensity = Math.round(newIntensity / (double) newCount);

                // always store the average intensity to prevent an overflow
                existingPeaks[indexAllPeaks] = new BinaryConsensusPeak(
                        existingPeaks[indexAllPeaks].getMz(),
                        (int) newIntensity,
                         newCount
                );

                indexAllPeaks++;
                indexToAdd++;
                break;
            }

            if (indexAllPeaks >= existingPeaks.length && indexToAdd < peaksToAdd.length) {
                if (peaksToAdd[indexToAdd] instanceof BinaryConsensusPeak)
                    newPeaks.add(new BinaryConsensusPeak((BinaryConsensusPeak) peaksToAdd[indexToAdd]));
                else
                    newPeaks.add(new BinaryConsensusPeak(peaksToAdd[indexToAdd]));

                indexToAdd++;
            }
        }

        // add the new peaks
        if (newPeaks.size() > 0) {
            BinaryConsensusPeak[] mergedPeaks = Stream.concat(Arrays.stream(existingPeaks), newPeaks.stream()).toArray(BinaryConsensusPeak[]::new);
            Arrays.parallelSort(mergedPeaks, Comparator.comparingInt(BinaryPeak::getMz));
            return mergedPeaks;
        } else {
            return existingPeaks;
        }
    }

    /**
     * Updates all properties of the consensus spectrum as well as the actual consensus spectrum.
     * Aver
     */
    protected void updateProperties() {
        if (nSpectra > 0) {
            averagePrecursorMz = (int) sumPrecursorMz / nSpectra;
            averageCharge = sumCharge / nSpectra;
        } else {
            averagePrecursorMz = 0;
            averageCharge = 0;
        }
    }

    /**
     * Calls the required functions to do normalization and noise filtering that result in the actual consensus
     * spectrum.
     *
     * @return !null The generated IBinarySpectrum object
     */
    private void generateConsensusSpectrum() {
        if (allPeaksInCluster.length < 1) {
            consensusPeaks = new BinaryPeak[0];
        }

        // Step 2: adapt the peak intensities based on the probability that the peak has been observed
        BinaryConsensusPeak[] adaptedPeakIntensities = adaptPeakIntensities(allPeaksInCluster, nSpectra);

        // Step 3: filter the spectrum
        BinaryConsensusPeak[] filteredPeaks = filterNoise(adaptedPeakIntensities);

        consensusPeaks = filteredPeaks;
    }

    /**
     * Filters the consensus spectrum keeping only the top N peaks per M m/z
     */
    protected BinaryConsensusPeak[] filterNoise(BinaryConsensusPeak[] peaks) {
        if (peaks.length < minPeaksToKeep || peaks.length < 1) {
            return peaks;
        }

        // get the max m/z
        int maxMz = Arrays.stream(peaks).mapToInt(BinaryPeak::getMz).max().getAsInt();
        int peakIndex = 0;

        List<BinaryConsensusPeak> peaksToKeep = new ArrayList<>(100);

        // Keep top N peaks per W m/z
        for (int windowStart = 0; windowStart <= maxMz && peakIndex < peaks.length; windowStart += windowSize) {
            List<BinaryConsensusPeak> windowPeaks = new ArrayList<>(20);

            for(; peakIndex < peaks.length && peaks[peakIndex].getMz() < windowStart + windowSize; peakIndex++) {
                windowPeaks.add(peaks[peakIndex]);
            }

            if (windowPeaks.size() < 1) {
                continue;
            }

            if (windowPeaks.size() <= peaksPerWindowToKeep) {
                peaksToKeep.addAll(windowPeaks);
            } else {
                // only keep the top N peaks
                windowPeaks.sort(Comparator.comparingInt(BinaryPeak::getIntensity));
                peaksToKeep.addAll(windowPeaks.subList(windowPeaks.size() - peaksPerWindowToKeep, windowPeaks.size()));
            }
        }

        return peaksToKeep.stream().sorted(Comparator.comparingInt(BinaryPeak::getMz)).toArray(BinaryConsensusPeak[]::new);
    }

    /**
     * Adapt the peak intensities in allPeaksInCluster using the following formula:
     * I = I * (0.95 + 0.05 * (1 + pi)^5)
     * //TOdo : @jgriss where this came from.
     * where pi is the peaks probability
     */
    protected static BinaryConsensusPeak[] adaptPeakIntensities(BinaryConsensusPeak[] peaks, int nSpectra) {
        BinaryConsensusPeak[] adaptedPeaks = new BinaryConsensusPeak[peaks.length];
        double doubleSpectra = (double) nSpectra;

        for (int i = 0; i < peaks.length; i++) {
            double peakProbability = (double) peaks[i].getCount() / doubleSpectra;
            int newIntensity = (int) Math.round((double) peaks[i].getIntensity() * (0.95 + 0.05 * Math.pow(1 + peakProbability, 5)));

            adaptedPeaks[i] = new BinaryConsensusPeak(peaks[i].getMz(), newIntensity, peaks[i].getCount());
        }

        return adaptedPeaks;
    }

    /**
     * This function retrieve the current {@link IBinarySpectrum}.
     * @return binaryConsensusSpectrum
     */
    @Override
    public IBinarySpectrum getConsensusSpectrum() {
        if(isDirty)
            generateConsensusSpectrum();
        return this;
    }

    @Override
    public void clear() {
        sumCharge = 0;
        sumPrecursorMz = 0;
        nSpectra = 0;

        allPeaksInCluster = new BinaryConsensusPeak[0];
        setIsDirty(true);
    }

    @Override
    public int getSpectraCount() {
        return nSpectra;
    }

    private boolean isDirty() {
        return isDirty;
    }

    private void setIsDirty(boolean isDirty) {
        this.isDirty = isDirty;
    }

    @Override
    public long getSummedPrecursorMz() {
        return sumPrecursorMz;
    }

    @Override
    public int getSummedCharge() {
        return sumCharge;
    }

    @Override
    public int getPrecursorMz() {
        return averagePrecursorMz;
    }

    @Override
    public int getPrecursorCharge() {
        return averageCharge;
    }

    @Override
    public int[] getMzVector() {
        return Arrays.stream(consensusPeaks).mapToInt(BinaryPeak::getMz).toArray();
    }

    @Override
    public int[] getIntensityVector() {
        return Arrays.stream(consensusPeaks).mapToInt(BinaryPeak::getIntensity).toArray();
    }

    @Override
    public int getNumberOfPeaks() {
        return consensusPeaks.length;
    }

    @Override
    public String getUUI() {
        return id;
    }

    @Override
    public BinaryPeak[] getPeaks() {
        return consensusPeaks;
    }

    @Override
    public void setPeaks(BinaryPeak[] peaks) {
        consensusPeaks = peaks;
    }
}
