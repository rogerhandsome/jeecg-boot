package org.jeecg.modules.quartz.job;

import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.system.service.impl.ThirdAppWechatEnterpriseServiceImpl;
import org.jeecg.modules.system.vo.thirdapp.SyncInfoVo;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import javax.annotation.Resource;

/**
 * 同步企业微信用户定时任务
 *
 * @Author luojie
 */
@Slf4j
public class SyncUserJob implements Job {

    @Resource
    ThirdAppWechatEnterpriseServiceImpl wechatEnterpriseService;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        log.info(" Job Execution key：" + jobExecutionContext.getJobDetail().getKey());
        SyncInfoVo syncInfoVo = wechatEnterpriseService.syncThirdAppUserToLocal();
        if (syncInfoVo.getFailInfo().size() == 0) {
            log.info("同步成功:{}", syncInfoVo);
        } else {
            log.info("同步失败:{}", syncInfoVo);
        }
    }
}
