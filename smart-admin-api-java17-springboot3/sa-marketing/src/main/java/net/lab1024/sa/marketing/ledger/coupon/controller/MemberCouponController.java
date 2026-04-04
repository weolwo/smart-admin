package net.lab1024.sa.marketing.ledger.coupon.controller;

import net.lab1024.sa.base.common.domain.ValidateList;
import net.lab1024.sa.marketing.ledger.coupon.domain.entity.MemberCoupon;
import net.lab1024.sa.marketing.ledger.coupon.domain.form.MemberCouponAddForm;
import net.lab1024.sa.marketing.ledger.coupon.domain.form.MemberCouponQueryForm;
import net.lab1024.sa.marketing.ledger.coupon.domain.form.MemberCouponUpdateForm;
import net.lab1024.sa.marketing.ledger.coupon.domain.vo.MemberCouponVO;
import net.lab1024.sa.marketing.ledger.coupon.service.MemberCouponService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import net.lab1024.sa.base.common.domain.PageResult;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import cn.dev33.satoken.annotation.SaCheckPermission;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
/**
 * 资产域-会员优惠券实例表 Controller
 *
 * @Author weolwo
 * @Date 2026-04-03 17:15:39
 * @Copyright weolwo
 */
@RequiredArgsConstructor
@RestController
@Tag(name = "资产域-会员优惠券实例表")
@RequestMapping("/memberCoupon")
public class MemberCouponController {

    private final MemberCouponService Service;

    @Operation(summary = "分页查询")
    @PostMapping("/queryPage")
    @SaCheckPermission(":query")
    public ResponseDTO<PageResult<MemberCouponVO>> queryPage(@RequestBody @Valid MemberCouponQueryForm queryForm) {
        return ResponseDTO.ok(Service.queryPage(queryForm));
    }

    @Operation(summary = "添加")
    @PostMapping("/add")
    @SaCheckPermission(":add")
    public ResponseDTO<String> add(@RequestBody @Valid MemberCouponAddForm addForm) {
        return Service.add(addForm);
    }

    @Operation(summary = "更新")
    @PostMapping("/update")
    @SaCheckPermission(":update")
    public ResponseDTO<String> update(@RequestBody @Valid MemberCouponUpdateForm updateForm) {
        return Service.update(updateForm);
    }

    @Operation(summary = "批量删除")
    @PostMapping("/batchDelete")
    @SaCheckPermission(":delete")
    public ResponseDTO<String> batchDelete(@RequestBody ValidateList<Long> idList) {
        return Service.batchDelete(idList);
    }

    @Operation(summary = "单个删除")
    @GetMapping("/delete/{id}")
    @SaCheckPermission(":delete")
    public ResponseDTO<String> batchDelete(@PathVariable Long id) {
        return Service.delete(id);
    }
}
