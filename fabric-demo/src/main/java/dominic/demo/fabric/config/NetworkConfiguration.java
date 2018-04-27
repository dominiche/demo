package dominic.demo.fabric.config;

import org.hyperledger.fabric.sdk.NetworkConfig;

import java.io.File;

public class NetworkConfiguration {

    private static NetworkConfig networkConfig;

    static {
        try {
            networkConfig = NetworkConfig.fromYamlFile(new File("sdkintegration/network_configs/network-config.yaml"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static NetworkConfig networkConfig() {
        return networkConfig;
    }

}
