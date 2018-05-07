package dominic.demo.fabric.controller;

import com.alibaba.fastjson.JSON;
import dominic.common.base.ResultDTO;
import dominic.demo.fabric.config.HFClientConfiguration;
import dominic.demo.fabric.dto.chaincode.ChaincodeIdDTO;
import dominic.demo.fabric.dto.chaincode.ChaincodeInstallDTO;
import dominic.demo.fabric.dto.chaincode.ChaincodeInstantiateDTO;
import dominic.demo.fabric.dto.chaincode.ChaincodeTransactDTO;
import dominic.fabric.sdk.helper.ChaincodeHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.hyperledger.fabric.sdk.*;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping(value = "chaincode/custom", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class ChaincodeCustomController {

    @RequestMapping("install")
    public ResultDTO<Integer> installChaincode(@RequestBody ChaincodeInstallDTO dto) {
        if (null == dto || StringUtils.isEmpty(dto.getChannelName())
                || StringUtils.isEmpty(dto.getChaincodeSourceLocation())
                || StringUtils.isEmpty(dto.getChaincodeLanguage())) {
            return ResultDTO.failed("illegal arguments!!");
        }

        String channelName = dto.getChannelName();
        ChaincodeID chaincodeId = getChaincodeId(dto.getChaincodeId());
        if (null == chaincodeId) return ResultDTO.failed("illegal chaincodeId!!");

        HFClient client = HFClientConfiguration.client();
        Channel channel = client.getChannel(channelName);
        if (null == channel) {
            return ResultDTO.failed("can't find channel " + channelName);
        }

        TransactionRequest.Type chaincodeLanguage;
        String toLowerCase = dto.getChaincodeLanguage().toLowerCase();
        if ("go".equals(toLowerCase)) {
            chaincodeLanguage = TransactionRequest.Type.GO_LANG;
        } else if ("java".equals(toLowerCase)) {
            chaincodeLanguage = TransactionRequest.Type.JAVA;
            //chaincodePath must be null for Java chaincode
            chaincodeId = ChaincodeID.newBuilder().setName(chaincodeId.getName())
                    .setVersion(chaincodeId.getVersion()).build();
        } else if ("node".equals(toLowerCase)) {
            chaincodeLanguage = TransactionRequest.Type.NODE;
        } else {
            return ResultDTO.failed("chaincodeLanguage not supported!!");
        }
        ChaincodeHelper chaincodeHelper = ChaincodeHelper.getHelper(channel);
        try {
            InstallProposalRequest installProposalRequest = client.newInstallProposalRequest();
            installProposalRequest.setChaincodeID(chaincodeId);
            installProposalRequest.setChaincodeSourceLocation(new File(dto.getChaincodeSourceLocation()));
//            installProposalRequest.setChaincodeVersion(ccVersion);
            installProposalRequest.setChaincodeLanguage(chaincodeLanguage);

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

    private ChaincodeID getChaincodeId(ChaincodeIdDTO chaincodeIdDTO) {
        if(null == chaincodeIdDTO
                || StringUtils.isEmpty(chaincodeIdDTO.getName())
                || StringUtils.isEmpty(chaincodeIdDTO.getPath())
                || StringUtils.isEmpty(chaincodeIdDTO.getVersion())) {
            return null;
        }
        return ChaincodeID.newBuilder().setName(chaincodeIdDTO.getName())
                .setVersion(chaincodeIdDTO.getVersion()).setPath(chaincodeIdDTO.getPath()).build();
    }

    @RequestMapping("instantiate")
    public ResultDTO<String> instantiate(@RequestBody ChaincodeInstantiateDTO dto) {
        if (null == dto || StringUtils.isEmpty(dto.getChannelName())
                || StringUtils.isEmpty(dto.getChaincodeEndorsementPolicyPath())
//                || StringUtils.isEmpty(dto.getFcn())
//                || ArrayUtils.isEmpty(dto.getArgs())
                ) {
            return ResultDTO.failed("illegal arguments!!");
        }

        String channelName = dto.getChannelName();
        ChaincodeID chaincodeId = getChaincodeId(dto.getChaincodeId());
        if (null == chaincodeId) return ResultDTO.failed("illegal chaincodeId!!");

        HFClient client = HFClientConfiguration.client();
        Channel channel = client.getChannel(channelName);
        if (null == channel) {
            return ResultDTO.failed("can't find channel " + channelName);
        }

        ChaincodeHelper chaincodeHelper = ChaincodeHelper.getHelper(channel);
        try {
            InstantiateProposalRequest instantiateProposalRequest = client.newInstantiationProposalRequest();
//        instantiateProposalRequest.setProposalWaitTime(testConfig.getProposalWaitTime());
            instantiateProposalRequest.setChaincodeID(chaincodeId);
            instantiateProposalRequest.setFcn("init");
            if (ArrayUtils.isNotEmpty(dto.getArgs())) {
                instantiateProposalRequest.setArgs(dto.getArgs());
            }

            Map<String, byte[]> tm = new HashMap<>();
            tm.put("HyperLedgerFabric", "InstantiateProposalRequest:JavaSDK".getBytes(StandardCharsets.UTF_8));
            tm.put("method", "InstantiateProposalRequest".getBytes(StandardCharsets.UTF_8));
            instantiateProposalRequest.setTransientMap(tm);
            ChaincodeEndorsementPolicy chaincodeEndorsementPolicy = new ChaincodeEndorsementPolicy();
            chaincodeEndorsementPolicy.fromYamlFile(new File(dto.getChaincodeEndorsementPolicyPath()));
            instantiateProposalRequest.setChaincodeEndorsementPolicy(chaincodeEndorsementPolicy);

            ResultDTO<BlockEvent.TransactionEvent> eventResultDTO = chaincodeHelper.instantiate(instantiateProposalRequest);
            if (!eventResultDTO.isSuccess()) {
                log.error("instantiate fail: {}", eventResultDTO.getMessage());
                return ResultDTO.failed(String.format("instantiate chaincode fail: %s", eventResultDTO.getMessage()));
            }

            BlockEvent.TransactionEvent event = eventResultDTO.getModel();
            return ResultDTO.succeedWith("instantiate chaincode success, transactionId is " + event.getTransactionID());
        } catch (Exception e) {
            log.error("instantiate chaincode on channel:{} fail: ", channelName, e);
            return ResultDTO.failed(String.format("instantiate chaincode on channel:%s fail: %s", channelName, e.getMessage()));
        }
    }

    @RequestMapping("transact")
    public ResultDTO<String> transact(@RequestBody ChaincodeTransactDTO dto) {
        if (null == dto || StringUtils.isEmpty(dto.getChannelName())
                || StringUtils.isEmpty(dto.getFcn())
//                || ArrayUtils.isEmpty(dto.getArgs())
                ) {
            return ResultDTO.failed("illegal arguments!!");
        }

        String channelName = dto.getChannelName();
        ChaincodeID chaincodeId = getChaincodeId(dto.getChaincodeId());
        if (null == chaincodeId) return ResultDTO.failed("illegal chaincodeId!!");

        HFClient client = HFClientConfiguration.client();
        Channel channel = client.getChannel(channelName);
        if (null == channel) {
            return ResultDTO.failed("can't find channel " + channelName);
        }

        ChaincodeHelper chaincodeHelper = ChaincodeHelper.getHelper(channel);
        try {
            TransactionProposalRequest transactionProposalRequest = client.newTransactionProposalRequest();
            transactionProposalRequest.setChaincodeID(chaincodeId);
//            transactionProposalRequest.setChaincodeLanguage(TransactionRequest.Type.GO_LANG);
            transactionProposalRequest.setFcn(dto.getFcn());
//        transactionProposalRequest.setProposalWaitTime(testConfig.getProposalWaitTime());
            if (ArrayUtils.isNotEmpty(dto.getArgs())) {
                transactionProposalRequest.setArgs(dto.getArgs());
            }

            ResultDTO<BlockEvent.TransactionEvent> eventResultDTO = chaincodeHelper.transact(transactionProposalRequest);
            if (!eventResultDTO.isSuccess()) {
                log.error("chaincode transaction fail, chaincodeId: {}, fcn: {}, args: {}, messages: ",
                        JSON.toJSONString(chaincodeId), transactionProposalRequest.getFcn(), transactionProposalRequest.getArgs(), eventResultDTO.getMessage());
                return ResultDTO.failed(String.format("chaincode transaction fail: %s", eventResultDTO.getMessage()));
            }

            BlockEvent.TransactionEvent event = eventResultDTO.getModel();
            return ResultDTO.succeedWith("Transaction success, transactionId is " + event.getTransactionID());
        } catch (Exception e) {
            log.error("chaincode transaction fail, chaincodeId: {} ",
                    JSON.toJSONString(chaincodeId), e);
            return ResultDTO.failed(String.format("instantiate chaincode on channel:%s fail: %s", channelName, e.getMessage()));
        }
    }

    @RequestMapping("queryInfoByChaincode")
    public ResultDTO<Collection<String>> queryInfoByChaincode(@RequestBody ChaincodeTransactDTO dto) {
        if (null == dto || StringUtils.isEmpty(dto.getChannelName())
//                || StringUtils.isEmpty(dto.getFcn())
//                || ArrayUtils.isEmpty(dto.getArgs())
                ) {
            return ResultDTO.failed("illegal arguments!!");
        }

        String channelName = dto.getChannelName();
        ChaincodeID chaincodeId = getChaincodeId(dto.getChaincodeId());
        if (null == chaincodeId) return ResultDTO.failed("illegal chaincodeId!!");

        HFClient client = HFClientConfiguration.client();
        Channel channel = client.getChannel(channelName);
        if (null == channel) {
            return ResultDTO.failed("can't find channel " + channelName);
        }

        ChaincodeHelper chaincodeHelper = ChaincodeHelper.getHelper(channel);
        QueryByChaincodeRequest queryByChaincodeRequest = client.newQueryProposalRequest();
        queryByChaincodeRequest.setChaincodeID(chaincodeId);
        queryByChaincodeRequest.setFcn("query");
        if (ArrayUtils.isNotEmpty(dto.getArgs())) {
            queryByChaincodeRequest.setArgs(dto.getArgs());
        }

        return chaincodeHelper.queryInfoByChaincode(queryByChaincodeRequest);
    }
}
