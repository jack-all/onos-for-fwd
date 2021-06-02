/*
 * Copyright 2021-present Open Networking Foundation
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

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.onosproject.net.Device;
import org.onosproject.net.DeviceId;
import org.onosproject.net.device.DeviceService;
import org.onosproject.rest.AbstractWebResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.util.Set;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.PathParam;

/**
 * Sample web resource.
 */
@Path("getcnt")
public class AppWebResource extends AbstractWebResource {

    /**
     * Get hello world greeting.
     *
     * @return 200 OK
     */
    private final Logger log = LoggerFactory.getLogger(getClass());

    @GET
    @Path("{id}")
    public Response getGreeting(@PathParam("id") String id) {
        OptFibTable app = get(OptFibTable.class);

        log.info("device id: {}", id);
        DeviceService deviceService = get(DeviceService.class);
        Device device = deviceService.getDevice(DeviceId.deviceId(id));

        Set<OptFibTable.PacketCounter> ret = app.GetCntTableFlowRules(device.id());

        if(ret.isEmpty()){
            ObjectNode node = mapper().createObjectNode().put(device.id().toString(), "NOT GET!");
            return ok(node).build();
        }

        ObjectMapper mapper = new ObjectMapper();

        ObjectNode root = mapper.createObjectNode();
        ArrayNode arrayNode = mapper.createArrayNode();

        for(OptFibTable.PacketCounter element : ret){
            ObjectNode node = mapper.createObjectNode();
            node.put("name", element.getName());
            node.put("packets", element.getPackets());
            node.put("bytes", element.getBytes());
            arrayNode.add(node);
            log.info("name:{},node:{}",element.getName(),node.toString());
        }

        root.set("CNT", arrayNode);
        return Response.ok(root.toString(), MediaType.APPLICATION_JSON_TYPE).build();
    }

}
