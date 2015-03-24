package utilities

import javaposse.jobdsl.dsl.Job

public class MyUtilities {
    
    def donwstreamJob(Job job) {
        job.with {
            wrappers {
                deliveryPipelineVersion('${ENV,var="PIPELINE_VERSION"}', true)
            }
        }
    }

}
