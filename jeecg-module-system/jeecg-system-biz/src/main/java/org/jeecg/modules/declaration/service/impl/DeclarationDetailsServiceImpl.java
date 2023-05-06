package org.jeecg.modules.declaration.service.impl;

import org.jeecg.common.system.base.service.impl.JeecgServiceImpl;
import org.jeecg.modules.declaration.entity.DeclarationDetailsEntity;
import org.jeecg.modules.declaration.mapper.DeclarationDetailsMapper;
import org.jeecg.modules.declaration.service.DeclarationDetailsService;
import org.springframework.stereotype.Service;

@Service
public class DeclarationDetailsServiceImpl extends JeecgServiceImpl<DeclarationDetailsMapper, DeclarationDetailsEntity> implements DeclarationDetailsService {
}
