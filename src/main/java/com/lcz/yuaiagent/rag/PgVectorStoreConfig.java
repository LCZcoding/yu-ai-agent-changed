package com.lcz.yuaiagent.rag;

import jakarta.annotation.Resource;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.springframework.ai.vectorstore.pgvector.PgVectorStore.PgDistanceType.COSINE_DISTANCE;
import static org.springframework.ai.vectorstore.pgvector.PgVectorStore.PgIndexType.HNSW;

// PostgreSQL pgvector 向量存储配置类
// 用 @Configuration + @Bean 手动构建 PgVectorStore，因为它是第三方库的类，没有 @Component 注解
// 方法参数 JdbcTemplate 和 EmbeddingModel 由 Spring 自动注入（DI）前者便于sql书写，后者便于调用embedding模型
@Configuration
public class PgVectorStoreConfig {

    @Resource
    private LoveAppDocumentLoader loveAppDocumentLoader;

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