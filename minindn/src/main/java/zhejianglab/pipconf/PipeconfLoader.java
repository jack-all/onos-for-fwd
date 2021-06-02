package zhejianglab.pipconf;

import org.onosproject.driver.pipeline.DefaultSingleTablePipeline;
import org.onosproject.net.behaviour.Pipeliner;
import org.onosproject.net.pi.model.*;
import org.onosproject.net.pi.service.PiPipeconfService;
import org.onosproject.p4runtime.model.P4InfoParser;
import org.onosproject.p4runtime.model.P4InfoParserException;
import org.osgi.service.component.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;

import static org.onosproject.net.pi.model.PiPipeconf.ExtensionType.BMV2_JSON;
import static org.onosproject.net.pi.model.PiPipeconf.ExtensionType.P4_INFO_TEXT;

import static zhejianglab.AppConstants.PIPECONF_ID;

/**
 * Component that builds and register the pipeconf at app activation.
 */
@Component(immediate = true, service = PipeconfLoader.class)
public final class PipeconfLoader {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private static final URL P4INFO_URL = PipeconfLoader.class.getResource("/pipe/p4info.txt");
    private static final URL BMV2_JSON_URL = PipeconfLoader.class.getResource("/pipe/bmv2.json");

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    private PiPipeconfService piPipeconfService;

    @Activate
    public void activate() {
        // Registers the pipeconf at component activation.
        try {
            piPipeconfService.register(buildPipeconf());
        } catch (P4InfoParserException e) {
            log.error("Fail to register {} - Exception: {} - Cause: {}",
                    PIPECONF_ID, e.getMessage(), e.getCause().getMessage());
        }
    }

    @Deactivate
    public void deactivate() {
        // Unregisters the pipeconf at component deactivation.
        try {
            piPipeconfService.unregister(PIPECONF_ID);
        } catch (IllegalStateException e) {
            log.warn("{} haven't been registered", PIPECONF_ID);
        }
    }

    private PiPipeconf buildPipeconf() throws P4InfoParserException {

        final PiPipelineModel pipelineModel = P4InfoParser.parse(P4INFO_URL);

        return DefaultPiPipeconf.builder()
                .withId(PIPECONF_ID)
                .withPipelineModel(pipelineModel)
                .addBehaviour(PiPipelineInterpreter.class, InterpreterImpl.class)
                // Since mytunnel.p4 defines only 1 table, we re-use the existing single-table pipeliner.
                .addBehaviour(Pipeliner.class, DefaultSingleTablePipeline.class)
                .addExtension(P4_INFO_TEXT, P4INFO_URL)
                .addExtension(BMV2_JSON, BMV2_JSON_URL)
                .build();
    }
}
