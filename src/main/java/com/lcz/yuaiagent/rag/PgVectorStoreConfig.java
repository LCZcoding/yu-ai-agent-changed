package com.lcz.yuaiagent.rag;

import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.springframework.ai.vectorstore.pgvector.PgVectorStore.PgDistanceType.COSINE_DISTANCE;
import static org.springframework.ai.vectorstore.pgvector.PgVectorStore.PgIndexType.HNSW;

@Configuration
@ConditionalOnProperty(name = "spring.datasource.url")
public class PgVectorStoreConfig {

    private static final Logger log = LoggerFactory.getLogger(PgVectorStoreConfig.class);

    @Resource
    private LoveAppDocumentLoader loveAppDocumentLoader;

    /*
     * TODO 恢复 PG 向量存储时需修复的 bug：（目前的操作是关闭pgvector存储）
     *   1. batch insert 时报错（大概率是远程 PG 服务器不可达，非代码 bug）：
     *        - 42.7.5: AssertionError — pgStatement.getConnection().getAutoCommit() should not throw
     *        - 42.7.7+: PSQLException — This connection has been closed
     *      排查方向：先确认 72.155.89.172:5432 通不通、PG 服务是否正常、防火墙是否放行
     *   2. 每次启动重复插入文档（vectorStore.add 无去重逻辑，这个是代码问题）
     *      修复方向：添加前先查 vector_store 表判断文档是否已存在
     */

    @Bean
    public VectorStore pgVectorStore(JdbcTemplate jdbcTemplate, EmbeddingModel dashscopeEmbeddingModel) {
        VectorStore vectorStore = PgVectorStore.builder(jdbcTemplate, dashscopeEmbeddingModel)
                .dimensions(1024)                    // 不要盲目设置
                .distanceType(COSINE_DISTANCE)       // Optional: defaults to COSINE_DISTANCE
                .indexType(HNSW)                     // Optional: defaults to HNSW
                .initializeSchema(true)              // Optional: defaults to false
                .schemaName("public")                // Optional: defaults to "public"
                .vectorTableName("vector_store")     // Optional: defaults to "vector_store"
                .maxDocumentBatchSize(10000)         // Optional: defaults to 10000
                .build();
        //加载wiki文档
        List<Document> documents = loveAppDocumentLoader.loadMarkdowns();

        // 分批添加
        int batchSize = 10;
        for(int i = 0; i < documents.size(); i += batchSize){
            //添加文档到pg存储，todo每次启动重复添加
            vectorStore.add(documents.subList(i, Math.min(i + batchSize, documents.size())));
        }
        return vectorStore;
    }
}