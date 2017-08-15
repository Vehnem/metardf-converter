# Metardf-converter

This tool converts the [artists dataset](https://bitbucket.org/GlebGawriljuk/aifb-isi-knowledgegraphconstruction/src/160d080404f3097b5f267ce61197643bc299a449/KG_final.json.gz?at=master&fileviewer=file-view-default) with  statement-level provenance in a resource centric json format into the metadata agnostic [meta-rdf format](https://github.com/AKSW/meta-rdf)

## Basic usage

1. git clone https://github.com/Vehnem/metardf-converter.git
1. cd metardf-converter 
1. maven package
1. java -jar target/metardf-converter-0.0.1-SNAPSHOT.jar <path/to/input> <path/to/output> [-gz]

if gz flag is set then use gzip file as input stream

##  Related papers

* G. Gawriljuk, A. Harth, C. A. Knoblock, and P. Szekely. A scalable approach to incrementally building knowledge graphs. In International Conference on Theory and Practice of Digital Libraries, volume 9819 of Lecture Notes in Computer Science, pages 188–199. Springer, Springer, 2016
