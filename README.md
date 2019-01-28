# Nuxeo XML Importer POC

## About

An implementation of Nuxeo XML Importer using Nuxeo Stream

## Build

`mvn clean install`

## Configuration

By default the importer will use Chronicle Queue for streaming records for further import.
To redirect the import records through Kafka you must supply `nuxeo.conf` with 
following parameters:

`nuxeo.importer.log.name=kafka` 
`nuxeo.importer.bootstrap.servers=` - your Kafka discover ip address(es)
`nuxeo.importer.request.timeout.ms=30000` - Or a value that fits the best your cluster settings
`nuxeo.importer.max.poll.interval.ms=20000` - Or a value that fits the best your cluster settings
`nuxeo.importer.session.timeout.ms=10000` - Or a value that fits the best your cluster settings
`nuxeo.importer.heartbeat.interval.ms=400` - Or a value that fits the best your cluster settings
`nuxeo.importer.max.poll.records=25` - Or a value that fits the best your cluster settings

## About Nuxeo
Nuxeo dramatically improves how content-based applications are built,
managed and deployed, making customers more agile,
innovative and successful. Nuxeo provides a next generation,
enterprise ready platform for building traditional and cutting-edge
content oriented applications. Combining a powerful application
development environment with SaaS-based tools and a modular
architecture, the Nuxeo Platform and Products provide clear business
value to some of the most recognizable brands including Verizon,
Electronic Arts, Sharp, FICO, the U.S. Navy, and Boeing.
Nuxeo is headquartered in New York and Paris.
More information is available at [www.nuxeo.com](http://www.nuxeo.com).