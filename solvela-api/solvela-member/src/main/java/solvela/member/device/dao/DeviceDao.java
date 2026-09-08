package solvela.member.device.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import solvela.member.Device;

/**
 * 设备注册表 Dao。
 *
 * <p>刻意<b>没有 XML</b>：眼下只有「插入一行」和「按 device_id 点查」两种用法，
 * {@link BaseMapper} 都覆盖得了。等到后台要做设备列表分页查询时再加，
 * 与 {@code MemberLoginLogDao} 的做法一致 —— 先有需求再有 SQL。
 *
 * <p>包在 {@code solvela.member.device.dao} 下是为了落进 {@code BizApplication} 已有的
 * {@code @MapperScan("solvela.member")} 范围里。<b>不要挪到别的顶层包</b>：
 * 挪了就得同时改 biz 与 admin 两处扫描配置，而漏改的表现是启动时
 * NoSuchBeanDefinitionException —— 2026-09-07 的包重命名正是这么差点翻车的。
 */
@Mapper
public interface DeviceDao extends BaseMapper<Device> {
}
