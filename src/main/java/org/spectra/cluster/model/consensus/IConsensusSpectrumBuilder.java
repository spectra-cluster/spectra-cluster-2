/*
 * Copyright 2013 European Bioinformatics Institute.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.spectra.cluster.model.consensus;

import org.spectra.cluster.model.spectra.IBinarySpectrum;

/**
 * IConsensusSpectrum is the default interface for objects used to
 * build consensus spectra more efficiently. The consensus spectrum
 * is made available just like a normal spectrum. It is generated through
 * adding and / or removing spectra from the consensus spectrum.
 *
 * @author jg
 */
public interface IConsensusSpectrumBuilder {

    /**
     * return the current spectrum represented as the data in the stored spectra
     *
     * @return !null Spectrum
     */
    IBinarySpectrum getConsensusSpectrum();

    /**
     * Clear the consensus spectrum.
     */
    void clear();

    /**
     * Returns the number of spectra making up the consensus spectrum.
     * @return Number of Spectra in the consensus cluster
     */
    int getSpectraCount();

    /**
     * Used to merge two consensus spectra
     * @param consensusSpectrumToAdd Add a ConsensusSpectrum
     */
    void addConsensusSpectrum(IConsensusSpectrumBuilder consensusSpectrumToAdd);

    /**
     * Add spectra to the consensus spectrum.
     * @param spectra The spectra to add
     */
    void addSpectra(IBinarySpectrum... spectra);

    /**
     * These getters are used to merge two consensus spectra
     */
    long getSummedPrecursorMz();

    int getSummedCharge();
}