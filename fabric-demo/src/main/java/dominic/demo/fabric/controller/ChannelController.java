package dominic.demo.fabric.controller;

import dominic.common.base.ResultDTO;
import dominic.demo.fabric.config.HFClientConfiguration;
import dominic.demo.fabric.config.NetworkConfiguration;
import dominic.demo.fabric.utils.PathUtil;
import dominic.fabric.sdk.helper.ChannelHelper;
import lombok.extern.slf4j.Slf4j;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.HFClient;
import org.hyperledger.fabric.sdk.NetworkConfig;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;

@Slf4j
@RestController
@RequestMapping("channel")
public class ChannelController {

    private String channelConfigFilePath = PathUtil.parentPath + "sdkintegration/e2e-2Orgs/v1.1/";

    @RequestMapping("create/{channelName}")
    public ResultDTO<Integer> createChannel(@PathVariable("channelName") String channelName, @RequestParam String orgName) {
        NetworkConfig networkConfig = NetworkConfiguration.networkConfig();
        HFClient client = HFClientConfiguration.client();

//        NetworkConfig.OrgInfo clientOrganization = networkConfig.getClientOrganization();
//        String orgName = clientOrganization.getName();
        NetworkConfig.OrgInfo orgInfo = networkConfig.getOrganizationInfo(orgName);
        if (null == orgInfo) {
            return ResultDTO.failed("can't find organization " + orgName);
        }

        try {
            NetworkConfig.UserInfo peerAdmin = orgInfo.getPeerAdmin();
            client.setUserContext(peerAdmin);
            Channel newChannel = ChannelHelper.createNewChannel(networkConfig, client, channelName,
                    new File(channelConfigFilePath + channelName + ".tx"), orgName);

        } catch (Exception e) {
            log.error("create channel:{} fail: ", channelName, e);
            return ResultDTO.failed(String.format("create channel:%s fail: %s", channelName, e.getMessage()));
        }

        return ResultDTO.succeed();
    }

    @RequestMapping("resume/{channelName}")
    public ResultDTO<Integer> resumeChannel(@PathVariable("channelName") String name, @RequestParam String orgName) {
        NetworkConfig networkConfig = NetworkConfiguration.networkConfig();
        HFClient client = HFClientConfiguration.client();

        NetworkConfig.OrgInfo orgInfo = networkConfig.getOrganizationInfo(orgName);
        if (null == orgInfo) {
            return ResultDTO.failed("can't find organization " + orgName);
        }

        try {
            NetworkConfig.UserInfo peerAdmin = orgInfo.getPeerAdmin();
            client.setUserContext(peerAdmin);
            Channel newChannel = ChannelHelper.resumeChannel(networkConfig, client, name);
        } catch (Exception e) {
            log.error("create channel:{} fail: ", name, e);
            return ResultDTO.failed(String.format("create channel:%s fail: %s", name, e.getMessage()));
        }

        return ResultDTO.succeed();
    }
}
