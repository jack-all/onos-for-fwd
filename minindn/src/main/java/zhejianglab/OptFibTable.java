package zhejianglab;

import org.onlab.packet.MacAddress;
import org.onosproject.core.ApplicationId;
import org.onosproject.mastership.MastershipService;
import org.onosproject.net.DeviceId;
import org.onosproject.net.PortNumber;
import org.onosproject.net.device.DeviceEvent;
import org.onosproject.net.device.DeviceListener;
import org.onosproject.net.device.DeviceService;
import org.onosproject.net.flow.FlowEntry;
import org.onosproject.net.flow.FlowId;
import org.onosproject.net.flow.FlowRule;
import org.onosproject.net.flow.FlowRuleService;
import org.onosproject.net.flow.criteria.PiCriterion;
import org.onosproject.net.group.GroupDescription;
import org.onosproject.net.group.GroupService;
import org.onosproject.net.pi.model.PiActionId;
import org.onosproject.net.pi.model.PiActionParamId;
import org.onosproject.net.pi.model.PiMatchFieldId;
import org.onosproject.net.pi.runtime.PiAction;
import org.onosproject.net.pi.runtime.PiActionParam;
import org.osgi.service.component.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zhejianglab.common.Utils;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import static zhejianglab.AppConstants.INITIAL_SETUP_DELAY;

/**
 * App component that configures devices to provide L2 bridging capabilities.
 */
@Component(
        immediate = true,
        enabled = true,
        service = OptFibTable.class
)
public class OptFibTable {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final DeviceListener deviceListener = new InternalDeviceListener();

    private ApplicationId appId;

    private Set<CntFlow> CntFlows = new TreeSet<CntFlow>();

    //--------------------------------------------------------------------------
    // ONOS CORE SERVICE BINDING
    //
    // These variables are set by the Karaf runtime environment before calling
    // the activate() method.
    //--------------------------------------------------------------------------

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    private DeviceService deviceService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    private FlowRuleService flowRuleService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    private MastershipService mastershipService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    private MainComponent mainComponent;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    private GroupService groupService;

    //--------------------------------------------------------------------------
    // COMPONENT ACTIVATION.
    //
    // When loading/unloading the app the Karaf runtime environment will call
    // activate()/deactivate().
    //--------------------------------------------------------------------------

    @Activate
    protected void activate() {
        appId = mainComponent.getAppId();

        // Register listeners to be informed about device and host events.
        deviceService.addListener(deviceListener);
        log.info("Started0");
        // Schedule set up of existing devices. Needed when reloading the app.
        mainComponent.scheduleTask(this::setUpAllDevices, INITIAL_SETUP_DELAY);

        log.info("Started");
    }

    @Deactivate
    protected void deactivate() {

        log.info("Stopped");
    }

    //--------------------------------------------------------------------------
    // EVENT LISTENERS
    //
    // Events are processed only if isRelevant() returns true.
    //--------------------------------------------------------------------------

    /**
     * Listener of device events.
     */
    public class InternalDeviceListener implements DeviceListener {

        @Override
        public boolean isRelevant(DeviceEvent event) {
            switch (event.type()) {
                case DEVICE_ADDED:
                case DEVICE_AVAILABILITY_CHANGED:
                    break;
                default:
                    // Ignore other events.
                    return false;
            }
            // Process only if this controller instance is the master.

            final DeviceId deviceId = event.subject().id();
            log.info("{} eisRelevant! deviceId={}", event.type(), deviceId);
            return mastershipService.isLocalMaster(deviceId);
        }

        @Override
        public void event(DeviceEvent event) {
            final DeviceId deviceId = event.subject().id();
            if (deviceService.isAvailable(deviceId)) {
                // A P4Runtime device is considered available in ONOS when there
                // is a StreamChannel session open and the pipeline
                // configuration has been set.

                // Events are processed using a thread pool defined in the
                // MainComponent.
                mainComponent.getExecutorService().execute(() -> {
                    log.info("{} event! deviceId={}", event.type(), deviceId);
                    //insertMulticastGroup(deviceId);
                    //insertMulticastFlowRules(deviceId);
                    //insertFibTableFlowRules(deviceId);
                    //sendToCpuTableFlowRules(deviceId);
                    //aclCntTableFlowRules(deviceId);
                });
            }
        }
    }

    /**
     * Insert flow rules matching matching ethernet destination
     * broadcast/multicast addresses (e.g. ARP requests, NDP Neighbor
     * Solicitation, etc.). Such packets should be processed by the multicast
     * group created before.
     * <p>
     * This method will be called at component activation for each device
     * (switch) known by ONOS, and every time a new device-added event is
     * captured by the InternalDeviceListener defined below.
     *
     * @param deviceId device ID where to install the rules
     */
    private void insertFibTableFlowRules(DeviceId deviceId) {
        log.info("*****step3: insert fib deviceId={}", deviceId);
        final String s1 = "device:barefoot1";

        final String tableId = "IngressPipeImpl.l2_exact_table";
        if(s1.equals(deviceId.toString())){
            log.info("Add1"+deviceId.toString());

            insertMulticastGroup(deviceId);
            insertMulticastFlowRules(deviceId);

            final PiAction sendAction = PiAction.builder()
                    .withId(PiActionId.of("IngressPipeImpl.set_egress_port"))
                    .withParameter(new PiActionParam(PiActionParamId.of("port_num"), 140))
                    .build();

            final PiCriterion l2Match = PiCriterion.builder()
                    .matchExact(PiMatchFieldId.of("hdr.ethernet.dst_addr"), MacAddress.valueOf("00:00:00:00:00:1b").toBytes())
                    .build();

            final FlowRule rule1 = Utils.buildFlowRule(
                    deviceId, appId, tableId,
                    l2Match, sendAction);

            // Insert rules.
            flowRuleService.applyFlowRules(rule1);
        }
        else{
            log.info("Add2"+deviceId.toString());

            final PiAction sendAction = PiAction.builder()
                    .withId(PiActionId.of("IngressPipeImpl.set_egress_port"))
                    .withParameter(new PiActionParam(PiActionParamId.of("port_num"), 132))
                    .build();

            final PiCriterion l2Match = PiCriterion.builder()
                    .matchExact(PiMatchFieldId.of("hdr.ethernet.dst_addr"), MacAddress.valueOf("00:00:00:00:00:1b").toBytes())
                    .build();

            final FlowRule rule1 = Utils.buildFlowRule(
                    deviceId, appId, tableId,
                    l2Match, sendAction);

            // Insert rules.
            flowRuleService.applyFlowRules(rule1);
        }
    }

    private void sendToCpuTableFlowRules(DeviceId deviceId) {
        log.info("*****step3: insert fib deviceId={}", deviceId);

        final String tableId = "IngressPipeImpl.acl_table";

        log.info("Add ACL"+deviceId.toString());

        final PiAction sendToCpuAction = PiAction.builder()
                .withId(PiActionId.of("IngressPipeImpl.send_to_cpu"))
                .build();

        final PiCriterion AclMatch = PiCriterion.builder()
                .matchTernary(PiMatchFieldId.of("hdr.ethernet.dst_addr"),
                        MacAddress.valueOf("00:00:00:00:00:1d").toBytes(),
                        MacAddress.valueOf("FF:FF:FF:FF:FF:FF").toBytes())
                .build();

        final FlowRule rule1 = Utils.buildFlowRule(
                deviceId, appId, tableId,
                AclMatch, sendToCpuAction);

        // Insert rules.
        flowRuleService.applyFlowRules(rule1);

    }

    private void aclCntTableFlowRules(DeviceId deviceId) {

        final String tableId = "IngressPipeImpl.count_table";

        log.info("Add ACL"+deviceId.toString());

        final PiAction countAclAction = PiAction.builder()
                .withId(PiActionId.of("IngressPipeImpl.count_nop"))
                .build();

        //GEO
        final PiCriterion geoAclMatch = PiCriterion.builder()
                .matchTernary(PiMatchFieldId.of("hdr.ethernet.ether_type"),0x8947,0xffff)
                .build();

        final FlowRule geoRule = Utils.buildFlowRule(deviceId, appId, tableId, geoAclMatch, countAclAction);
        log.info("Add GEO Cnt ACL Rule"+geoRule.id().toString());
        CntFlows.add(new CntFlow("GEO Pro", geoRule.id()));

        //NDN
        final PiCriterion NdnAclMatch = PiCriterion.builder()
                .matchTernary(PiMatchFieldId.of("hdr.ethernet.ether_type"),0x0809,0xffff)
                .build();

        final FlowRule ndnRule = Utils.buildFlowRule(deviceId, appId, tableId, NdnAclMatch, countAclAction);
        log.info("Add NDN Cnt ACL Rule"+ndnRule.id().toString());
        CntFlows.add(new CntFlow("NDN Pro", ndnRule.id()));


        final PiCriterion ipv4AclMatch = PiCriterion.builder()
                .matchTernary(PiMatchFieldId.of("hdr.ethernet.ether_type"),0x0800,0xffff)
                .build();

        final FlowRule ipv4Rule = Utils.buildFlowRule(deviceId, appId, tableId, ipv4AclMatch, countAclAction);
        log.info("Add IPV4 Cnt ACL Rule"+ipv4Rule.id().toString());
        CntFlows.add(new CntFlow("IPV4 Pro", ipv4Rule.id()));

        // Insert rules.
        flowRuleService.applyFlowRules(geoRule, ndnRule, ipv4Rule);

    }

    private class CntFlow implements Comparable{
        private String cntName;
        private FlowId cntFlowId;

        CntFlow(String name, FlowId flowId){
            this.cntName = name;
            this.cntFlowId = flowId;
        }

        public String getName(){
            return this.cntName;
        }

        public FlowId getFlowId(){
            return this.cntFlowId;
        }

        @Override
        public int compareTo(Object obj) {
            CntFlow f = (CntFlow) obj;
            return this.cntFlowId.id().compareTo(f.cntFlowId.id());
        }
    }

    public class PacketCounter implements Comparable {
        private long packets = 0;
        private long bytes = 0;
        private String name = "";

        PacketCounter(String name){
            this.name=name;
        }

        public String getName(){
            return name;
        }

        public long getPackets(){
            return packets;
        }

        public long getBytes(){
            return bytes;
        }

        public void setPackets(long packets){
            this.packets = packets;
        }

        public void setBytes(long bytes){
            this.bytes = bytes;
        }

        @Override
        public int compareTo(Object obj) {
            PacketCounter p = (PacketCounter) obj;
            return this.name.compareTo(p.name);
        }
    }

    /**
     *  增加对特定报文的计数，用于多模态报文统计
     * */
    public Set<PacketCounter> GetCntTableFlowRules(DeviceId deviceId) {

        Set<PacketCounter> newpacketCounters = new TreeSet<PacketCounter>();
        Iterable<FlowEntry> flows = flowRuleService.getFlowEntries(deviceId);

        for(FlowEntry element: flows){
            for(CntFlow cntflow : CntFlows){
               // log.info("flowid1: {} flowid2:{}, byte:{}", element.id(), cntflow.getFlowId());
                if(element.id().equals(cntflow.getFlowId())){
                    PacketCounter ret = new PacketCounter(cntflow.getName());
                    ret.bytes = element.bytes();
                    ret.packets = element.packets();
                    newpacketCounters.add(ret);
                   // log.info("name: {} packet:{}, byte:{}", element.id().toString(), element.packets(), element.bytes());
                }
            }
        }
        return newpacketCounters;
    }

    private void insertMulticastGroup(DeviceId deviceId) {

        // Replicate packets where we know hosts are attached.
        Set<PortNumber> ports = new HashSet<PortNumber>();
        //p4交换机上接口3/0 5/0 7/0对应的D_P号
        ports.add(PortNumber.portNumber(140));
        ports.add(PortNumber.portNumber(128));
        //ports.add(PortNumber.portNumber(136));

        log.info("****Adding multicast group with {} ports on {}...",
                ports, deviceId);

        // Forge group object.
        final GroupDescription multicastGroup = Utils.buildMulticastGroup(appId, deviceId, 1, ports);

        // Insert.
        groupService.addGroup(multicastGroup);
    }

    private void insertMulticastFlowRules(DeviceId deviceId) {
        final PiCriterion macBroadcastCriterion = PiCriterion.builder()
                //.matchExact(PiMatchFieldId.of("hdr.ethernet.etherType"), 0x8624)
                .matchTernary(  PiMatchFieldId.of("hdr.ethernet.dst_addr"),
                        MacAddress.valueOf("00:00:00:00:00:1c").toBytes(),
                        MacAddress.valueOf("FF:FF:FF:FF:FF:FF").toBytes())
                .build();

        // Action: set multicast group id
        final PiAction setMcastGroupAction = PiAction.builder()
                .withId(PiActionId.of("IngressPipeImpl.set_multicast_group"))
                .withParameter(new PiActionParam(PiActionParamId.of("gid"),1))
                .build();

        //  Build 2 flow rules.
        final String tableId = "IngressPipeImpl.l2_ternary_table";
        // ---- END SOLUTION ----


        final FlowRule rule2 = Utils.buildFlowRule(
                deviceId, appId, tableId,
                macBroadcastCriterion, setMcastGroupAction);

        // Insert rules.
        flowRuleService.applyFlowRules(rule2);
    }

    /**
     * Sets up L2 bridging on all devices known by ONOS and for which this ONOS
     * node instance is currently master.
     * <p>
     * This method is called at component activation.
     */
    private void setUpAllDevices() {
        deviceService.getAvailableDevices().forEach(device -> {
            log.info("*** step1 devices： {}...", device.id());
            if (mastershipService.isLocalMaster(device.id())) {
                log.info("*** step2 device is master Fib Table - Starting initial set up for {}...", device.id());
                //insertMulticastGroup(device.id());
                //insertMulticastFlowRules(device.id());
                //insertFibTableFlowRules(device.id());
                //sendToCpuTableFlowRules(device.id());
                //aclCntTableFlowRules(device.id());
            }
        });
    }
}
