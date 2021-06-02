package zhejianglab;

import org.onosproject.net.pi.model.PiPipeconfId;

public class AppConstants {
    public static final String APP_NAME = "org.onosproject.zhejianglab.minindn";
    public static final PiPipeconfId PIPECONF_ID = new PiPipeconfId("org.onosproject.zhejianglab.minindn");

    public static final int DEFAULT_FLOW_RULE_PRIORITY = 10;
    public static final int INITIAL_SETUP_DELAY = 2; // Seconds.
    public static final int CLEAN_UP_DELAY = 2000; // milliseconds
    public static final int DEFAULT_CLEAN_UP_RETRY_TIMES = 10;

    public static final int CPU_PORT_ID = 255;
    public static final int CPU_CLONE_SESSION_ID = 99;

}
