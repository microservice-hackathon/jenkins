package pl.wybcz.pipeline.pr

import javaposse.jobdsl.dsl.Job

interface PrBuilder {
    Job buildPrJob(String projectName)
}