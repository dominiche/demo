package dominic.demo.fabric.config;

import dominic.demo.fabric.utils.PathUtil;
import lombok.extern.slf4j.Slf4j;
import org.hyperledger.fabric.sdk.NetworkConfig;

import java.io.File;

@Slf4j
public class NetworkConfiguration {

    private static NetworkConfig networkConfig;

    static {
        try {
            networkConfig = NetworkConfig.fromYamlFile(new File(PathUtil.parentPath + "sdkintegration/network_configs/network-config.yaml"));
        } catch (Exception e) {
            log.info("load networkConfig fail: ", e);
            throw new RuntimeException(e);
        }
    }

    public static NetworkConfig networkConfig() {
        return networkConfig;
    }

}
