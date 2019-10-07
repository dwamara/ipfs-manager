package com.dwitech.blockchain.ipfs.processor.control;

import com.dwitech.blockchain.ipfs.processor.entity.FormData;
import com.dwitech.blockchain.ipfs.processor.entity.InsertResponse;
import net.consensys.mahuta.core.Mahuta;
import net.consensys.mahuta.core.MahutaFactory;
import net.consensys.mahuta.core.domain.common.MetadataAndPayload;
import net.consensys.mahuta.core.domain.createindex.CreateIndexResponse;
import net.consensys.mahuta.core.domain.get.GetResponse;
import net.consensys.mahuta.core.domain.getindexes.GetIndexesResponse;
import net.consensys.mahuta.core.domain.indexing.IndexingResponse;
import net.consensys.mahuta.core.domain.search.SearchResponse;
import net.consensys.mahuta.core.indexer.elasticsearch.ElasticSearchService;
import net.consensys.mahuta.core.service.storage.StorageService;
import net.consensys.mahuta.core.service.storage.ipfs.IPFSService;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.consensys.mahuta.core.domain.Response.ResponseStatus.SUCCESS;
import static net.consensys.mahuta.core.domain.common.pagination.PageRequest.of;
import static net.consensys.mahuta.core.domain.common.query.Query.newQuery;

@ApplicationScoped
public class Documents {
    @ConfigProperty(name = "ipfs.host") String ipfsHost;
    @ConfigProperty(name = "ipfs.port") int ipfsPort;
    @ConfigProperty(name = "elasticsearch.host") String elasticsearchHost;
    @ConfigProperty(name = "elasticsearch.port") int elasticsearchPort;
    @ConfigProperty(name = "elasticsearch.cluster.name") String clusterName;
    @ConfigProperty(name = "elasticsearch.index.name") String indexName;

    StorageService storage;
    ElasticSearchService indexer;
    Mahuta mahuta;

    @PostConstruct
    public void init() {
        storage = IPFSService.connect(ipfsHost, ipfsPort);
        indexer = ElasticSearchService.connect(elasticsearchHost, elasticsearchPort, clusterName).withIndex(indexName);
        mahuta = new MahutaFactory()
                .configureStorage(storage)
                .configureIndexer(indexer)
                .defaultImplementation();

        createIndex();
    }

    void createIndex() {
        final GetIndexesResponse getIndexesResponse = mahuta.prepareGetIndexes().execute();
        if (getIndexesResponse.getStatus() == SUCCESS) {
            List<String> indexes = getIndexesResponse.getIndexes();
            for (String index : indexes) {
                if (index == indexName) {
                    System.out.println("Index <" + indexName + "> already created.");
                    return;
                }
            }
        }

        System.out.println("Index <" + indexName + "> not yet created. Creating...");

        final CreateIndexResponse createIndexResponse = mahuta.prepareCreateIndex(indexName).execute();

        System.out.println("Index <" + indexName + "> creation: " + (createIndexResponse.getStatus() != SUCCESS ? "failure" : "success"));
    }

    /**
     * {
     *   "indexName": "article",
     *   "indexDocId": "hello_world",
     *   "contentId": "QmWHR4e1JHMs2h7XtbDsS9r2oQkyuzVr5bHdkEMYiqfeNm",
     *   "contentType": "text/markdown",
     *   "content": null,
     *   "pinned": true,
     *   "indexFields": {
     *     "title": "Hello world",
     *     "author": "Gregoire Jeanmart",
     *     "votes": 10,
     *     "createAt": 1518700549,
     *     "tags": [
     *       "general"
     *     ]
     *   },
     *   "status": "SUCCESS"
     * }
     * @param formData
     * @return
     */
    public InsertResponse storeAndIndex(final FormData formData) {
        try {
            IndexingResponse response = mahuta.prepareInputStreamIndexing(indexName, new FileInputStream(formData.getDocument()))
                    .indexDocId("article-1")
                    .contentType(formData.getFileContentType())
                    .indexFields(getIndexFields(formData))
                    .execute();
            if (response.getStatus() == SUCCESS) {
                InsertResponse insertResponse = new InsertResponse();
                insertResponse.setContentId(response.getContentId());
                insertResponse.setFields(response.getIndexFields());
                insertResponse.setIndexDocumentId(response.getIndexDocId());
                insertResponse.setIndexName(response.getIndexName());
                return insertResponse;
            }
        } catch (FileNotFoundException fnfExc) {
            fnfExc.printStackTrace();
        }
        return null;
    }

    public void getFile(final String contentId) {
        final GetResponse response = mahuta.prepareGet()
                //.indexName(indexName)
                .contentId(contentId)
                .loadFile(true)
                .execute();

        if (response.getStatus() == SUCCESS) {
            System.out.println(response.getPayload());
            return;
        }
    }

    private Map<String, Object> getIndexFields(final FormData formData) {
        final Map<String, Object> indexFields = new HashMap();
        indexFields.put("name", formData.getDocument().getName());
        indexFields.put("client_id", formData.getClientId());
        indexFields.put("content_type", formData.getFileContentType());
        return indexFields;
    }

    /**
     {
     "status": "SUCCESS",
     "page": {
     "pageRequest": {
     "page": 0,
     "size": 20,
     "sort": null,
     "direction": "ASC"
     },
     "elements": [
     {
     "metadata": {
     "indexName": "article",
     "indexDocId": "hello_world",
     "contentId": "Qmd6VkHiLbLPncVQiewQe3SBP8rrG96HTkYkLbMzMe6tP2",
     "contentType": "text/markdown",
     "content": null,
     "pinned": true,
     "indexFields": {
     "author": "Gregoire Jeanmart",
     "votes": 10,
     "title": "Hello world",
     "createAt": 1518700549,
     "tags": [
     "general"
     ]
     }
     },
     "payload": null
     }
     ],
     "totalElements": 1,
     "totalPages": 1
     }
     }
     */
    private void search() {
        final SearchResponse response = mahuta.prepareSearch()
                .indexName(indexName)
                .query(newQuery().equals("author", "greg"))
                .pageRequest(of(0, 20))
                .loadFile(false)
                .execute();

        if (response.getStatus() == SUCCESS) {
            List<MetadataAndPayload> elements = response.getPage().getElements();
            return;
        }
    }
}