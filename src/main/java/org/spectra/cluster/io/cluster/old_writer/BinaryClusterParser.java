package org.spectra.cluster.io.cluster.old_writer;

import org.spectra.cluster.model.cluster.ICluster;

import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * @author ypriverol
 * @author jgriss
 */
public class BinaryClusterParser {

    public static final BinaryClusterParser INSTANCE = new BinaryClusterParser();

    private BinaryClusterParser() {

    }

    public ICluster parseNextCluster(ObjectInputStream inputStream, String lastReadString) throws IOException, ClassNotFoundException {
//        // the first string marking the beginning of a cluster may have already been read
//        String className = lastReadString;
//        if (className == null)
//            className = (String) inputStream.readObject();
//
//        String id = (String) inputStream.readObject();
//        int charge = inputStream.readInt();
//        float precursorMz = inputStream.readFloat();
//        boolean storesPeaklists = inputStream.readBoolean();
//
//        List<ComparisonMatch> comparisonMatches = parseComparisonMatches(inputStream);
//
//        IConsensusSpectrumBuilder consensusSpectrumBuilder = null;
//        if (!storesPeaklists) {
//            consensusSpectrumBuilder = parseConsensusSpectrumBuilder(inputStream);
//        }
//
//        List<IBinarySpectrum> spectra = parseSpectra(inputStream);
//
//        // create the cluster
//        if (storesPeaklists) {
//            ICluster ret = new SpectralCluster(id, Defaults.getDefaultConsensusSpectrumBuilder());
//            ISpectrum[] spectraArray = new ISpectrum[spectra.size()];
//            ret.addSpectra(spectra.toArray(spectraArray));
//
//            return ret;
//        }
//        else {
//            ICluster ret = new GreedySpectralCluster(id, spectra, (BinnedGreedyConsensusSpectrum) consensusSpectrumBuilder, comparisonMatches);
//            return ret;
//        }
        return null;
    }

//    private List<ISpectrum> parseSpectra(ObjectInputStream inputStream) throws IOException, ClassNotFoundException {
//        int nSpectra = inputStream.readInt();
//        List<ISpectrum> spectra = new ArrayList<>(nSpectra);
//
//        for (int i = 0; i < nSpectra; i++) {
//            String id = (String) inputStream.readObject();
//            int charge = inputStream.readInt();
//            float precursorMz = inputStream.readFloat();
//            Properties properties = (Properties) inputStream.readObject();
//            List<IPeak> peakList = parsePeakList(inputStream);
//
//            // create the spectrum
//            ISpectrum spectrum = new Spectrum(id, charge, precursorMz, Defaults.getDefaultQualityScorer(), peakList);
//
//            // set the properties
//            for (String propertyName : properties.stringPropertyNames()) {
//                spectrum.setProperty(propertyName, properties.get(propertyName));
//            }
//
//            spectra.put(spectrum);
//        }
//
//        return spectra;
//        return null;
//    }

//    private IConsensusSpectrumBuilder parseConsensusSpectrumBuilder(ObjectInputStream inputStream) throws IOException, ClassNotFoundException {
//        String className = (String) inputStream.readObject();
//
//        // standard fields
//        String id = (String) inputStream.readObject();
//        int nSpectra = inputStream.readInt();
//        int sumCharge = inputStream.readInt();
//        double sumPrecursorIntensity = inputStream.readDouble();
//        double sumPrecursorMz = inputStream.readDouble();
//
//        List<IPeak> peakList = parsePeakList(inputStream);
//
//        // create the consensus spectrum
//        IConsensusSpectrumBuilder consensusSpectrumBuilder = new BinnedGreedyConsensusSpectrum(Defaults.getFragmentIonTolerance(), id, nSpectra, sumPrecursorMz, sumPrecursorIntensity, sumCharge, peakList, new BinSpectrumMaxFunction(Defaults.getFragmentIonTolerance()));
//
//        return consensusSpectrumBuilder;
//        return null;
//    }

//    private List<I> parsePeakList(ObjectInputStream inputStream) throws IOException {
//        int nPeaks = inputStream.readInt();
//
//        List<IPeak> peakList = new ArrayList<>(nPeaks);
//
//        for (int i = 0; i < nPeaks; i++) {
//            float mz = inputStream.readFloat();
//            float intens = inputStream.readFloat();
//            int count = inputStream.readInt();
//
//            peakList.put(new Peak(mz, intens, count));
//        }
//
//        return peakList;
//    }

//    private List<ComparisonMatch> parseComparisonMatches(ObjectInputStream inputStream) throws IOException, ClassNotFoundException {
//        int nMatches = inputStream.readInt();
//
//        List<ComparisonMatch> comparisonMatches = new ArrayList<>(nMatches);
//
//        for (int i = 0; i < nMatches; i++) {
//            float similarity = inputStream.readFloat();
//            String id = (String) inputStream.readObject();
//
//            comparisonMatches.put(new ComparisonMatch(id, similarity));
//        }
//
//        return comparisonMatches;
//    }
}
