package dominic.demo.fabric.dto.chaincode;

import lombok.Data;

@Data
public class ChaincodeIdDTO {
    private String name;
    private String version;
    private String path;
}
