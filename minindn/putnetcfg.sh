curl --fail -sSL --user onos:rocks --noproxy localhost -X POST -H 'Content-Type:application/json' http://192.168.214.137:8181/onos/v1/network/configuration -d@../mininet/netcfg.json
