package net.lab1024.sa.marketing.ledger.transaction.manager;

import net.lab1024.sa.marketing.ledger.transaction.dao.MemberAssetTransactionDao;
import net.lab1024.sa.marketing.ledger.transaction.domain.entity.MemberAssetTransaction;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
/**
 * 账务域-资产变动交易明细表  Manager
 *
 * @Author weolwo
 * @Date 2026-04-03 17:11:19
 * @Copyright weolwo
 */
@RequiredArgsConstructor
@Service
public class MemberAssetTransactionManager extends ServiceImpl<MemberAssetTransactionDao, MemberAssetTransaction> {


}
