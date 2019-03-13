package org.spectra.cluster.model.consensus;

import lombok.extern.slf4j.Slf4j;
import org.spectra.cluster.model.spectra.BinaryPeak;
import org.spectra.cluster.model.spectra.IBinarySpectrum;

import java.util.*;

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
 * @author ypriverol
 */
@Slf4j
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
            nSpectra++;

            averagePrecursorMz = (int) Math.round(
                    (double) averagePrecursorMz * ((double) (nSpectra -1) / nSpectra) +
                            (double) spectrum.getPrecursorMz() / nSpectra);
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
        int totalSpectra = nSpectra + consensusSpectrumToAdd.getSpectraCount();

        averagePrecursorMz = (int) Math.round(
                (double) averagePrecursorMz * ((double) nSpectra / totalSpectra) +
                        (double) consensusSpectrumToAdd.getPrecursorMz() * ((double) consensusSpectrumToAdd.getSpectraCount() / totalSpectra));


        nSpectra = totalSpectra;

        // update properties charge, precursor m/z and precursor intensity
        updateProperties();

        setIsDirty(true);
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

        int indexExistingPeaks = 0;
        int indexPeaksToAdd = 0;
        int finalPeakIndex = 0;

        BinaryConsensusPeak[] newPeaks = new BinaryConsensusPeak[existingPeaks.length + peaksToAdd.length];

        while (indexExistingPeaks < existingPeaks.length && indexPeaksToAdd < peaksToAdd.length){

            if(existingPeaks[indexExistingPeaks].getMz() < peaksToAdd[indexPeaksToAdd].getMz()){
                newPeaks[finalPeakIndex] = existingPeaks[indexExistingPeaks];
                indexExistingPeaks++;
                finalPeakIndex++;
            }else if (existingPeaks[indexExistingPeaks].getMz() == peaksToAdd[indexPeaksToAdd].getMz()){
                // it's the same peak so adapt it
                int newCount = existingPeaks[indexExistingPeaks].getCount();
                long newIntensity = existingPeaks[indexExistingPeaks].getIntensity() * existingPeaks[indexExistingPeaks].getCount();

                if (peaksToAdd[indexPeaksToAdd] instanceof BinaryConsensusPeak) {
                    newCount += ((BinaryConsensusPeak) peaksToAdd[indexPeaksToAdd]).getCount();
                    newIntensity += (long) peaksToAdd[indexPeaksToAdd].getIntensity() * ((BinaryConsensusPeak) peaksToAdd[indexPeaksToAdd]).getCount();
                } else {
                    newCount++;
                    newIntensity += peaksToAdd[indexPeaksToAdd].getIntensity();
                }

                // store the average intensity to prevent an overflow
                newIntensity = Math.round(newIntensity / (double) newCount);

                // always store the average intensity to prevent an overflow
                newPeaks[finalPeakIndex] = new BinaryConsensusPeak(
                        existingPeaks[indexExistingPeaks].getMz(),
                        (int) newIntensity,
                        newCount
                );
                indexExistingPeaks++;
                finalPeakIndex++;
                indexPeaksToAdd++;
            } else {
                if (peaksToAdd[indexPeaksToAdd] instanceof BinaryConsensusPeak)
                    newPeaks[finalPeakIndex] = new BinaryConsensusPeak((BinaryConsensusPeak) peaksToAdd[indexPeaksToAdd]);
                else
                    newPeaks[finalPeakIndex] = new BinaryConsensusPeak(peaksToAdd[indexPeaksToAdd]);
                indexPeaksToAdd++;
                finalPeakIndex++;
            }
        }

        // Add the remaining spectra from existing Peaks
        while (indexExistingPeaks < existingPeaks.length){
            newPeaks[finalPeakIndex] = existingPeaks[indexExistingPeaks];
            indexExistingPeaks++;
            finalPeakIndex++;
        }

        // Add the remaining spectra from new Peaks
        while (indexPeaksToAdd < peaksToAdd.length){
            if (peaksToAdd[indexPeaksToAdd] instanceof BinaryConsensusPeak)
                newPeaks[finalPeakIndex] = new BinaryConsensusPeak((BinaryConsensusPeak) peaksToAdd[indexPeaksToAdd]);
            else
                newPeaks[finalPeakIndex] = new BinaryConsensusPeak(peaksToAdd[indexPeaksToAdd]);
            indexPeaksToAdd++;
            finalPeakIndex++;
        }

        return Arrays.copyOf(newPeaks, finalPeakIndex, BinaryConsensusPeak[].class);
    }

    /**
     * Updates all properties of the consensus spectrum as well as the actual consensus spectrum.
     * AveragePrecursorMZ and averageCharge.
     */
    protected void updateProperties() {
        if (nSpectra > 0) {
            averageCharge = sumCharge / nSpectra;
        } else {
            averagePrecursorMz = 0;
            averageCharge = 0;
        }
    }

    /**
     * Generate the consensus Spectrum using the Intensities in the allPeaksInCluster.
     * A normalization step is performed in the peaks and only the most intensity peaks
     * in an mz windows are keept . The current implementation combine the functions
     * adaptPeak and the function filterNoise.
     *
     */
    private void generateConsensusSpectrum(){
        if (allPeaksInCluster.length < 1) {
            consensusPeaks = new BinaryPeak[0];
        }
        consensusPeaks = adaptPeakWithNoiseFilterIntensities(allPeaksInCluster, nSpectra);

        setIsDirty(false);
    }

    /**
     * Filters the consensus spectrum keeping only the top N peaks per M m/z
     * @param peaks Peaks to filter
     * @return final clean array {@link BinaryConsensusPeak}
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
     * Adapt the peak intensities in allPeaksInCluster using the following formula:
     * I = I * (0.95 + 0.05 * (1 + pi)^5) . This probability comes from the FrankEtAll manuscript.
     *
     *
     * @param peaks This are all the peaks in the following consensus cluster.
     * @param nSpectra Number of spectra.
     * @return A clean array with {@link BinaryConsensusPeak}
     */
    protected  BinaryConsensusPeak[] adaptPeakWithNoiseFilterIntensities(BinaryConsensusPeak[] peaks, int nSpectra) {
        BinaryConsensusPeak[] adaptedPeaks = new BinaryConsensusPeak[peaks.length];

        double doubleSpectra = (double) nSpectra;

        int maxMz = 0;

        for (int i = 0; i < peaks.length; i++) {
            double peakProbability = (double) peaks[i].getCount() / doubleSpectra;
            int newIntensity = (int) Math.round((double) peaks[i].getIntensity() * (0.95 + 0.05 * Math.pow(1 + peakProbability, 5)));

            adaptedPeaks[i] = new BinaryConsensusPeak(peaks[i].getMz(), newIntensity, peaks[i].getCount());
            if(peaks[i].getMz() > maxMz)
                maxMz = peaks[i].getMz();
        }

        if (adaptedPeaks.length < minPeaksToKeep || adaptedPeaks.length < 1) {
            return adaptedPeaks;
        }

        int peakIndex = 0;

        /**
         * The maxiumn number of peaks to keep will be, the number of intervals * number of peaks per interval.
         */
        List<BinaryConsensusPeak> peaksToKeep = new ArrayList<>((maxMz/windowSize) * peaksPerWindowToKeep);

        // Keep top N peaks per W m/z
        for (int windowStart = 0; windowStart <= maxMz && peakIndex < adaptedPeaks.length; windowStart += windowSize) {
            List<BinaryConsensusPeak> windowPeaks = new ArrayList<>((maxMz/windowSize) * peaksPerWindowToKeep);

            for(; peakIndex < adaptedPeaks.length && adaptedPeaks[peakIndex].getMz() < windowStart + windowSize; peakIndex++) {
                windowPeaks.add(adaptedPeaks[peakIndex]);
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
        averagePrecursorMz = 0;
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
    public int getSummedCharge() {
        return sumCharge;
    }

    @Override
    public int getPrecursorMz() {
        if(isDirty())
            generateConsensusSpectrum();
        return averagePrecursorMz;
    }

    @Override
    public int getPrecursorCharge() {
        if(isDirty())
            generateConsensusSpectrum();
        return averageCharge;
    }

    @Override
    public int[] getCopyMzVector() {
        if(isDirty())
            generateConsensusSpectrum();
        return Arrays.stream(consensusPeaks).mapToInt(BinaryPeak::getMz).toArray();
    }

    @Override
    public int[] getCopyIntensityVector() {
        if(isDirty())
            generateConsensusSpectrum();
        return Arrays.stream(consensusPeaks).mapToInt(BinaryPeak::getIntensity).toArray();
    }

    @Override
    public int getNumberOfPeaks() {
        if(isDirty())
            generateConsensusSpectrum();
        return consensusPeaks.length;
    }

    @Override
    public String getUUI() {
        return id;
    }

    @Override
    public BinaryPeak[] getPeaks() {
        if(isDirty())
            generateConsensusSpectrum();

        return consensusPeaks;
    }

    @Override
    public BinaryPeak[] getCopyPeaks() {
        if (isDirty())
            generateConsensusSpectrum();

        return Arrays.copyOf(consensusPeaks, consensusPeaks.length);
    }
}
