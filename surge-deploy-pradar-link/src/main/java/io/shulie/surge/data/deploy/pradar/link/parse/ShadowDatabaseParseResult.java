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

package io.shulie.surge.data.deploy.pradar.link.parse;

import java.util.List;

import io.shulie.surge.data.deploy.pradar.link.model.ShadowBizTableModel;
import io.shulie.surge.data.deploy.pradar.link.model.ShadowDatabaseModel;

public class ShadowDatabaseParseResult {

    private ShadowDatabaseModel databaseModel;
    private List<ShadowBizTableModel> tableModelList;

    public ShadowDatabaseParseResult(ShadowDatabaseModel databaseModel, List<ShadowBizTableModel> tableModelList) {
        this.databaseModel = databaseModel;
        this.tableModelList = tableModelList;
    }

    public ShadowDatabaseModel getDatabaseModel() {
        return databaseModel;
    }

    public void setDatabaseModel(ShadowDatabaseModel databaseModel) {
        this.databaseModel = databaseModel;
    }

    public List<ShadowBizTableModel> getTableModelList() {
        return tableModelList;
    }

    public void setTableModelList(List<ShadowBizTableModel> tableModelList) {
        this.tableModelList = tableModelList;
    }
}
