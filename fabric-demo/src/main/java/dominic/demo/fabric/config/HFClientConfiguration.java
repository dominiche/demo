package dominic.demo.fabric.config;

import org.hyperledger.fabric.sdk.HFClient;
import org.hyperledger.fabric.sdk.security.CryptoSuite;

public class HFClientConfiguration {

    private static HFClient client;

    static {
        try {
            client = HFClient.createNewInstance();
            client.setCryptoSuite(CryptoSuite.Factory.getCryptoSuite());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static HFClient client() {
        return client;
    }

}
