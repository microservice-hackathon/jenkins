package utilities

import javaposse.jobdsl.dsl.FreeStyleJob

public class MyUtilities {
    
    def donwstreamJob(FreeStyleJob job) {
        job.with {
            wrappers {
                deliveryPipelineVersion('${ENV,var="PIPELINE_VERSION"}', true)
            }
        }
    }

}
