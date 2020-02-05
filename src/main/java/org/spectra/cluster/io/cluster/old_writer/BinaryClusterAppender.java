package org.spectra.cluster.io.cluster.old_writer;


import org.spectra.cluster.exceptions.SpectraClusterException;
import org.spectra.cluster.model.cluster.ComparisonMatch;
import org.spectra.cluster.model.cluster.ICluster;
import org.spectra.cluster.model.consensus.IConsensusSpectrumBuilder;
import org.spectra.cluster.model.spectra.BinaryPeak;
import org.spectra.cluster.model.spectra.IBinarySpectrum;

import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 * uk.ac.ebi.pride.spectracluster.cluster.CGFClusterAppender
 * @author ypriverol
 */
public class BinaryClusterAppender {


    public static BinaryClusterAppender INSTANCE = new BinaryClusterAppender();

    private BinaryClusterAppender() {

    }

    /**
     * @param out       !null open ObjectOutputStream
     * @param cluster   !null cluster
     */
    public void appendCluster(final ObjectOutputStream out, final ICluster cluster) throws SpectraClusterException {
        try {
            // first save the classname
            out.writeObject(cluster.getClass().getCanonicalName());

            // standard fields
            out.writeObject(cluster.getId());
            out.writeInt(cluster.getPrecursorCharge());
            out.writeFloat(cluster.getPrecursorMz());

            appendComparisonMatches(out, cluster);

            appendConsensusSpectrumBuilder(out, cluster.getConsensusSpectrumBuilder());

            appendSpectra(out, cluster);
        } catch (IOException e) {
            throw new SpectraClusterException("", e);
        }
    }

    private void appendConsensusSpectrumBuilder(ObjectOutputStream out, IConsensusSpectrumBuilder consensusSpectrumBuilder) throws IOException {
        // always save the class first
        out.writeObject(consensusSpectrumBuilder.getClass().getCanonicalName());

        // standard fields
        out.writeObject(consensusSpectrumBuilder.getConsensusSpectrum().getUUI());
        out.writeInt(consensusSpectrumBuilder.getSpectraCount());
        out.writeInt(consensusSpectrumBuilder.getSummedCharge());

        // write the raw peak list
        appendPeaklist(out, consensusSpectrumBuilder.getConsensusSpectrum());
    }

    private void appendPeaklist(ObjectOutputStream out, IBinarySpectrum peaklist) throws IOException {
        out.writeInt(peaklist.getNumberOfPeaks());

        for (BinaryPeak peak : peaklist.getPeaks()) {
            out.writeFloat(peak.getMz());
            out.writeFloat(peak.getIntensity());
            out.writeInt(peak.getMzHash());
            out.writeInt(peak.getRank());
        }
    }

    private void appendComparisonMatches(ObjectOutputStream out, ICluster cluster) throws IOException {
        out.writeInt(cluster.getComparisonMatches().size());

        // ignore empty matches
        if (cluster.getComparisonMatches().isEmpty())
            return;

        // write the comparison matches
        for (ComparisonMatch c : cluster.getComparisonMatches()) {
            out.writeFloat(c.getSimilarity());
            out.writeObject(c.getSpectrumId());
        }
    }

    private void appendSpectra(ObjectOutputStream out, ICluster cluster) throws IOException {
//        List<ISpectrum> clusteredSpectra = cluster.getClusteredSpectra();
//        out.writeInt(clusteredSpectra.size());
//
//        for (ISpectrum cs : clusteredSpectra) {
//            // default properties
//            out.writeObject(cs.getId());
//            out.writeInt(cs.getPrecursorCharge());
//            out.writeFloat(cs.getPrecursorMz());
//
//            // additional properties
//            Properties properties = cs.getProperties();
//            out.writeObject(properties);
//
//            // peak list
//            appendPeaklist(out, cs.getPeaks());
//        }
    }

    public void appendEnd(ObjectOutputStream out) throws IOException {
        out.writeObject("END");
    }
}
