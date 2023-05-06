package org.jeecg.modules.quartz.job;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.declaration.entity.CustomerDetailsEntity;
import org.jeecg.modules.declaration.entity.DeclarationDetailsEntity;
import org.jeecg.modules.declaration.entity.DeclarationMethodEntity;
import org.jeecg.modules.declaration.service.CustomerDetailsService;
import org.jeecg.modules.declaration.service.DeclarationDetailsService;
import org.jeecg.modules.declaration.service.DeclarationMethodService;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 生成申报明细定时任务
 *
 * @Author luojie
 */
@Slf4j
public class GenDeclarationJob implements Job {

	@Resource
	private DeclarationMethodService declarationMethodService;

	@Resource
	private CustomerDetailsService customerDetailsService;

	@Resource
	private DeclarationDetailsService declarationDetailsService;

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
		log.info("开始生成申报明细定时任务");
		log.info(" Job Execution key：" + jobExecutionContext.getJobDetail().getKey());

		//查出所有申报方式
		List<DeclarationMethodEntity> list = declarationMethodService.list();
		List<DeclarationDetailsEntity> detailsEntities = new ArrayList<>();
		Date date = new Date();
		int month = Integer.parseInt(DateUtil.format(date, "yyyyMMdd").substring(0, 6));
		list.forEach(declarationMethodEntity -> {
			//查询该方式下所有公司
			QueryWrapper<CustomerDetailsEntity> queryWrapper = new QueryWrapper<>();
			queryWrapper.eq("company_type", declarationMethodEntity.getCompanyType())
					.like("tax_type", declarationMethodEntity.getTaxType());
			List<CustomerDetailsEntity> customerDetailsEntities = customerDetailsService.list(queryWrapper);
			//根据申报方式生成申报明细
			if ("1".equals(declarationMethodEntity.getDeclarationMethod())) {
				//按月 排除本月内生成过申报的
				QueryWrapper<DeclarationDetailsEntity> queryWrapperD = new QueryWrapper<>();
				queryWrapperD.eq("tax_type", declarationMethodEntity.getTaxType())
						.eq("declaration_method", declarationMethodEntity.getDeclarationMethod())
						.in("customer_id", customerDetailsEntities.stream().map
								(CustomerDetailsEntity::getId).collect(Collectors.toList())
						).eq("declaration_month", month);
				List<String> ids = declarationDetailsService.list(queryWrapperD).stream()
						.map(DeclarationDetailsEntity::getCustomerId).collect(Collectors.toList());
				List<DeclarationDetailsEntity> entityList = customerDetailsEntities.stream().filter(entity ->
						!ids.contains(entity.getId())).map(customerDetailsEntity -> {
					DeclarationDetailsEntity entity = new DeclarationDetailsEntity();
					entity.setCustomerId(customerDetailsEntity.getId());
					entity.setCompanyName(customerDetailsEntity.getCompanyName());
					entity.setCompanyCode(customerDetailsEntity.getCompanyCode());
					entity.setDeclarationMethod(declarationMethodEntity.getDeclarationMethod());
					entity.setDeclarationMonth(month);
					entity.setCreateTime(date);
					entity.setCreateBy("system");
					entity.setTaxType(declarationMethodEntity.getTaxType());
					return entity;
				}).collect(Collectors.toList());
				detailsEntities.addAll(entityList);
			} else if ("2".equals(declarationMethodEntity.getDeclarationMethod())) {
				//按季度 排除三个月内生成过申报的
				QueryWrapper<DeclarationDetailsEntity> queryWrapperD = new QueryWrapper<>();
				queryWrapperD.eq("tax_type", declarationMethodEntity.getTaxType())
						.eq("declaration_method", declarationMethodEntity.getDeclarationMethod())
						.in("customer_id", customerDetailsEntities.stream().map
								(CustomerDetailsEntity::getId).collect(Collectors.toList())
						).gt(false, "declaration_month", month - 3);
				List<String> ids = declarationDetailsService.list(queryWrapperD).stream()
						.map(DeclarationDetailsEntity::getCustomerId).collect(Collectors.toList());
				List<DeclarationDetailsEntity> entityList = customerDetailsEntities.stream().filter(
								customerDetailsEntity -> !ids.contains(customerDetailsEntity.getId())).
						map(customerDetailsEntity -> {
							DeclarationDetailsEntity entity = new DeclarationDetailsEntity();
							entity.setCustomerId(customerDetailsEntity.getId());
							entity.setCompanyName(customerDetailsEntity.getCompanyName());
							entity.setCompanyCode(customerDetailsEntity.getCompanyCode());
							entity.setDeclarationMethod(declarationMethodEntity.getDeclarationMethod());
							entity.setDeclarationMonth(month);
							entity.setCreateTime(date);
							entity.setCreateBy("system");
							entity.setTaxType(declarationMethodEntity.getTaxType());
							return entity;
						}).collect(Collectors.toList());
				detailsEntities.addAll(entityList);
			} else if ("3".equals(declarationMethodEntity.getDeclarationMethod())) {
				//按年 排除12个月内生成过申报的
				QueryWrapper<DeclarationDetailsEntity> queryWrapperD = new QueryWrapper<>();
				queryWrapperD.eq("tax_type", declarationMethodEntity.getTaxType())
						.eq("declaration_method", declarationMethodEntity.getDeclarationMethod())
						.in("customer_id", customerDetailsEntities.stream().map
								(CustomerDetailsEntity::getId).collect(Collectors.toList())
						).gt(false, "declaration_month", month - 12);
				List<String> ids = declarationDetailsService.list(queryWrapperD).stream()
						.map(DeclarationDetailsEntity::getCustomerId).collect(Collectors.toList());
				List<DeclarationDetailsEntity> entityList = customerDetailsEntities.stream().filter(
								customerDetailsEntity -> !ids.contains(customerDetailsEntity.getId())).
						map(customerDetailsEntity -> {
							DeclarationDetailsEntity entity = new DeclarationDetailsEntity();
							entity.setCustomerId(customerDetailsEntity.getId());
							entity.setCompanyName(customerDetailsEntity.getCompanyName());
							entity.setCompanyCode(customerDetailsEntity.getCompanyCode());
							entity.setDeclarationMethod(declarationMethodEntity.getDeclarationMethod());
							entity.setDeclarationMonth(month);
							entity.setCreateTime(date);
							entity.setCreateBy("system");
							entity.setTaxType(declarationMethodEntity.getTaxType());
							return entity;
						}).collect(Collectors.toList());
				detailsEntities.addAll(entityList);
			}
		});
		try {
			declarationDetailsService.saveBatch(detailsEntities);
			log.info("生成结果:成功,条数:{}", detailsEntities.size());
		} catch (Exception e) {
			log.info("生成结果:失败:{}", e.getMessage());
		}
		log.info("生成申报明细定时任务结束");
	}
}
