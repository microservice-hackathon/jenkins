package pl.wybcz

import javaposse.jobdsl.dsl.DslFactory
import javaposse.jobdsl.dsl.Job

class GithubPrBuilder {

    private final DslFactory dslFactory

    GithubPrBuilder(DslFactory dslFactory) {
        this.dslFactory = dslFactory
    }

    Job buildPrJob(String projectName) {
        return dslFactory.freeStyleJob("$projectName-pr-build") {

        }
    }
}
