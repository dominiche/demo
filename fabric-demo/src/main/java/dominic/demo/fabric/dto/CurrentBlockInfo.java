package dominic.demo.fabric.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CurrentBlockInfo {

    private String channelName;

    private Long height;

    private String currentBlockHash;

    private String previousBlockHash;
}
