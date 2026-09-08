package solvela.admin.enums;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import solvela.admin.constant.UserTypeEnum;
import solvela.admin.module.system.job.api.domain.SolvelaJobLogQueryForm;
import solvela.admin.module.system.job.api.domain.SolvelaJobLogVO;
import solvela.admin.module.system.job.constant.SolvelaJobExecuteStatusEnum;
import solvela.admin.module.system.job.repository.SolvelaJobLogDao;
import solvela.admin.module.system.job.repository.domain.SolvelaJobLogEntity;
import solvela.admin.module.system.loginlog.LoginLogDao;
import solvela.admin.module.system.loginlog.domain.LoginLogQueryForm;
import solvela.admin.module.system.loginlog.domain.LoginLogVO;
import solvela.enums.LoginLogResultEnum;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A 桶各列改成枚举之后的真实验收（连数据库，只读）。
 *
 * <p>与 {@link DataTracerEnumMappingTest} 同样的三条路径，但覆盖数据量更大的几张表：
 * {@code t_solvela_job_log.status} 跨 6 个取值，{@code t_login_log.user_type} 跨 2 个 ——
 * 空表证明不了任何事。
 *
 * <p>⚠️ 这里原来写着「563 行 / 196 行」。行数会涨，写进注释只会过期；
 * 更要命的是下面两条用例把它当成了分页大小的依据 —— 见 {@link #pageCovering}。
 *
 * <p>额外钉住 {@code @EnumSerialize}：job log 的 VO 上挂着它，
 * 输出必须仍然是 {@code {"status":2,"statusDesc":"成功"}}，
 * 而不是字段类型一换 desc 就悄悄变 null。
 *
 * @Author alaric
 * @Date 2026-08-29
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
class BucketAEnumMappingTest {

    @Autowired
    private SolvelaJobLogDao jobLogDao;

    @Autowired
    private LoginLogDao loginLogDao;

    private final JsonMapper mapper = JsonMapper.builder().build();

    // ------------------------------------------------------------ t_solvela_job_log.status

    @Test
    @DisplayName("job log：实体的 status 能从 int 列装配成枚举")
    void jobLog实体装配() {
        List<SolvelaJobLogEntity> list = jobLogDao.selectList(null);
        assertFalse(list.isEmpty(), "t_solvela_job_log 没有数据，这条用例失去意义");
        for (SolvelaJobLogEntity e : list) {
            assertNotNull(e.getStatus(), "status 装配成了 null");
        }
    }

    @Test
    @DisplayName("job log：XML 的 resultType + 按枚举过滤，两条路一起验")
    void jobLog按状态查询() {
        int pageSize = pageCovering(jobLogDao.selectCount(null), "t_solvela_job_log");
        List<SolvelaJobLogVO> all = jobLogDao.query(new Page<>(1, pageSize), new SolvelaJobLogQueryForm());
        assertFalse(all.isEmpty(), "查不到数据，这条用例失去意义");
        for (SolvelaJobLogVO vo : all) {
            assertNotNull(vo.getStatus(), "VO.status 是 null —— resultType 没走到枚举 TypeHandler");
        }

        int sum = 0;
        for (SolvelaJobExecuteStatusEnum status : SolvelaJobExecuteStatusEnum.values()) {
            SolvelaJobLogQueryForm form = new SolvelaJobLogQueryForm();
            form.setStatus(status);
            List<SolvelaJobLogVO> hit = jobLogDao.query(new Page<>(1, pageSize), form);
            for (SolvelaJobLogVO vo : hit) {
                assertTrue(status == vo.getStatus(),
                        "按 " + status + " 查询却查出了 " + vo.getStatus() + "，条件没有正确下推");
            }
            sum += hit.size();
        }
        assertEquals(all.size(), sum,
                "按各状态分别查询的总数与不带条件的总数对不上，说明有行的 status 落在枚举之外");
    }

    @Test
    @DisplayName("job log：@EnumSerialize 仍然输出 statusDesc，字段换成枚举也不能变")
    void jobLogVO序列化带desc() {
        List<SolvelaJobLogVO> list = jobLogDao.query(new Page<>(1, 1), new SolvelaJobLogQueryForm());
        assertFalse(list.isEmpty());

        SolvelaJobLogVO vo = list.get(0);
        String json = mapper.writeValueAsString(vo);

        assertTrue(json.contains("\"status\":" + vo.getStatus().getValue()),
                "status 应该序列化成数字，实际：" + json);
        assertTrue(json.contains("\"statusDesc\":\"" + vo.getStatus().getDesc() + "\""),
                "statusDesc 丢了或不对，实际：" + json);
    }

    // ------------------------------------------------------------ t_login_log.user_type

    @Test
    @DisplayName("login log：XML 里 #{query.userType} 要按 value 下推")
    void loginLog按用户类型查询() {
        int pageSize = pageCovering(loginLogDao.selectCount(null), "t_login_log");
        List<LoginLogVO> all = loginLogDao.queryByPage(new Page<>(1, pageSize), new LoginLogQueryForm());
        assertFalse(all.isEmpty(), "t_login_log 没有数据，这条用例失去意义");
        for (LoginLogVO vo : all) {
            assertNotNull(vo.getUserType(), "VO.userType 是 null");
        }

        int sum = 0;
        for (UserTypeEnum userType : UserTypeEnum.values()) {
            LoginLogQueryForm form = new LoginLogQueryForm();
            form.setUserType(userType);
            List<LoginLogVO> hit = loginLogDao.queryByPage(new Page<>(1, pageSize), form);
            for (LoginLogVO vo : hit) {
                assertTrue(userType == vo.getUserType(),
                        "按 " + userType + " 查询却查出了 " + vo.getUserType());
            }
            sum += hit.size();
        }
        assertEquals(all.size(), sum, "分类型查询的总数与总量对不上");
    }

    @Test
    @DisplayName("login log：login_result 统一口径后仍然 0-成功 1-失败 2-退出")
    void loginResult口径() {
        List<LoginLogVO> all = loginLogDao.queryByPage(new Page<>(1, 500), new LoginLogQueryForm());
        assertFalse(all.isEmpty(), "t_login_log 没有数据，这条用例失去意义");

        for (LoginLogVO vo : all) {
            assertNotNull(vo.getLoginResult(), "loginResult 装配成了 null");
        }

        // 库里 0×170 / 1×15 / 2×11：成功一定是最多的那个。
        // 这条断言的意义在于：如果哪天有人把 0/1 调回去，成功与失败的比例会立刻反转过来。
        long success = all.stream().filter(v -> v.getLoginResult() == LoginLogResultEnum.LOGIN_SUCCESS).count();
        long fail = all.stream().filter(v -> v.getLoginResult() == LoginLogResultEnum.LOGIN_FAIL).count();
        assertTrue(success > fail,
                "登录成功(" + success + ") 居然不比失败(" + fail + ") 多 —— 0/1 口径多半被改反了");
    }

    // ------------------------------------------------------------ 分页大小

    /**
     * 单表全量比对时，最多愿意拉进内存的行数。
     *
     * <p>超过就该换验法了（比如直接查 {@code SELECT DISTINCT status}），而不是把表拖进 JVM。
     * 到那一天让用例带着这句话失败，比让它悄悄退化成「只比对前 N 行」强。
     */
    private static final long MAX_ROWS_FOR_FULL_SCAN = 50_000;

    /**
     * 按表里的真实行数算一个<b>不会被截断</b>的页大小。
     *
     * <p>🔴 这两条用例原来写死 1000 / 500，而它们比较的是「不带条件查出的条数」与
     * 「按各取值分别查出的条数之和」—— <b>前者被分页封顶，后者不会</b>。
     * 表一涨过那个数，比较就必然不成立。
     *
     * <p>更糟的是它挂的时候会说「有行的 status 落在枚举之外」，指向一个不存在的问题：
     * {@code t_solvela_job_log} 于 2026-09-08 涨过 1000 行时就是这么挂的，
     * 报的是 {@code expected: <1000> but was: <1001>} —— 1000 正是那个页大小。
     *
     * <p>所以页大小必须<b>跟着表走</b>。多给 100 是留给「取行数与真正查询之间又插进来几行」
     * 的余量：这两张都是日志表，定时任务随时在写。
     */
    private static int pageCovering(Long total, String table) {
        assertNotNull(total, table + " 的行数查不出来");
        assertTrue(total < MAX_ROWS_FOR_FULL_SCAN,
                table + " 已有 " + total + " 行，超过全量比对的上限 " + MAX_ROWS_FOR_FULL_SCAN
                        + "。这条用例的做法（全表拉进内存逐一比对）已经不合适了，"
                        + "换成按 DISTINCT 取值比对，或给这张日志表加保留策略。");
        return (int) (total + 100);
    }
}
