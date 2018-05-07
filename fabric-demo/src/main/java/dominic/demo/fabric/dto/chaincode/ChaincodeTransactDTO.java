package dominic.demo.fabric.dto.chaincode;

import lombok.Data;

@Data
public class ChaincodeTransactDTO {
    private String channelName;
    private ChaincodeIdDTO chaincodeId;
    private String fcn;
    private String[] args;
}
