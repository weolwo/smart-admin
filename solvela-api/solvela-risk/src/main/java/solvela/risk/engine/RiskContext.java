package solvela.risk.engine;

import lombok.Data;
import solvela.risk.PromotionConfig;
import solvela.risk.proposal.domain.command.ProposalRecordAddCommand;

@Data
public class RiskContext {

    private final ProposalRecordAddCommand request;
    private final PromotionConfig config;

    /**
     * 设备号，可能为 null。
     *
     * <p>🔴 它<b>不在 request 里</b>是有原因的：设备号从网关一路走请求头到这里
     * （见 {@code DeviceContract}），而 {@code ProposalRecordAddCommand} 是
     * 「要落库的提案数据」。两者的来源和生命周期都不一样。
     *
     * <p>用具名字段而不是原来注释里设想的 {@code Map<String,Object> extAttributes}：
     * 那个 map 少一个「字段名打错却编译通过」的保护，而这里每多一个维度就多一次打错的机会。
     */
    private final String deviceId;

    public RiskContext(ProposalRecordAddCommand request, PromotionConfig config, String deviceId) {
        this.request = request;
        this.config = config;
        this.deviceId = deviceId;
    }
}
