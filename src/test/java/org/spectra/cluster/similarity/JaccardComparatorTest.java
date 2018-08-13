package org.spectra.cluster.similarity;


import org.junit.Assert;
import org.junit.Test;
import org.spectra.cluster.model.spectra.BinarySpectrum;
import org.spectra.cluster.model.spectra.ISpectrum;
import org.spectra.cluster.model.spectra.Spectrum;
import org.spectra.cluster.utils.ParserUtilities;

import java.io.LineNumberReader;
import java.io.StringReader;
import java.util.stream.Collectors;

public class JaccardComparatorTest {

    public static final String SPECTRUM_1 =
            "BEGIN IONS\n" +
                    "TITLE=id=1000,sequence=A\n" +
                    "PEPMASS=400.29999\n" +
                    "CHARGE=2.0+\n" +
                    "126.03780\t1.32\t1\n" +
                    "128.97778\t4.54\t1\n" +
                    "130.12135\t5.32\t1\n" +
                    "141.71326\t3.73\t1\n" +
                    "155.79489\t2.11\t1\n" +
                    "157.09422\t7.54\t1\n" +
                    "158.13786\t8.77\t1\n" +
                    "173.08929\t2.98\t1\n" +
                    "175.01099\t72.17\t1\n" +
                    "186.67210\t2.52\t1\n" +
                    "190.05768\t7.00\t1\n" +
                    "197.29556\t15.06\t1\n" +
                    "199.04955\t2.13\t1\n" +
                    "220.01772\t3.28\t1\n" +
                    "223.12512\t2.68\t1\n" +
                    "228.51855\t1.04\t1\n" +
                    "234.98022\t1.51\t1\n" +
                    "243.04980\t4.17\t1\n" +
                    "249.20453\t4.32\t1\n" +
                    "271.15723\t4.81\t1\n" +
                    "274.47568\t2.13\t1\n" +
                    "279.02142\t3.98\t1\n" +
                    "279.99423\t17.66\t1\n" +
                    "282.30859\t3.04\t1\n" +
                    "283.51581\t3.16\t1\n" +
                    "286.22876\t10.03\t1\n" +
                    "290.57080\t1.73\t1\n" +
                    "293.40524\t9.20\t1\n" +
                    "300.41602\t2.39\t1\n" +
                    "306.26575\t1.58\t1\n" +
                    "317.03546\t4.69\t1\n" +
                    "318.95529\t7.65\t1\n" +
                    "327.34570\t15.32\t1\n" +
                    "329.15344\t31.22\t1\n" +
                    "331.03186\t3.89\t1\n" +
                    "339.85724\t127.68\t1\n" +
                    "340.68414\t1.28\t1\n" +
                    "342.02014\t13.21\t1\n" +
                    "346.54468\t10.66\t1\n" +
                    "348.25540\t7.77\t1\n" +
                    "349.11072\t5.65\t1\n" +
                    "356.49588\t3.66\t1\n" +
                    "358.00571\t9.95\t1\n" +
                    "359.08218\t6.78\t1\n" +
                    "364.94586\t26.50\t1\n" +
                    "366.25629\t47.06\t1\n" +
                    "371.22272\t8.50\t1\n" +
                    "380.18915\t1.42\t1\n" +
                    "381.25684\t21.79\t1\n" +
                    "382.19437\t62.27\t1\n" +
                    "383.31369\t38.82\t1\n" +
                    "384.26367\t12.26\t1\n" +
                    "385.05157\t43.37\t1\n" +
                    "391.01205\t41.33\t1\n" +
                    "391.66846\t2.54\t1\n" +
                    "392.36270\t27.55\t1\n" +
                    "393.10971\t2.69\t1\n" +
                    "397.79718\t4.86\t1\n" +
                    "399.89346\t15.87\t1\n" +
                    "400.86118\t56.83\t1\n" +
                    "402.02863\t15.93\t1\n" +
                    "402.97165\t9.95\t1\n" +
                    "451.53363\t2.27\t1\n" +
                    "459.32065\t1.67\t1\n" +
                    "474.35086\t9.96\t1\n" +
                    "485.41226\t8.86\t1\n" +
                    "486.89313\t7.25\t1\n" +
                    "507.66586\t5.14\t1\n" +
                    "519.41101\t5.17\t1\n" +
                    "532.21863\t2.69\t1\n" +
                    "541.02563\t2.55\t1\n" +
                    "542.48981\t3.18\t1\n" +
                    "558.53662\t4.75\t1\n" +
                    "581.58582\t2.75\t1\n" +
                    "583.65271\t3.45\t1\n" +
                    "584.99292\t3.18\t1\n" +
                    "596.28265\t2.45\t1\n" +
                    "615.37720\t2.13\t1\n" +
                    "628.45862\t7.96\t1\n" +
                    "634.85840\t1.48\t1\n" +
                    "671.59094\t4.90\t1\n" +
                    "700.31726\t1.73\t1\n" +
                    "726.90869\t3.10\t1\n" +
                    "1064.92664\t3.94\t1\n" +
                    "1150.09473\t1.77\t1\n" +
                    "1347.98328\t1.92\t1\n" +
                    "1545.88293\t2.22\t1\n" +
                    "1607.71973\t2.67\t1\n" +
                    "END IONS\n";

    public static final String SPECTRUM_2 =
            "BEGIN IONS\n" +
                    "TITLE=id=1000,sequence=A\n" +
                    "PEPMASS=400.29999\n" +
                    "CHARGE=2.0+\n" +
                    "126.03780\t1.32\t1\n" +
                    "128.97778\t4.54\t1\n" +
                    "130.12135\t5.32\t1\n" +
                    "141.71326\t3.73\t1\n" +
                    "155.79489\t2.11\t1\n" +
                    "157.09422\t7.54\t1\n" +
                    "158.13786\t8.77\t1\n" +
                    "173.08929\t2.98\t1\n" +
                    "175.01099\t72.17\t1\n" +
                    "186.67210\t2.52\t1\n" +
                    "190.05768\t7.00\t1\n" +
                    "197.29556\t15.06\t1\n" +
                    "199.04955\t2.13\t1\n" +
                    "220.01772\t3.28\t1\n" +
                    "223.12512\t2.68\t1\n" +
                    "228.51855\t1.04\t1\n" +
                    "234.98022\t1.51\t1\n" +
                    "243.04980\t4.17\t1\n" +
                    "249.20453\t4.32\t1\n" +
//                    "271.15723\t4.81\t1\n" +
                    "274.47568\t2.13\t1\n" +
                    "279.02142\t3.98\t1\n" +
                    "279.99423\t17.66\t1\n" +
                    "282.30859\t3.04\t1\n" +
                    "283.51581\t3.16\t1\n" +
                    "286.22876\t10.03\t1\n" +
                    "290.57080\t1.73\t1\n" +
                    "293.40524\t9.20\t1\n" +
                    "300.41602\t2.39\t1\n" +
                    "306.26575\t1.58\t1\n" +
                    "317.03546\t4.69\t1\n" +
                    "318.95529\t7.65\t1\n" +
                    "327.34570\t15.32\t1\n" +
                    "329.15344\t31.22\t1\n" +
                    "331.03186\t3.89\t1\n" +
                    "339.85724\t127.68\t1\n" +
                    "340.68414\t1.28\t1\n" +
                    "342.02014\t13.21\t1\n" +
                    "346.54468\t10.66\t1\n" +
                    "348.25540\t7.77\t1\n" +
                    "349.11072\t5.65\t1\n" +
                    "356.49588\t3.66\t1\n" +
                    "358.00571\t9.95\t1\n" +
                    "359.08218\t6.78\t1\n" +
                    "364.94586\t26.50\t1\n" +
                    "366.25629\t47.06\t1\n" +
                    "371.22272\t8.50\t1\n" +
                    "380.18915\t1.42\t1\n" +
                    "381.25684\t21.79\t1\n" +
                    "382.19437\t62.27\t1\n" +
                    "383.31369\t38.82\t1\n" +
                    "384.26367\t12.26\t1\n" +
                    "385.05157\t43.37\t1\n" +
                    "391.01205\t41.33\t1\n" +
                    "391.66846\t2.54\t1\n" +
                    "392.36270\t27.55\t1\n" +
                    "393.10971\t2.69\t1\n" +
                    "397.79718\t4.86\t1\n" +
                    "399.89346\t15.87\t1\n" +
                    "400.86118\t56.83\t1\n" +
                    "402.02863\t15.93\t1\n" +
                    "402.97165\t9.95\t1\n" +
                    "451.53363\t2.27\t1\n" +
                    "459.32065\t1.67\t1\n" +
                    "474.35086\t9.96\t1\n" +
                    "485.41226\t8.86\t1\n" +
                    "486.89313\t7.25\t1\n" +
                    "507.66586\t5.14\t1\n" +
                    "519.41101\t5.17\t1\n" +
                    "532.21863\t2.69\t1\n" +
                    "541.02563\t2.55\t1\n" +
                    "542.48981\t3.18\t1\n" +
                    "558.53662\t4.75\t1\n" +
                    "581.58582\t2.75\t1\n" +
                    "583.65271\t3.45\t1\n" +
                    "584.99292\t3.18\t1\n" +
                    "596.28265\t2.45\t1\n" +
                    "615.37720\t2.13\t1\n" +
                    "628.45862\t7.96\t1\n" +
                    "634.85840\t1.48\t1\n" +
                    "671.59094\t4.90\t1\n" +
                    "700.31726\t1.73\t1\n" +
                    "726.90869\t3.10\t1\n" +
                    "1064.92664\t3.94\t1\n" +
                    "1150.09473\t1.77\t1\n" +
                    "1347.98328\t1.92\t1\n" +
                    "1545.88293\t2.22\t1\n" +
                    "1607.71973\t2.67\t1\n" +
                    "END IONS\n";


    @Test
    public void computeJaccard() {

        LineNumberReader inp = new LineNumberReader(new StringReader(SPECTRUM_1));
        ISpectrum spectrum = ParserUtilities.readMGFScan(inp);


        BinarySpectrum binarySpectrum1 = BinarySpectrum.builder()
                .precursortMZ((int) ((Spectrum)spectrum).getPrecursorMZ())
                .precursorCharge(((Spectrum) spectrum).getPrecursorCharge())
                .mzPeaksVector(((Spectrum) spectrum).getPeaks().stream().map(x-> (int)x.getKey().floatValue()).collect(Collectors.toList()))
                .build();

        inp = new LineNumberReader(new StringReader(SPECTRUM_2));
        spectrum = ParserUtilities.readMGFScan(inp);


        BinarySpectrum binarySpectrum2 = BinarySpectrum.builder()
                .precursortMZ((int) ((Spectrum)spectrum).getPrecursorMZ())
                .precursorCharge(((Spectrum) spectrum).getPrecursorCharge())
                .mzPeaksVector(((Spectrum) spectrum).getPeaks().stream().map(x-> (int)x.getKey().floatValue()).collect(Collectors.toList()))
                .build();

        float similarity = JaccardComparator.computeJaccard(binarySpectrum1.getMzPeaksVector(), binarySpectrum2.getMzPeaksVector());

        Assert.assertTrue(similarity - 0.988f < 0.1);



    }
}
