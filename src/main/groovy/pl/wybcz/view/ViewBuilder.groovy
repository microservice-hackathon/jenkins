package pl.wybcz.view

import groovy.transform.CompileStatic
import javaposse.jobdsl.dsl.DslFactory
import javaposse.jobdsl.dsl.JobParent
import javaposse.jobdsl.dsl.View

@CompileStatic
class ViewBuilder {

    private final DslFactory dslFactory

    ViewBuilder(DslFactory dslFactory) {
        this.dslFactory = dslFactory
    }

    View appendDashboard() {
        JobParent jobParent = dslFactory as JobParent
        View view = new Dashboard('Overview', jobParent.jm)
        jobParent.referencedViews << view
        return view
    }
}
