mvn clean
mvn package -DskipTests
curl --fail -sSL --user onos:rocks -X POST -HContent-Type:application/octet-stream 'http://192.168.60.173:8181/onos/v1/applications?activate=true' --data-binary @target/minindn-1.0-SNAPSHOT.oar

