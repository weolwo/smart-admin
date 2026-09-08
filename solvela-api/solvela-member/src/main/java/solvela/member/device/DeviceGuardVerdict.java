package solvela.member.device;

/**
 * 设备闸门的判定结果。
 *
 * <p>形状照 {@code RiskResult} 来：放行不带任何信息，拦截带上<b>编码 + 还要等多久</b>。
 * 不带文案 —— 说什么由调用方决定，理由同 {@code AuthFailReason} 的类注释。
 *
 * @param allowed           是否放行。<b>dry-run 期间恒为 true</b>，命中与否看 {@link #rule}
 * @param rule              命中的规则；没命中为 null
 * @param retryAfterSeconds 还要等多久，仅命中时有意义
 */
public record DeviceGuardVerdict(boolean allowed, DeviceGuardRule rule, long retryAfterSeconds) {

    private static final DeviceGuardVerdict PASS = new DeviceGuardVerdict(true, null, 0L);

    public static DeviceGuardVerdict pass() {
        return PASS;
    }

    /**
     * 命中了规则。
     *
     * <p>🔴 {@code allowed} 与「有没有命中」是<b>两件事</b>：dry-run 期间命中了照样放行。
     * 调用方要判断的是 {@code allowed}，要记日志/做统计的是 {@link #rule}。
     * 把它们合成一个布尔，dry-run 这一档就没法表达了。
     */
    public static DeviceGuardVerdict hit(DeviceGuardRule rule, long retryAfterSeconds, boolean allowed) {
        return new DeviceGuardVerdict(allowed, rule, retryAfterSeconds);
    }

    /** 是否命中了某条规则（不管最终放没放行）。 */
    public boolean hit() {
        return rule != null;
    }
}
