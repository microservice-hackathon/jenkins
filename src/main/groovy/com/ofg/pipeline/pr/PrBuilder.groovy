package com.ofg.pipeline.pr

import javaposse.jobdsl.dsl.Job

interface PrBuilder {
    Job buildPrJob(String projectName)
}