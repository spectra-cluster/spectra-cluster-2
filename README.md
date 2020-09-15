# spectra-cluster-2
A complete re-write of the spectra-cluster codebase

## Development status

This is a list of planned and already integrated key features of the tool:

  - [x] Use binary representation of spectra for clustering
  - [x] Cluster MGF files
  - [x] Create storage systems to store spectrum properties and clustering results
  - [x] Out-of-memory processing of large files
  - [x] Write spectral libraries in MSP format
  - [x] Multi-threading support
  - [ ] Support clustering files as input to clustering process for incremental clustering
  - [ ] Include different algorithms to create consensus spectra

## Release process using jgitflow

Releases are currently performed using the
[jgitflow](https://bitbucket.org/atlassian/jgit-flow/wiki/Home) plugin.

The `jgitflow` plugin (or git-flow in general) uses different branches
to keep track of the development cycle. As in our current concept,
the `master` branch only contains stable code, while development is
done in the `develop` branch.

Additionally, separate branches can be created to start working on
a `feature` or a `hotfix` (these terms are used by git-flow).

To start a release, use `mvn jgitflow:release-start`. This will
create a new branch for that releast. Once work on the release
is complete, `mvn jgitflow:release-finish` can be used to finally
create it.
