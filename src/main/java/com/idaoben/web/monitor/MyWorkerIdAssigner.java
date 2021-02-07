package com.idaoben.web.monitor;

import com.baidu.fsg.uid.worker.WorkerIdAssigner;


public class MyWorkerIdAssigner implements WorkerIdAssigner {

    @Override
    public long assignWorkerId() {
        return 1;
    }

}
