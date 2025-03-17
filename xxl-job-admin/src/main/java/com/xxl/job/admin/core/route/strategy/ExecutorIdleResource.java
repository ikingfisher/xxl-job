package com.xxl.job.admin.core.route.strategy;

import com.xxl.job.admin.core.conf.XxlJobAdminConfig;
import com.xxl.job.admin.core.model.XxlJobRegistry;
import com.xxl.job.admin.core.route.ExecutorRouter;
import com.xxl.job.admin.core.util.I18nUtil;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.biz.model.TriggerParam;
import com.xxl.job.core.monitor.SystemResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class ExecutorIdleResource extends ExecutorRouter {
    private static Logger logger = LoggerFactory.getLogger(ExecutorIdleResource.class);

    private static Random localRandom = new Random();

    @Override
    public ReturnT<String> route(TriggerParam triggerParam, List<String> addressList) {
        logger.info(">>>>>>>>>>> job {}, require resource: {}", triggerParam.getJobId(), triggerParam.getRequireResource());
        StringBuffer idleBeatResultSB = new StringBuffer();
        Integer requireMem = SystemResource.parseMemory(triggerParam.getRequireResource());
        if (requireMem == null) {
            String address = addressList.get(localRandom.nextInt(addressList.size()));
            return new ReturnT<String>(address);
        }

        Map<String, Integer> xxlJobRegistrysMap = new HashMap<>();
        for (String address : addressList) {
            // beat
            ReturnT<String> idleBeatResult = null;

            List<XxlJobRegistry> xxlJobRegistrys = XxlJobAdminConfig.getAdminConfig().getXxlJobRegistryDao().loadByAddress(address);
            if (xxlJobRegistrys != null && !xxlJobRegistrys.isEmpty()) {
                for (XxlJobRegistry xxlJobRegistry : xxlJobRegistrys) {
                    logger.info(">>>>>>>>>>> ExecutorIdleResource xxlJobRegistry address: {}, resource: {}", xxlJobRegistry.getRegistryValue(), xxlJobRegistry.getSystemResource());
                    Integer mem = SystemResource.parseMemory(xxlJobRegistry.getSystemResource());

                    if ( mem < requireMem ) {
                        idleBeatResult = new ReturnT<String>(ReturnT.FAIL_CODE, "memory not available." );
                        idleBeatResultSB.append( (idleBeatResultSB.length()>0)?"<br><br>":"")
                                .append(I18nUtil.getString("jobconf_idleBeat") + "：")
                                .append("<br>address：").append(address)
                                .append("<br>code：").append(idleBeatResult.getCode())
                                .append("<br>msg：").append(idleBeatResult.getMsg());
                    } else {
                        xxlJobRegistrysMap.put(xxlJobRegistry.getRegistryValue(), mem);
                    }
                }
            }
        }
        String address = getKeyWithMaxValue(xxlJobRegistrysMap);
        if (address == null) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, idleBeatResultSB.toString());
        }
        return new ReturnT<String>(address);
    }

    /**
     * 从给定的Map中找到值最大的键。
     * @param inputMap 输入的Map<String, Integer>
     * @return 值最大的键，如果Map为空或null，则返回null
     */
    public String getKeyWithMaxValue(Map<String, Integer> inputMap) {
        if (inputMap == null || inputMap.isEmpty()) {
            return null;
        }

        Map.Entry<String, Integer> maxEntry = null;
        for (Map.Entry<String, Integer> entry : inputMap.entrySet()) {
            if (maxEntry == null || entry.getValue().compareTo(maxEntry.getValue()) > 0) {
                maxEntry = entry;
            }
        }

        return maxEntry != null ? maxEntry.getKey() : null;
    }
}
