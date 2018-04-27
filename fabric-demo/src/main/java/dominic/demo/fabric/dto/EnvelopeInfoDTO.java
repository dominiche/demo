package dominic.demo.fabric.dto;

import lombok.Builder;
import lombok.Data;
import org.hyperledger.fabric.sdk.BlockInfo;

import java.util.Date;
import java.util.List;

@Data
@Builder
public class EnvelopeInfoDTO {

    private String transactionID;
    private String channelId;
    private Date transactionTimestamp;
    private String submitterMSPID;
    private BlockInfo.EnvelopeType envelopeType;
    private List<TransactionActionInfoDTO> transactionActionInfoDTOList;
}
