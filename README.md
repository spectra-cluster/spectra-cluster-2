# spectra-cluster-2
A complete re-write of the spectra-cluster codebase

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