package org.spectra.cluster.binning;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import org.spectra.cluster.exceptions.SpectraClusterException;
import org.spectra.cluster.model.cluster.IClusterProperties;

import java.util.*;

/**
 * A cluster binner that bins clusters primarily according to m/z and charge (optional).
 * Binning is governed by two parameters: the minimum m/z window size and the minimum
 * number of spectra per bin. Smaller bins are merged into adjacent larger ones.
 */
@Data
public class SimilarSizedClusterBinner implements IClusterBinner {
    /**
     * The minimum bin size in (integer changed) m/z.
     */
    private final int binSizeMz;
    /**
     * Minimum number of cluster per bin before merging clusters.
     */
    private final int minimumBinSizeCluster;
    /**
     * If set, clusters with different charge states are put in separate bins
     */
    private final boolean useCharge;

    @Setter(AccessLevel.NONE)
    private int precursorOffset;

    @Override
    public String[][] binClusters(IClusterProperties[] clusters, boolean shift) throws SpectraClusterException {
        // ensure that there are clusters to bin
        if (clusters.length < minimumBinSizeCluster) {
            String[] allIds = Arrays.stream(clusters)
                    .map(IClusterProperties::getId)
                    .toArray(String[]::new);

            // return all ids in a single bin
            return new String[][] { allIds };
        }

        if (shift)
            // shifts the assignment of clusters to bins by half the bin size
            precursorOffset = Math.round((float) binSizeMz / 2);
        else
            precursorOffset = 0;

        // get the base binning result
        Map<Integer, List<String>> bins = doBinning(clusters);

        // merge small bins
        Integer[] allBins = bins.keySet().stream().sorted().toArray(Integer[]::new);

        for (int binIndex = 0; binIndex < allBins.length - 1; binIndex++) {
            // merge the bin with the next one if it is too small
            if (bins.get(allBins[binIndex]).size() < minimumBinSizeCluster) {
                // add all clusters to the next bin
                bins.get(allBins[binIndex + 1]).addAll( bins.get(allBins[binIndex]) );

                // put an empty list instead
                bins.put(allBins[binIndex], new ArrayList<>(0));
            }
        }

        // convert to array and remove empty bins
        String[][] finalBins = bins.values().stream()
                .filter((List<String> currentBin) -> currentBin.size() > 0)
                .map((List<String> currentBin) -> currentBin.toArray(new String[0]))
                .toArray(String[][]::new);

        return finalBins;
    }

    /**
     * Performs the initial binning based only on the set bin m/z size and the charge state (if set).
     *
     * @param clusters The clusters to bin.
     * @return A map with the bin index as key and the respective cluster ids as values.
     * @throws SpectraClusterException
     */
    private Map<Integer, List<String>> doBinning(IClusterProperties[] clusters) throws SpectraClusterException {
        // bin the cluster in the smallest possible bins
        Map<Integer, List<String>> bins = new HashMap<>(1000);

        for (IClusterProperties cluster : clusters) {
            // get the "raw" bin
            int bin = (cluster.getPrecursorMz() + precursorOffset) / binSizeMz;

            // encode the charge as 100_000_000 * charge in the bin #
            if (useCharge) {
                if (cluster.getPrecursorCharge() != null) {
                    // ensure that the charge is plausible
                    if (cluster.getPrecursorCharge() < 0 || cluster.getPrecursorCharge() > 9)
                        throw new SpectraClusterException(
                                String.format("Cluster %s contains unrealistic charge of %d",
                                        cluster.getId(), cluster.getPrecursorCharge()));

                    // encode the charge
                    bin += 100_000_000 * cluster.getPrecursorCharge();
                }
            }

            if (!bins.containsKey(bin))
                bins.put(bin, new ArrayList<>(1));

            bins.get(bin).add(cluster.getId());
        }

        return bins;
    }
}
