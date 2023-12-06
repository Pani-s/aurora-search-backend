//package com.pani.searchsoso;
//import java.net.InetSocketAddress;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.stream.Collectors;
//
//
//import com.alibaba.otter.canal.client.CanalConnectors;
//import com.alibaba.otter.canal.client.CanalConnector;
//import com.alibaba.otter.canal.common.utils.AddressUtils;
//import com.alibaba.otter.canal.protocol.CanalEntry;
//import com.alibaba.otter.canal.protocol.Message;
//import com.alibaba.otter.canal.protocol.CanalEntry.Column;
//import com.alibaba.otter.canal.protocol.CanalEntry.Entry;
//import com.alibaba.otter.canal.protocol.CanalEntry.EntryType;
//import com.alibaba.otter.canal.protocol.CanalEntry.EventType;
//import com.alibaba.otter.canal.protocol.CanalEntry.RowChange;
//import com.alibaba.otter.canal.protocol.CanalEntry.RowData;
//import com.pani.searchsoso.esdao.PostEsDao;
//import com.pani.searchsoso.model.dto.post.PostEsDTO;
//import com.pani.searchsoso.model.entity.Post;
//import org.springframework.boot.test.context.SpringBootTest;
//
//import javax.annotation.Resource;
//
///**
// * @author Pani
// * {@code @date} Created in 2023/11/12 14:36
// * {@code @description}
// */
//@SpringBootTest
//public class ClientSample {
//    @Resource
//    private PostEsDao postEsDao;
//    public static void main(String args[]) {
//        // 创建链接
//        CanalConnector connector = CanalConnectors.newSingleConnector(new InetSocketAddress(AddressUtils.getHostIp(),
//                11111), "example", "", "");
//        int batchSize = 1000;
//        int emptyCount = 0;
//        try {
//            connector.connect();
//            connector.subscribe(".*\\..*");
//            connector.rollback();
//            int totalEmptyCount = 120;
//            while (emptyCount < totalEmptyCount) {
//                Message message = connector.getWithoutAck(batchSize); // 获取指定数量的数据
//                long batchId = message.getId();
//                int size = message.getEntries().size();
//                if (batchId == -1 || size == 0) {
//                    emptyCount++;
//                    System.out.println("empty count : " + emptyCount);
//                    try {
//                        Thread.sleep(1000);
//                    } catch (InterruptedException e) {
//                    }
//                } else {
//                    emptyCount = 0;
//                    // System.out.printf("message[batchId=%s,size=%s] \n", batchId, size);
//                    printEntry(message.getEntries());
//                }
//
//                connector.ack(batchId); // 提交确认
//                // connector.rollback(batchId); // 处理失败, 回滚数据
//            }
//
//            System.out.println("empty too many times, exit[空太多次了，我先跑了]");
//        } finally {
//            connector.disconnect();
//        }
//    }
//
//    private static void printEntry(List<Entry> entrys) {
//        for (Entry entry : entrys) {
//            if (entry.getEntryType() == EntryType.TRANSACTIONBEGIN || entry.getEntryType() == EntryType.TRANSACTIONEND) {
//                continue;
//            }
//
//            RowChange rowChage = null;
//            try {
//                rowChage = RowChange.parseFrom(entry.getStoreValue());
//            } catch (Exception e) {
//                throw new RuntimeException("ERROR ## parser of eromanga-event has an error , data:" + entry.toString(),
//                        e);
//            }
//
//            EventType eventType = rowChage.getEventType();
//            System.out.println(String.format("================&gt; binlog[%s:%s] , name[%s,%s] , eventType : %s",
//                    entry.getHeader().getLogfileName(), entry.getHeader().getLogfileOffset(),
//                    entry.getHeader().getSchemaName(), entry.getHeader().getTableName(),
//                    eventType));
//
//            for (RowData rowData : rowChage.getRowDatasList()) {
//
//                // 操作前数据
//                List<CanalEntry.Column> beforeColumnsList =
//                        rowData.getBeforeColumnsList();
//                // 操作后数据
//                List<CanalEntry.Column> afterColumnsList = rowData.getAfterColumnsList();
//
//                if (eventType == EventType.DELETE) {
//                    // 删除操作，只有删除前的数据
//                    for (CanalEntry.Column column : beforeColumnsList) {
//                        // 既要删post，还要删跟post相关联的点赞表用户表，需要事务，很麻烦
//                    }
//                    printColumn(rowData.getBeforeColumnsList());
//                } else if (eventType == EventType.INSERT) {
//                    // 新增数据，只有新增后的数据
//                    WriteES(afterColumnsList);
//                    printColumn(rowData.getAfterColumnsList());
//                } else {
//                    //替换这里吧。
//                    System.out.println("-------&gt; before");
//                    printColumn(rowData.getBeforeColumnsList());
//                    System.out.println("-------&gt; after");
//                    printColumn(rowData.getAfterColumnsList());
//                }
//            }
//        }
//    }
//
//    private static void printColumn(List<Column> columns) {
//        for (Column column : columns) {
//            System.out.println(column.getName() + " : " + column.getValue() + "    update=" + column.getUpdated());
//        }
//    }
//
//    private void WriteES(List<CanalEntry.Column> afterColumnsList) {
//        // 处理数据
//        List<Post> postList =new ArrayList<>();
//        for (CanalEntry.Column column : afterColumnsList) {
//            Post postCache =new Post();
//            //使用Optional优雅的判空，当map字段为空时，赋默认值空串
//            String field = column.getName();
//            if("title".equals(field)){
//                postCache.setTitle(column.getValue());
//            }
//            if("content".equals(field)){
//                postCache.setContent(column.getValue());
//            }
//
//            if("tags".equals(field)){
//                postCache.setTags(column.getValue());
//            }
//            if("userId".equals(field)){
//                postCache.setUserId(Long.valueOf(column.getValue()));
//            }
//            if("id".equals(field)){
//                postCache.setId(Long.valueOf(column.getValue()));
//            }
//            postList.add(postCache);
//        }
//        List<PostEsDTO> postEsDTOList = postList.stream().map(PostEsDTO::objToDto).collect(Collectors.toList());
//        postEsDao.saveAll(postEsDTOList);
//    }
//}
