package org.jeecg.modules.quartz.job;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.dto.message.MessageDTO;
import org.jeecg.common.constant.enums.MessageTypeEnum;
import org.jeecg.common.system.api.ISysBaseAPI;
import org.jeecg.modules.declaration.entity.DeclarationDetailsEntity;
import org.jeecg.modules.declaration.service.DeclarationDetailsService;
import org.jeecg.modules.system.entity.SysUser;
import org.jeecg.modules.system.service.ISysUserService;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 推送申报提醒定时任务
 *
 * @Author luojie
 */
@Slf4j
public class PushDeclarationMessageJob implements Job {

	@Resource
	private DeclarationDetailsService declarationDetailsService;

	@Resource
	private ISysBaseAPI sysBaseApi;

	@Resource
	private ISysUserService iSysUserService;

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
		log.info("开始推送申报提醒定时任务");
		log.info(" Job Execution key：" + jobExecutionContext.getJobDetail().getKey());

		QueryWrapper<DeclarationDetailsEntity> queryWrapper = new QueryWrapper<>();
		queryWrapper.eq("state", 1);
		long count = declarationDetailsService.count(queryWrapper);

		if (count > 0) {
			MessageDTO md = new MessageDTO();
			md.setType(MessageTypeEnum.QYWX.getType());
			md.setToAll(true);
			md.setTitle("申报提醒");
			md.setTemplateCode("sys_ts_note");
			//企业微信
			String testData = "{\"count\":\"" + count + "\"}";
			Map<String, Object> data = JSON.parseObject(testData, Map.class);
			md.setData(data);
			sysBaseApi.sendTemplateMessage(md);

			//系统消息
			md.setType(MessageTypeEnum.XT.getType());
			md.setToAll(true);
			String toUsers = iSysUserService.list().stream().map(SysUser::getUsername).collect(Collectors.joining(","));
			md.setToUser(toUsers);
			md.setFromUser("system");
			sysBaseApi.sendTemplateMessage(md);
		}

		log.info("推送申报提醒定时任务结束");
	}
}
