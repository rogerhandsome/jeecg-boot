package org.jeecg.modules.quartz.job;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.dto.message.MessageDTO;
import org.jeecg.common.constant.enums.MessageTypeEnum;
import org.jeecg.common.system.api.ISysBaseAPI;
import org.jeecg.modules.declaration.entity.CustomerDetailsEntity;
import org.jeecg.modules.declaration.service.CustomerDetailsService;
import org.jeecg.modules.system.entity.SysUser;
import org.jeecg.modules.system.service.ISysUserService;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 推送合同到期提醒定时任务
 *
 * @Author luojie
 */
@Slf4j
public class PushContactExpireMessageJob implements Job {

	@Resource
	private CustomerDetailsService customerDetailsService;

	@Resource
	private ISysBaseAPI sysBaseApi;

	@Resource
	private ISysUserService iSysUserService;

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
		log.info("开始合同到期提醒定时任务");
		log.info(" Job Execution key：" + jobExecutionContext.getJobDetail().getKey());

		QueryWrapper<CustomerDetailsEntity> queryWrapper = new QueryWrapper<>();
		queryWrapper.eq("cus_state", 1);
		queryWrapper.le("renewal_time", DateUtil.formatDate(DateUtil.offsetDay(new Date(), -370)));
		long count = customerDetailsService.count(queryWrapper);

		if (count > 0) {
			MessageDTO md = new MessageDTO();
			md.setType(MessageTypeEnum.QYWX.getType());
			md.setToAll(true);
			md.setTitle("合同到期提醒");
			md.setTemplateCode("sys_contact_expire");
			//企业微信
			String testData = "{\"count\":\"" + count + "\"}";
			Map<String, Object> data = JSON.parseObject(testData, Map.class);
			md.setData(data);
//			sysBaseApi.sendTemplateMessage(md);

			//系统消息
			md.setType(MessageTypeEnum.XT.getType());
			String toUsers = iSysUserService.list().stream().map(SysUser::getUsername).collect(Collectors.joining(","));
			md.setToUser(toUsers);
			md.setFromUser("system");
			sysBaseApi.sendTemplateMessage(md);
		}

		log.info("推送合同到期定时任务结束");
	}
}
