/*
 * Copyright 2021 Shulie Technology, Co.Ltd
 * Email: shulie@shulie.io
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.shulie.surge.data.runtime.common.utils;

import com.google.common.collect.Maps;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author sunshiyu
 * @description 规则匹配测试用例
 * @datetime 2021-08-02 9:35 下午
 */
public class ApiProcessorTest {

    @Test
    public void testMerge() {
        Map<String, List<String>> newApiMap = Maps.newHashMap();
        newApiMap.put("GET", Arrays.asList("/hello/{name}", "/{id}/{name}", "/path/{id}/test", "/path/{id}/test/{num}", "/path/{id}", "/{id}/{name}/test", "/{id}"));
        ApiProcessor.MATHERS.clear();
        ApiProcessor.API_COLLECTION.put("test", newApiMap);
        Assert.assertEquals("/123/hello/sdsds/dsds", ApiProcessor.merge("test", "/123/hello/sdsds/dsds", "GET"));
        Assert.assertEquals("/hello/sdsds/dsds", ApiProcessor.merge("test", "/hello/sdsds/dsds", "GET"));
        Assert.assertEquals("/hello/{name}", ApiProcessor.merge("test", "/hello/yyy", "GET"));
        Assert.assertEquals("/{id}/{name}", ApiProcessor.merge("test", "/mmm/nnn", "GET"));
        Assert.assertEquals("/path/{id}/test", ApiProcessor.merge("test", "/path/321/test", "GET"));
        Assert.assertEquals("/path/{id}/test/{num}", ApiProcessor.merge("test", "/path/321/test/654", "GET"));
        Assert.assertEquals("/path/{id}", ApiProcessor.merge("test", "/path/123", "GET"));
        Assert.assertEquals("/{id}/{name}/test", ApiProcessor.merge("test", "/123/haha/test", "GET"));
        Assert.assertEquals("/{id}", ApiProcessor.merge("test", "/123", "GET"));
    }

    @Test
    public void testMerge1() {
        Map<String, List<String>> newApiMap = Maps.newHashMap();
        newApiMap.put("GET", Arrays.asList("/api/customer/coupons/availableCouponInfo/{couponId}", "/api/customer/activities/{activity}/calendar", "/api/customer/activities/{activity}/faq"));
        ApiProcessor.MATHERS.clear();
        ApiProcessor.API_COLLECTION.put("test", newApiMap);
        Assert.assertEquals("/api/customer/activities/activity", ApiProcessor.merge("test", "/api/customer/activities/activity", "GET"));
        Assert.assertEquals("/api/customer/activities/ongoing", ApiProcessor.merge("test", "/api/customer/activities/ongoing", "GET"));
    }


    @Test
    public void testMerge2() {
        Map<String, List<String>> newApiMap = Maps.newHashMap();
        newApiMap.put("GET", Arrays.asList("/AlteredVersion/addAlteredVersion", "/AlteredVersion/getAlteredVersionByPage", "/AlteredVersion/getAlteredVersionDetailById", "/AlteredVersion/getPageByAlteredVersionAndPage", "/AlteredVersion/getPageTypeCountByAlteredId", "/biz/add", "/biz/adjustBizBusinessAbilityExtPriorityConflict", "/biz/bindingBusinessAbility", "/biz/codeScanning/{id}", "/biz/createDemoBiz", "/biz/deleteBizById", "/biz/demoBizStatusChange", "/biz/deploy", "/biz/editBizDetail", "/biz/getAllBizListByBizCode", "/biz/getBizDetail", "/biz/getBizList", "/biz/getBizVersionList", "/biz/getConfig", "/biz/getDemoBizListByPage", "/biz/publishConfigToNacos", "/biz/queryBizBusinessAbilityExtPriorityConflict", "/biz/queryBizDetailBizConfig", "/biz/queryBizDetailBizConfigList", "/biz/queryBizDetailUsedProductListByPage", "/biz/queryBizbindingBusinessAbility", "/biz/unBindingBusinessAbility", "/biz/updateBindingBusinessAbility", "/biz/updateBizBusinessActivityScenarioBpmRel", "/biz/updateBizStatus", "/biz/updateDemoBiz", "/biz/uploadImg", "/bizIdentityController/checkBizIdentity", "/bizIdentityController/checkBizIdentityByList", "/bizIdentityController/getBizIdentityByBizCode", "/bizIdentityController/getRelationOperator", "/bizIdentityController/saveBizIdentity", "/bizIdentityModel/add", "/bizIdentityModel/getAllBizIdentityModel", "/bizIdentityModel/getBusinessIdentityModelByPage", "/bizIdentityModel/update", "/blockAltered/detail", "/blockAltered/operate", "/blockLayoutAltered/insert", "/blockLayoutAltered/list", "/blockLayoutAltered/update", "/bpmDefineController/abandonBpmDraft", "/bpmDefineController/checkHasBpmDraft", "/bpmDefineController/getAllBpmDefineByCondition", "/bpmDefineController/getBpmDefineByPage", "/bpmDefineController/getBpmDefineDetail", "/bpmDefineController/getScriptNodeTypeList", "/bpmDefineController/saveBpmDefine", "/businessAbility/add", "/businessAbility/codeScanning/{id}", "/businessAbility/getBizBusinessAbilityInfoList", "/businessAbility/getBizBusinessAbilityList", "/businessAbility/list", "/businessAbility/listForSolutionCase", "/businessAbility/look/bizExt", "/businessAbility/look/detail", "/businessAbility/look/statisticsBiz", "/businessAbility/look/statisticsProduct", "/businessAbility/queryBizBusinessAbilityInfoListByAbilityCode", "/businessAbility/update", "/businessAbilityExt/bizUsedetail", "/businessAbilityExt/delete/{id}", "/businessAbilityExt/extDetail", "/businessAbilityExt/getBusinessAbilityExtByAbilityCodeAndActivityCode", "/businessAbilityExt/getBusinessAbilityExtByPage", "/businessAbilityExt/getProductBusinessAbilityExtByPage", "/businessAbilityExt/list", "/businessAbilityExt/productUseDetail", "/businessAbilityExt/use/{id}", "/businessActivity/addBizActivityScenarioBmpRelation", "/businessActivity/getBizActivityDetail", "/businessActivity/getBmpDetailExtList", "/businessActivity/getRelatedSceneByBizActicity", "/businessActivity/list", "/businessActivity/query", "/businessActivity/queryBusinessActivityList", "/businessActivity/registerBusinessActivity", "/businessActivity/removeBizActivityScenarioBmpRelation", "/businessActivity/saveBusinessActivity", "/businessActivity/scanBusinessActivityCode", "/businessActivity/updateBizActivityScenarioBmpRelation", "/businessFlow/delete/{id}", "/businessFlow/list", "/businessFlow/operate", "/businessFlow/queryAll", "/category/add", "/category/delete/{id}", "/category/tree", "/category/update", "/category/uploadLogo", "/check", "/checkpreload.htm", "/codeScanErrorLog/getAllCodeScanType", "/codeScanErrorLog/getCodeScanErrorLogErrorDataById", "/codeScanErrorLog/getLatestErrorLogByPage", "/component/detail/{id}", "/component/getComponentUsedInfo", "/component/initComponentByJsFile", "/component/list", "/component/operate", "/component/queryComponentMetaDataVO", "/componentModelAttr/getAllComponentModelAttrByComponentId", "/domain/queryDomainDetail", "/domain/queryDomainList", "/domainServiceController/getDomainServiceByDomainCode", "/domainServiceController/getDomainServiceByPage", "/domainServiceController/getDomainServiceDetailById", "/domainServiceController/getDomainServiceInfoList", "/domainServiceController/queryDomainServiceByDomainCode", "/git/{spaceKey}", "/git/{spaceKey}/blame", "/git/{spaceKey}/branch", "/git/{spaceKey}/branches", "/git/{spaceKey}/branches", "/git/{spaceKey}/branches", "/git/{spaceKey}/checkout", "/git/{spaceKey}/commits", "/git/{spaceKey}/commits", "/git/{spaceKey}/conflicts", "/git/{spaceKey}/conflicts", "/git/{spaceKey}/conflicts", "/git/{spaceKey}/fetch", "/git/{spaceKey}/logs", "/git/{spaceKey}/merge", "/git/{spaceKey}/pull", "/git/{spaceKey}/push", "/git/{spaceKey}/push", "/git/{spaceKey}/read", "/git/{spaceKey}/rebase", "/git/{spaceKey}/rebase/operate", "/git/{spaceKey}/rebase/update", "/git/{spaceKey}/refs", "/git/{spaceKey}/reset", "/git/{spaceKey}/rollbacks", "/git/{spaceKey}/stash", "/git/{spaceKey}/stash", "/git/{spaceKey}/stash", "/git/{spaceKey}/stash/apply", "/git/{spaceKey}/stash/checkout", "/git/{spaceKey}/submodules/sync", "/git/{spaceKey}/tags", "/git/{spaceKey}/tags", "/gitController/createGitProject", "/jenkins/build", "/jenkins/buildWithParam", "/jenkins/getResult", "/jenkins/log", "/log/list", "/nacos/loadAllBizListToNacos", "/nacos/loadAllSystemListToNacos", "/nacos/loadConfToRedis", "/nacos/loadVersionToNacos", "/packages", "/packages/{name}/{version}/{fileName:.+}", "/page/add", "/page/copyPage/{pageId}", "/page/list", "/page/publishAltered", "/page/queryFrameWorkPageParams", "/page/setHomePageUrl", "/page/update", "/pageAltered/add", "/pageAltered/copyPage/{pageId}", "/pageAltered/delete/{pageId}", "/pageAltered/detail", "/pageAltered/list", "/pageAltered/lockEdit", "/pageAltered/lockPage/{pageId}", "/pageAltered/unlockPage/{pageId}", "/pageAltered/update", "/pageAltered/updatePageBaseInfo", "/pageDraft/add", "/pageDraft/list", "/pageDraft/update", "/product/addProduct", "/product/getProductByPage", "/product/getProductDetailById", "/product/getProductGroup", "/product/getProductUseInfoByPage", "/product/modifyProduct", "/product/queryBusinessAbilityByProductId", "/productUseScope/getBizTreeByProductCode", "/productUseScope/getBizTreeByProductId", "/productUseScope/getProductUseScopeByProductCodeAndPage", "/productUseScope/getProductUseScopeByProductIdAndPage", "/productUseScope/modifyBizUsedProductScope", "/productUseScope/modifyProduceUseScope", "/projects", "/roleManager/add", "/roleManager/delete/{id}", "/roleManager/list", "/roleManager/update", "/scenarioController/addScene", "/scenarioController/getAllScenarioByCondition", "/scenarioController/getAllScenarioByConditionAndPage", "/scenarioController/getScenarioByPage", "/solutionCase/add", "/solutionCase/delete/{id}", "/solutionCase/detail/system", "/solutionCase/detail/{id}", "/solutionCase/list", "/solutionCase/update", "/solutionCase/updateSystem", "/solutionCase/uploadLogo", "/system/add", "/system/delete/{id}", "/system/getOnlineBizByApp", "/system/getSystemByBiz", "/system/list", "/system/update", "/system/uploadLogo", "/tesla/account/delete", "/tesla/account/list", "/tesla/account/listone", "/tesla/account/save", "/tesla/org/add", "/tesla/org/bindRoles", "/tesla/org/delete", "/tesla/org/queryOrgTreeList", "/tesla/org/queryRoleIdsByOrg", "/tesla/org/queryRolesByOrg", "/tesla/org/update", "/tesla/resource/button/add", "/tesla/resource/button/delete", "/tesla/resource/button/queryByPage", "/tesla/resource/button/queryByRoleAndMenu", "/tesla/resource/button/save/{roleId}", "/tesla/resource/button/update", "/tesla/resource/interface/add", "/tesla/resource/interface/delete", "/tesla/resource/interface/queryByPage", "/tesla/resource/interface/save/{roleId}", "/tesla/resource/interface/update", "/tesla/resource/menu/add", "/tesla/resource/menu/delete", "/tesla/resource/menu/list", "/tesla/resource/menu/save/{roleId}", "/tesla/resource/menu/update", "/tesla/role/delete", "/tesla/role/list", "/tesla/role/listAll", "/tesla/role/resource/list", "/tesla/role/save", "/tesla/user/login", "/tesla/user/logout", "/tesla/user/menu/list", "/tesla/user/updatepasswd", "/user", "/workspaces", "/workspaces", "/workspaces/{spaceKey}", "/workspaces/{spaceKey}", "/workspaces/{spaceKey}/copy", "/workspaces/{spaceKey}/encoding", "/workspaces/{spaceKey}/file/read", "/workspaces/{spaceKey}/files", "/workspaces/{spaceKey}/files", "/workspaces/{spaceKey}/files", "/workspaces/{spaceKey}/files", "/workspaces/{spaceKey}/mkdir", "/workspaces/{spaceKey}/move", "/workspaces/{spaceKey}/pack", "/workspaces/{spaceKey}/raw", "/workspaces/{spaceKey}/search", "/workspaces/{spaceKey}/settings", "/workspaces/{spaceKey}/settings", "/workspaces/{spaceKey}/setup", "/workspaces/{spaceKey}/upload", "/hello/{name}", "/{id}/{name}", "/path/{id}/test", "/path/{id}/test/{num}", "/path/{id}", "/{id}/{name}/test", "/{id}"));
        ApiProcessor.MATHERS.clear();
        ApiProcessor.API_COLLECTION.put("test", newApiMap);
        long start = System.currentTimeMillis();
        Assert.assertEquals("/{id}/{name}", ApiProcessor.merge("test", "/mmm/yyy", "GET"));
        System.out.println("新方法耗时:" + (System.currentTimeMillis() - start));

        Assert.assertEquals("/path/{id}/test/{num}", ApiProcessor.merge("test", "/path/yyy/test/123", "GET"));

        start = System.currentTimeMillis();
        Assert.assertEquals("/hello/{name}", ApiProcessor.oldMerge("test", "/hello/yyy", "GET"));
        System.out.println("原方法耗时:" + (System.currentTimeMillis() - start));
    }
}