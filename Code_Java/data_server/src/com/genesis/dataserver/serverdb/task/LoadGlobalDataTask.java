package com.genesis.dataserver.serverdb.task;

import com.genesis.dataserver.globals.Globals;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.common.collect.Table.Cell;

import com.genesis.core.orm.hibernate.HibernateDBService;
import com.genesis.core.redis.IRedis;
import com.genesis.core.redis.op.IPipelineOp;
import com.genesis.core.redis.op.PipelineProcess;
import com.genesis.dataserver.serverdb.ServerDBManager;
import com.genesis.dataserver.serverdb.task.loadglobaldata.GlobalDataLoader;
import com.genesis.gamedb.human.HumanInfo;
import com.genesis.gamedb.orm.entity.AccountEntity;
import com.genesis.gamedb.orm.entity.HumanEntity;
import com.genesis.gamedb.redis.key.SpecialRedisKeyBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * 加载全局数据和账号数据，放进Redis
 * @author Joey
 *
 */
public class LoadGlobalDataTask implements Runnable {

    private ServerDBManager dbm;

    public LoadGlobalDataTask(ServerDBManager serverDBManager) {
        this.dbm = serverDBManager;
    }

    @Override
    public void run() {
        GlobalDataLoader.loadGlobalData(dbm);

        //加载所有账号信息及角色信息列表
        List<HumanEntity> humanList = loadAccounts();

        GlobalDataLoader.loadHumanRelatedGlobalData(dbm, humanList);
    }

    /**
     * 加载所有账号信息及角色信息列表
     * @return
     */
    private List<HumanEntity> loadAccounts() {
        HibernateDBService dbservice = Globals.getDbservice();

        //保存账号和角色的映射关系
        /** <channel, accountId, Human简要信息List> */
        final Table<String, String, List<HumanInfo>> table = HashBasedTable.create();
        final List<HumanEntity> humanList = dbservice
                .findByNamedQueryAndNamedParamAllT(HumanEntity.class, "selectHumans",
                        new String[]{"originalServerId"}, new Object[]{dbm.getOriginalServerId()});
        for (HumanEntity humanEntity : humanList) {
            String channel = humanEntity.getChannel();
            String accountId = humanEntity.getAccountId();
            if (!table.contains(channel, accountId)) {
                List<HumanInfo> humanIdList = new ArrayList<HumanInfo>();
                table.put(channel, accountId, humanIdList);
            }
            List<HumanInfo> list = table.get(channel, accountId);
            HumanInfo humanInfo = humanEntity.buildHumanInfo();
            list.add(humanInfo);
        }

        IRedis redis = dbm.getiRedis();
        IPipelineOp pip = redis.pipeline();
        pip.exec(new PipelineProcess() {

            @Override
            public void apply() {

                //账号与角色的对应关系
                for (Cell<String, String, List<HumanInfo>> cell : table.cellSet()) {
                    String accountKey =
                            SpecialRedisKeyBuilder.buildAccountKey(dbm.getCurrentServerId());
                    AccountEntity account = new AccountEntity();
                    account.setChannel(cell.getRowKey());
                    account.setId(cell.getColumnKey());
                    this.getSetOp().sadd(accountKey, account);

                    String key = SpecialRedisKeyBuilder
                            .buildAccount2HumanKey(dbm.getCurrentServerId(), cell.getRowKey(),
                                    cell.getColumnKey());
                    List<HumanInfo> tempList = cell.getValue();
                    for (HumanInfo humanInfo : tempList) {
                        this.getHashOp().hset(key, humanInfo.getId().toString(), humanInfo);
                    }
                }
            }
        });

        return humanList;
    }

}