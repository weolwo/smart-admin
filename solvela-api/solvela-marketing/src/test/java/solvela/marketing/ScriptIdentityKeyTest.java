package solvela.marketing;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import solvela.marketing.api.ActivityPlayKeys;
import solvela.scriptengine.spi.ScriptIdentity;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 钉住「会员号」这个内部通道键在两处的字面量一致。
 *
 * <p>绑定方是活动域（{@code ActivityPlayContext.bindInto} 用 {@link ActivityPlayKeys#MEMBER_ID}），
 * 读取方是各域的脚本函数（{@code member_} / {@code prize_} 走 {@link ScriptIdentity#MEMBER_ID}）。
 *
 * <p>两个常量没法互相引用：{@code solvela-marketing-api} 是契约模块，
 * 让它依赖脚本引擎是错的方向；而引擎又不该认识营销域的键。所以只能靠一条断言把它们钉在一起。
 *
 * <p>🔴 不钉的话，改了一边的表现是<b>函数拿到 null 却不报错</b>，
 * 于是「查不到这个人的记录」被当成「他没有记录」，所有基于历史的限制静默失效。
 *
 * <p>本模块是唯一同时看得见这两个常量的地方，所以测试放在这里。
 */
class ScriptIdentityKeyTest {

    @Test
    @DisplayName("🔴 会员号的内部通道键：绑定方与读取方必须是同一个字面量")
    void member_id_key_must_be_identical_on_both_sides() {
        assertEquals(ScriptIdentity.MEMBER_ID, ActivityPlayKeys.MEMBER_ID,
                "改了一边就等于让 member_ / prize_ 域的函数静默拿到 null");
    }
}
