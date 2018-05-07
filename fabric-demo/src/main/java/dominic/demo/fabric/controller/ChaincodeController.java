package dominic.demo.fabric.controller;

import com.alibaba.fastjson.JSON;
import dominic.common.base.ResultDTO;
import dominic.demo.fabric.config.HFClientConfiguration;
import dominic.demo.fabric.utils.PathUtil;
import dominic.fabric.sdk.helper.ChaincodeHelper;
import lombok.extern.slf4j.Slf4j;
import org.hyperledger.fabric.sdk.*;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("chaincode")
public class ChaincodeController {

    private String chainCodeSourceLocation = PathUtil.parentPath + "sdkintegration/gocc/sample1";
    private String chaincodeEndorsementPolicyPath = PathUtil.parentPath + "sdkintegration/chaincodeendorsementpolicy.yaml";

    private String ccName = "example_cc_go";
    private String ccVersion = "1";
    private String ccPath = "github.com/example_cc";
    private ChaincodeID chaincodeID = ChaincodeID.newBuilder().setName(ccName)
            .setVersion(ccVersion).setPath(ccPath).build();

    @RequestMapping("installOn/{channelName}")
    public ResultDTO<Integer> installChaincode(@PathVariable("channelName") String channelName) {
        HFClient client = HFClientConfiguration.client();
        Channel channel = client.getChannel(channelName);
        if (null == channel) {
            return ResultDTO.failed("can't find channel " + channelName);
        }

        ChaincodeHelper chaincodeHelper = ChaincodeHelper.getHelper(channel);
        try {
            InstallProposalRequest installProposalRequest = client.newInstallProposalRequest();
            installProposalRequest.setChaincodeID(chaincodeID);
            installProposalRequest.setChaincodeSourceLocation(new File(chainCodeSourceLocation));
//            installProposalRequest.setChaincodeVersion(ccVersion);
//            installProposalRequest.setChaincodeLanguage(TransactionRequest.Type.GO_LANG);

            ResultDTO<Collection<ProposalResponse>> installResultDTO = chaincodeHelper.install(installProposalRequest, client);
            if (!installResultDTO.isSuccess()) {
                log.error("install fail: {}", installResultDTO.getMessage());
                return ResultDTO.failed(String.format("install chaincode fail: %s", installResultDTO.getMessage()));
            }
        } catch (Exception e) {
            log.error("install chaincode on channel:{} fail: ", channelName, e);
            return ResultDTO.failed(String.format("install chaincode on channel:%s fail: %s", channelName, e.getMessage()));
        }

        return ResultDTO.succeed();
    }

    @RequestMapping("instantiateOn/{channelName}")
    public ResultDTO<String> instantiate(@PathVariable("channelName") String name) {
        HFClient client = HFClientConfiguration.client();
        Channel channel = client.getChannel(name);
        if (null == channel) {
            return ResultDTO.failed("can't find channel " + name);
        }

        ChaincodeHelper chaincodeHelper = ChaincodeHelper.getHelper(channel);
        try {
            InstantiateProposalRequest instantiateProposalRequest = client.newInstantiationProposalRequest();
//        instantiateProposalRequest.setProposalWaitTime(testConfig.getProposalWaitTime());
            instantiateProposalRequest.setChaincodeID(chaincodeID);
            instantiateProposalRequest.setFcn("init");
            instantiateProposalRequest.setArgs("a", "500", "b", "200");

            Map<String, byte[]> tm = new HashMap<>();
            tm.put("HyperLedgerFabric", "InstantiateProposalRequest:JavaSDK".getBytes(StandardCharsets.UTF_8));
            tm.put("method", "InstantiateProposalRequest".getBytes(StandardCharsets.UTF_8));
            instantiateProposalRequest.setTransientMap(tm);
            ChaincodeEndorsementPolicy chaincodeEndorsementPolicy = new ChaincodeEndorsementPolicy();
            chaincodeEndorsementPolicy.fromYamlFile(new File(chaincodeEndorsementPolicyPath));
            instantiateProposalRequest.setChaincodeEndorsementPolicy(chaincodeEndorsementPolicy);

            ResultDTO<BlockEvent.TransactionEvent> eventResultDTO = chaincodeHelper.instantiate(instantiateProposalRequest);
            if (!eventResultDTO.isSuccess()) {
                log.error("instantiate fail: {}", eventResultDTO.getMessage());
                return ResultDTO.failed(String.format("instantiate chaincode fail: %s", eventResultDTO.getMessage()));
            }

            BlockEvent.TransactionEvent event = eventResultDTO.getModel();
            return ResultDTO.succeedWith("instantiate chaincode success, transactionId is " + event.getTransactionID());
        } catch (Exception e) {
            log.error("instantiate chaincode on channel:{} fail: ", name, e);
            return ResultDTO.failed(String.format("instantiate chaincode on channel:%s fail: %s", name, e.getMessage()));
        }
    }

    @RequestMapping("transact/{channelName}")
    public ResultDTO<String> transact(@PathVariable("channelName") String channelName) {
//        if (StringUtils.isEmpty(fcn) || CollectionUtils.isEmpty(args)) {
//            return ResultDTO.failed("argument illegal");
//        }

        HFClient client = HFClientConfiguration.client();
        Channel channel = client.getChannel(channelName);
        if (null == channel) {
            return ResultDTO.failed("can't find channel " + channelName);
        }

        ChaincodeHelper chaincodeHelper = ChaincodeHelper.getHelper(channel);
        try {
            TransactionProposalRequest transactionProposalRequest = client.newTransactionProposalRequest();
            transactionProposalRequest.setChaincodeID(chaincodeID);
//            transactionProposalRequest.setChaincodeLanguage(TransactionRequest.Type.GO_LANG);
            transactionProposalRequest.setFcn("move");
//        transactionProposalRequest.setProposalWaitTime(testConfig.getProposalWaitTime());
            transactionProposalRequest.setArgs("a", "b", "100");

            ResultDTO<BlockEvent.TransactionEvent> eventResultDTO = chaincodeHelper.transact(transactionProposalRequest);
            if (!eventResultDTO.isSuccess()) {
                log.error("chaincode transaction fail, chaincodeId: {}, fcn: {}, args: {}, messages: ",
                        JSON.toJSONString(chaincodeID), transactionProposalRequest.getFcn(), transactionProposalRequest.getArgs(), eventResultDTO.getMessage());
                return ResultDTO.failed(String.format("chaincode transaction fail: %s", eventResultDTO.getMessage()));
            }

            BlockEvent.TransactionEvent event = eventResultDTO.getModel();
            return ResultDTO.succeedWith("Transaction success, transactionId is " + event.getTransactionID());
        } catch (Exception e) {
            log.error("chaincode transaction fail, chaincodeId: {} ",
                    JSON.toJSONString(chaincodeID), e);
            return ResultDTO.failed(String.format("instantiate chaincode on channel:%s fail: %s", channelName, e.getMessage()));
        }
    }

    @RequestMapping("queryInfoByChaincode/{channelName}")
    public ResultDTO<Collection<String>> queryInfoByChaincode(@PathVariable("channelName") String channelName) {
        HFClient client = HFClientConfiguration.client();
        Channel channel = client.getChannel(channelName);
        if (null == channel) {
            return ResultDTO.failed("can't find channel " + channelName);
        }

        ChaincodeHelper chaincodeHelper = ChaincodeHelper.getHelper(channel);
        QueryByChaincodeRequest queryByChaincodeRequest = client.newQueryProposalRequest();
        queryByChaincodeRequest.setArgs("b");
        queryByChaincodeRequest.setFcn("query");
        queryByChaincodeRequest.setChaincodeID(chaincodeID);

        return chaincodeHelper.queryInfoByChaincode(queryByChaincodeRequest);
    }
}
