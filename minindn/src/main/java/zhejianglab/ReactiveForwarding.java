/*
 * Copyright 2014-present Open Networking Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package zhejianglab;

import org.onlab.packet.*;
import org.onosproject.cfg.ComponentConfigService;
import org.onosproject.core.ApplicationId;
import org.onosproject.net.Host;
import org.onosproject.net.HostId;
import org.onosproject.net.PortNumber;
import org.onosproject.net.packet.*;
import org.onosproject.net.host.HostService;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.*;
import org.slf4j.Logger;
import org.onosproject.net.flow.DefaultTrafficSelector;
import org.onosproject.net.flow.TrafficSelector;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Sample reactive forwarding application.
 */
@Component(
    immediate = true,
    service = ReactiveForwarding.class
)
public class ReactiveForwarding {

    private final Logger log = getLogger(getClass());


    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected PacketService packetService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected HostService hostService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected ComponentConfigService cfgService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    private MainComponent mainComponent;

    private ReactivePacketProcessor processor = new ReactivePacketProcessor();

    private ApplicationId appId;

    @Activate
    public void activate(ComponentContext context) {
        appId = mainComponent.getAppId();
        packetService.addProcessor(processor, PacketProcessor.director(2));
        //requestIntercepts();

        log.info("***Started:", appId.id());
    }

    @Deactivate
    public void deactivate() {
        packetService.removeProcessor(processor);
        processor = null;
        log.info("Stopped");
    }

    /**
     * Request packet in via packet service.
     */
    private void requestIntercepts() {
        log.info("****requestIntercepts");
        TrafficSelector.Builder selector = DefaultTrafficSelector.builder();
        selector.matchEthType(Ethernet.TYPE_IPV4);
        //selector.matchIPProtocol(IPv4.PROTOCOL_ICMP);

        packetService.requestPackets(selector.build(), PacketPriority.REACTIVE, appId);
    }

    /**
     * Packet processor responsible for forwarding packets along their paths.
     */
    private class ReactivePacketProcessor implements PacketProcessor {

        @Override
        public void process(PacketContext context) {
            // Stop processing if the packet has been handled, since we
            // can't do any more to it.

            if (context.isHandled()) {
                //log.info("IsHandled");
                return;
            }


            InboundPacket pkt = context.inPacket();
            Ethernet ethPkt = pkt.parsed();
            if (ethPkt == null) {
                //log.info("ethPkt is Null");
                return;
            }

            if(ethPkt.getEtherType() != Ethernet.TYPE_IPV4){
                //log.info("drop not a ipv4");
                return;
            }

            IPv4 ipv4Packet = (IPv4) ethPkt.getPayload();

            if(ipv4Packet.getProtocol() != IPv4.PROTOCOL_ICMP){
                //log.info("not icmp");
                return;
            }

            log.info("1SA: {}", IpAddress.valueOf(ipv4Packet.getSourceAddress()).getIp4Address().toString());
            log.info("2DA: {}", IpAddress.valueOf(ipv4Packet.getDestinationAddress()).getIp4Address().toString());


            HostId id = HostId.hostId(ethPkt.getDestinationMAC());
            log.info("3H: {}",id);


            //if(pkt.receivedFrom().deviceId().equals(dst)
            context.treatmentBuilder().setOutput(PortNumber.portNumber(3));
            context.send();

            log.info("4S: {}",context.inPacket());

            //
        }

    }
}
