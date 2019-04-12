################## BASE IMAGE ######################
FROM biocontainers/biocontainers:latest

################## METADATA ######################
LABEL base_image="biocontainers:latest"
LABEL version="1"
LABEL software="spectra-cluster-2"
LABEL software.version="v0.1alpha"
LABEL about.summary="PRIDE Cluster algorithm to cluster heterogeneous mass spectra"
LABEL about.home="https://github.com/spectra-cluster/spectra-cluster-2"
LABEL about.documentation="https://github.com/spectra-cluster/spectra-cluster-2"
LABEL about.license_file="https://github.com/spectra-cluster/spectra-cluster-2/LICENSE"
LABEL about.license="SPDX:Apache-2.0"
LABEL about.tags="Proteomics"

################## MAINTAINER ######################
MAINTAINER Johannes Griss <jgriss@ebi.ac.uk>

USER root

RUN apt-get update && apt-get install -y maven openjdk-8-jdk

USER biodocker

RUN mkdir /home/biodocker/.m2

COPY Docker/settings.xml /home/biodocker/.m2/settings.xml

RUN wget https://github.com/spectra-cluster/spectra-cluster-2/archive/develop.zip -O /tmp/spectra-cluster-2.zip && \
    unzip /tmp/spectra-cluster-2.zip -d /tmp/spectra-cluster-2 && \
    rm /tmp/spectra-cluster-2.zip && \
    cd /tmp/spectra-cluster-2/spec* && \
    mvn -DskipTests package && \
    unzip target/spectra-cluster-2.*-bin.zip -d /home/biodocker/bin/ && \
    echo -e '#!/bin/bash\njava -jar /home/biodocker/bin/spectra-cluster-*.jar $@' > /home/biodocker/bin/spectra-cluster-2 && \
    chmod +x /home/biodocker/bin/spectra-cluster-2

ENV PATH /home/biodocker/bin/spectra-cluster-2:$PATH

WORKDIR /data/
