package dominic.demo.fabric.controller;

import com.google.common.collect.Lists;
import dominic.common.base.ResultDTO;
import dominic.demo.fabric.config.HFClientConfiguration;
import dominic.demo.fabric.dto.BlockInfoDTO;
import dominic.demo.fabric.dto.CurrentBlockInfo;
import dominic.demo.fabric.dto.EnvelopeInfoDTO;
import dominic.demo.fabric.dto.TransactionActionInfoDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.hyperledger.fabric.sdk.*;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import static org.hyperledger.fabric.sdk.BlockInfo.EnvelopeType.TRANSACTION_ENVELOPE;

@Slf4j
@RestController
@RequestMapping("blockInfo")
public class BlockInfoController {

    @RequestMapping("queryCurrentBlockInfo")
    public ResultDTO<CurrentBlockInfo> queryCurrentBlockInfo(@RequestParam String channelName) {
        HFClient client = HFClientConfiguration.client();
        Channel channel = client.getChannel(channelName);
        if (null == channel) {
            return ResultDTO.failed("can't find channel " + channelName);
        }

        try {
            BlockchainInfo blockchainInfo = channel.queryBlockchainInfo();
            long height = blockchainInfo.getHeight();
            String currentBlockHash = Hex.encodeHexString(blockchainInfo.getCurrentBlockHash());
            String previousBlockHash = Hex.encodeHexString(blockchainInfo.getPreviousBlockHash());
            CurrentBlockInfo currentBlockInfo = CurrentBlockInfo.builder()
                    .channelName(channelName).height(height).currentBlockHash(currentBlockHash)
                    .previousBlockHash(previousBlockHash)
                    .build();
            return ResultDTO.succeedWith(currentBlockInfo);
        } catch (Exception e) {
            log.error("queryCurrentBlockInfo fail, channelName={} ", e);
            return ResultDTO.failed("queryCurrentBlockInfo fail, " + e.getMessage());
        }
    }

    @RequestMapping("queryBlockContextByBlockHash")
    public String queryBlockContextByBlockHash(@RequestParam String channelName, @RequestParam String blockHash) {
        HFClient client = HFClientConfiguration.client();
        Channel channel = client.getChannel(channelName);
        if (null == channel) {
            return "can't find channel " + channelName;
        }

        try {
            BlockInfo blockInfo = channel.queryBlockByHash(Hex.decodeHex(blockHash));
            return blockInfo.getBlock().getData().getData(0).toStringUtf8();
        } catch (Exception e) {
            log.error("queryBlockByBlockHash fail, channelName={}, blockHash= ", channelName, blockHash, e);
            return "queryBlockByBlockHash fail, " + e.getMessage();
        }
    }

    @RequestMapping("queryBlockByBlockHash")
    public ResultDTO<BlockInfoDTO> queryBlockByBlockHash(@RequestParam String channelName, @RequestParam String blockHash) {
        HFClient client = HFClientConfiguration.client();
        Channel channel = client.getChannel(channelName);
        if (null == channel) {
            return ResultDTO.failed("can't find channel " + channelName);
        }

        try {
            BlockInfo blockInfo = channel.queryBlockByHash(Hex.decodeHex(blockHash));
            BlockInfoDTO blockInfoDTO = buildBlockInfoDTO(client, blockInfo);
            return ResultDTO.succeedWith(blockInfoDTO);
        } catch (Exception e) {
            log.error("queryBlockByBlockHash fail, channelName={}, blockHash= ", channelName, blockHash, e);
            return ResultDTO.failed("queryBlockByBlockHash fail, " + e.getMessage());
        }
    }

    @RequestMapping("queryBlockByBlockNumber")
    public ResultDTO<BlockInfoDTO> queryBlockByBlockNumber(@RequestParam String channelName, @RequestParam Long blockNumber) {
        HFClient client = HFClientConfiguration.client();
        Channel channel = client.getChannel(channelName);
        if (null == channel) {
            return ResultDTO.failed("can't find channel " + channelName);
        }

        try {
            BlockInfo blockInfo = channel.queryBlockByNumber(blockNumber);
            BlockInfoDTO blockInfoDTO = buildBlockInfoDTO(client, blockInfo);
            return ResultDTO.succeedWith(blockInfoDTO);
        } catch (Exception e) {
            log.error("queryBlockByBlockHash fail, channelName={} ", e);
            return ResultDTO.failed("queryBlockByBlockHash fail, " + e.getMessage());
        }
    }

    @RequestMapping("queryBlockByTransactionID")
    public ResultDTO<BlockInfoDTO> queryBlockByTransactionID(@RequestParam String channelName, @RequestParam String transactionID) {
        HFClient client = HFClientConfiguration.client();
        Channel channel = client.getChannel(channelName);
        if (null == channel) {
            return ResultDTO.failed("can't find channel " + channelName);
        }

        try {
            BlockInfo blockInfo = channel.queryBlockByTransactionID(transactionID);
            BlockInfoDTO blockInfoDTO = buildBlockInfoDTO(client, blockInfo);
            return ResultDTO.succeedWith(blockInfoDTO);
        } catch (Exception e) {
            log.error("queryBlockByBlockHash fail, channelName={} ", e);
            return ResultDTO.failed("queryBlockByBlockHash fail, " + e.getMessage());
        }
    }

    private BlockInfoDTO buildBlockInfoDTO(HFClient client, BlockInfo blockInfo) throws IOException, InvalidArgumentException {
        long blockNumber = blockInfo.getBlockNumber();
        String previousHash = Hex.encodeHexString(blockInfo.getPreviousHash());
        String currentHash = Hex.encodeHexString(SDKUtils.calculateBlockHash(client,
                blockNumber, blockInfo.getPreviousHash(), blockInfo.getDataHash()));
        List<EnvelopeInfoDTO> envelopeInfoDTOList = Lists.newArrayList();

        BlockInfoDTO blockInfoDTO = BlockInfoDTO.builder()
                .blockNumber(blockNumber)
                .previousBlockHash(previousHash)
                .currentBlockHash(currentHash)
                .envelopeInfoDTOList(envelopeInfoDTOList)
                .build();

        //fill envelopeInfoDTOList
        blockInfo.getEnvelopeInfos().forEach(envelopeInfo -> {
            String transactionID = envelopeInfo.getTransactionID();
            String channelId = envelopeInfo.getChannelId();
            Date timestamp = envelopeInfo.getTimestamp();
            BlockInfo.EnvelopeType envelopeInfoType = envelopeInfo.getType();
            String mspid = envelopeInfo.getCreator().getMspid();
            List<TransactionActionInfoDTO> transactionActionInfoDTOList = Lists.newArrayList();

            EnvelopeInfoDTO envelopeInfoDTO = EnvelopeInfoDTO.builder()
                    .transactionID(transactionID)
                    .channelId(channelId)
                    .transactionTimestamp(timestamp)
                    .envelopeType(envelopeInfoType)
                    .submitterMSPID(mspid)
                    .transactionActionInfoDTOList(transactionActionInfoDTOList)
                    .build();
            envelopeInfoDTOList.add(envelopeInfoDTO);

            //fill transactionActionInfoDTOList
            if (envelopeInfoType == TRANSACTION_ENVELOPE) {
                BlockInfo.TransactionEnvelopeInfo transactionEnvelopeInfo = (BlockInfo.TransactionEnvelopeInfo) envelopeInfo;
                transactionEnvelopeInfo.getTransactionActionInfos().forEach(transactionActionInfo -> {
                    String responseMessage = transactionActionInfo.getResponseMessage();
                    ChaincodeEvent transactionActionInfoEvent = transactionActionInfo.getEvent();
                    List<String> chainCodeInputArgs = Lists.newArrayList();
                    for (int i=0; i<transactionActionInfo.getChaincodeInputArgsCount(); ++i) {
                        chainCodeInputArgs.add(new String(transactionActionInfo.getChaincodeInputArgs(i)));
                    }
                    TransactionActionInfoDTO transactionActionInfoDTO = TransactionActionInfoDTO.builder()
                            .responseStatus(transactionActionInfo.getResponseStatus())
                            .responseMessage(responseMessage)
                            .chainCodeInputArgs(chainCodeInputArgs)
                            .txId(transactionActionInfoEvent==null?null:transactionActionInfoEvent.getTxId())
                            .chaincodeName(transactionActionInfoEvent==null?null:transactionActionInfoEvent.getChaincodeId())
//                                .txReadWriteSets()
                            .build();
                    transactionActionInfoDTOList.add(transactionActionInfoDTO);
                });
            }
        });
        return blockInfoDTO;
    }
}
