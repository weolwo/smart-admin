package solvela.member.sms;

import solvela.member.api.SmsScene;

/**
 * 「还没接短信服务商」这个事实的<b>代码表达</b>。调用即抛。
 *
 * <h3>为什么要有这个类，而不是让 SmsSender 干脆没有实现</h3>
 * 没有实现的话，任何注入 {@code SmsSender} 的地方在启动时就 NoSuchBeanDefinitionException ——
 * 于是<b>整条短信链路连代码都写不下去</b>，dev 也起不来。
 *
 * <p>而做成「有一个会抛的实现」，链路的其余部分（限频、验码、注册校验）
 * 可以先写完、先测完，缺的只是最后那一步。dev / test 走 LOG 通道压根不会调到它。
 *
 * <p>🔴 <b>它不是降级，是一堵墙。</b>不要给它加「记个日志然后返回」的行为 ——
 * 那会让生产上「短信没发出去」变成一件静默的事：用户收不到码、注册失败，
 * 而服务端日志里一切正常。抛出来才会有告警。
 *
 * <h3>🔴 它<b>不是</b>一个 Spring bean，这一点踩过坑</h3>
 * 最初它写成 {@code @Component @ConditionalOnMissingBean(SmsSender.class)} ——
 * 那个组合<b>不成立</b>：{@code @ConditionalOnMissingBean} 只在
 * {@code @Bean} 方法（自动配置）上被求值，挂在被组件扫描拾取的 {@code @Component} 上时，
 * 条件在「还没扫到任何 bean」的时刻求值，结果是<b>连它自己都没被注册</b>。
 * 表现不是短信发不出去，是<b>全仓每一个 Spring 上下文都起不来</b>
 * （{@code NoSuchBeanDefinitionException: SmsSender}）—— 168 个测试同时变红。
 *
 * <p>所以现在由 {@link MemberSmsCodeService} 在构造时用 {@code ObjectProvider}
 * 兜底：有实现就用实现，没有就 new 一个本类。没有任何条件求值顺序的问题。
 *
 * <p>接好厂商之后，加一个自己的 {@code @Component SmsSender} 实现即可，
 * {@code ObjectProvider} 会拿到它，本类自动让位。
 *
 * @Date 2026-09-10
 */
public class UnavailableSmsSender implements SmsSender {

    @Override
    public boolean available() {
        return false;
    }

    @Override
    public void send(String phone, SmsScene scene, String code, long minutes) {
        throw new IllegalStateException(
                "还没有接入短信服务商，发不出短信。dev / test 请配 solvela.member.code.sms-transport=LOG "
                        + "把验证码打进日志；生产请实现 SmsSender 并接好厂商（签名与模板要提前报备）。");
    }
}
