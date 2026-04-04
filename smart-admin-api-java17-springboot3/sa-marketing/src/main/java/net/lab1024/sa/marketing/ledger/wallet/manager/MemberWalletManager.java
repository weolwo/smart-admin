package net.lab1024.sa.marketing.ledger.wallet.manager;

import net.lab1024.sa.marketing.ledger.wallet.dao.MemberWalletDao;
import net.lab1024.sa.marketing.ledger.wallet.domain.entity.MemberWallet;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
/**
 * 账务域-会员资金/积分主账表  Manager
 *
 * @Author weolwo
 * @Date 2026-04-03 17:17:33
 * @Copyright weolwo
 */
@RequiredArgsConstructor
@Service
public class MemberWalletManager extends ServiceImpl<MemberWalletDao, MemberWallet> {


}
