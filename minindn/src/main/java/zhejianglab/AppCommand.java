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

import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Completion;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.onosproject.cli.AbstractShellCommand;
import org.onosproject.cli.net.DeviceIdCompleter;

import org.onosproject.net.Device;
import org.onosproject.net.DeviceId;
import org.onosproject.net.device.DeviceService;

import java.util.Set;

/**
 * Sample Apache Karaf CLI command.
 */
@Service
@Command(scope = "onos", name = "getcnt",
        description = "Get diff types package counter")
public class AppCommand extends AbstractShellCommand {

    @Argument(index = 0, name = "uri", description = "Device ID",
            required = true, multiValued = false)
    @Completion(DeviceIdCompleter.class)
    String uri = null;

    @Override
    protected void doExecute() {
        OptFibTable app = get(OptFibTable.class);

        DeviceService deviceService = get(DeviceService.class);

        Device device = deviceService.getDevice(DeviceId.deviceId(uri));
        if (device == null) {
            print("Device \"%s\" is not found", uri);
            return;
        }

        Set<OptFibTable.PacketCounter> ret = app.GetCntTableFlowRules(device.id());

        if(ret.isEmpty()){
            print("Can not get Device \"%s\" cnt", uri);
            return;
        }

        for(OptFibTable.PacketCounter element : ret){
            print("%s: packets %s bytes %s",element.getName(), Long.toString(element.getPackets()), Long.toString(element.getBytes()));
        }
    }
}
