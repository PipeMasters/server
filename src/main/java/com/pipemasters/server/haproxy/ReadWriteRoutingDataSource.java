package com.pipemasters.server.haproxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.transaction.support.TransactionSynchronizationManager;

public class ReadWriteRoutingDataSource extends AbstractRoutingDataSource {
    private final static Logger log = LoggerFactory.getLogger(ReadWriteRoutingDataSource.class);
    @Override
    protected Object determineCurrentLookupKey() {
        boolean readOnlyTx = TransactionSynchronizationManager.isCurrentTransactionReadOnly();
        log.debug("Determining data source type: readOnlyTx = {}", readOnlyTx);
        return readOnlyTx ? DataSourceType.READ : DataSourceType.WRITE;
    }
}