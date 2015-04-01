package pl.wybcz

import javaposse.jobdsl.dsl.Job

interface PrBuilder {
    Job buildPrJob(String projectName)
}