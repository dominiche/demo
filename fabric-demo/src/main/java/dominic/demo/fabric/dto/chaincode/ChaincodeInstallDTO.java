package dominic.demo.fabric.dto.chaincode;

import lombok.Data;

@Data
public class ChaincodeInstallDTO {
    private String channelName;
    private ChaincodeIdDTO chaincodeId;
//    private String chaincodeVersion;
    private String chaincodeLanguage;
    private String chaincodeSourceLocation;
}
